package com.inappstory.utils.iasfilepicker.file

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.inappstory.utils.iasfilepicker.FilePickerMainFragment
import com.inappstory.utils.iasfilepicker.R
import com.inappstory.utils.iasfilepicker.utils.BackPressedFragment
import com.inappstory.utils.iasfilepicker.utils.faststart.FastStart
import java.io.File
import java.util.UUID


internal class FilePickerFragment : BackPressedFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.cs_file_picker_fragment, null)
    }

    private lateinit var uploadButton: FloatingActionButton
    private lateinit var previews: FilePreviewsList
    private lateinit var manageButton: AppCompatButton
    private lateinit var manageHint: TextView
    private lateinit var partialLayout: View

    var acceptTypes = arrayListOf<String>()
    val selectedFiles = arrayListOf<SelectedFile>()


    private val STORAGE_PERMISSIONS_RESULT = 888
    private val CAMERA_PERMISSIONS_RESULT = 890

    private fun checkStoragePermissions() {
        activity?.apply {
            var allGranted = true;
            val localPerms = arrayListOf<String>()
            appPerms.forEach {
                if (ContextCompat.checkSelfPermission(
                        this,
                        it
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    allGranted = false;
                    localPerms.add(it)
                }
            }
            if (!allGranted) {
                if (Build.VERSION.SDK_INT >= 34) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (!loaded || previews.adapter?.itemCount == 0)
                            loadPreviews(filesAccess = FilesAccess.PARTIAL)
                    } else {
                        this.requestPermissions(
                            localPerms.toTypedArray(),
                            STORAGE_PERMISSIONS_RESULT
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(localPerms.toTypedArray(), STORAGE_PERMISSIONS_RESULT)
                }
            } else {
                if (!loaded || previews.adapter?.itemCount == 0)
                    loadPreviews(filesAccess = FilesAccess.FULL)
            }
        }
    }

    private fun checkCameraPermissions(audioCheck: Boolean = false) {
        activity?.apply {
            var allGranted = true;
            val localPerms =
                arrayListOf(Manifest.permission.CAMERA)
            if (audioCheck)
                localPerms.add(Manifest.permission.RECORD_AUDIO)
            localPerms.forEach {
                if (ContextCompat.checkSelfPermission(
                        this,
                        it
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    allGranted = false;
                }
            }
            if (!allGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(
                        localPerms.toTypedArray(),
                        CAMERA_PERMISSIONS_RESULT
                    )
                }
            } else {
                openCameraScreen()
            }
        }
    }

    private var loaded = false
    private var dialogShown = false

    private val appPerms = arrayListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= 33) {
            add("android.permission.READ_MEDIA_IMAGES")
            add("android.permission.READ_MEDIA_VIDEO")
            if (Build.VERSION.SDK_INT >= 34) {
                add("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
            }
        } else {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadButton = view.findViewById(R.id.upload)
        previews = view.findViewById(R.id.previews)
        partialLayout = view.findViewById(R.id.partialAccessLayout)
        manageButton = view.findViewById(R.id.manageAccess)
        manageHint = view.findViewById(R.id.partialAccessHint)
        arguments?.apply {
            val messageNames = getStringArray("messageNames")
            val messageValues = getStringArray("messages")
            if (messageNames != null && messageValues != null) {
                messages.putAll(messageNames.zip(messageValues).toMap())
            }
            acceptTypes = getStringArrayList("acceptTypes") ?: arrayListOf()
            messages["button_no_gallery_access"]?.let {
                galleryAccessText = it
            }
        }
        if (acceptTypes.isEmpty()) {
            activity?.onBackPressed()
            return
        }
        uploadButton.setOnClickListener {
            if (parentFragment is FilePickerMainFragment && selectedFiles.isNotEmpty()) {
                (parentFragment as FilePickerMainFragment).sendResult(convertFiles().toTypedArray())
            }
        }
        manageHint.text = messages.getOrElse(
            "android_gallery_permission_warning_label",
            defaultValue = { manageHintText })
        manageButton.text = messages.getOrElse(
            "android_gallery_permission_warning_manage_button",
            defaultValue = { manageButtonText })
        manageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                openManageBottomSheet(
                    newChoiceText = messages.getOrElse(
                        "android_gallery_permission_select_other_files_button",
                        defaultValue = { newChoice }),
                    settingsText = messages.getOrElse(
                        "android_gallery_permission_open_settings_button",
                        defaultValue = { openSettings })
                )
                /* openSettingsDialog(
                     text = "Your app has only partial access to gallery",
                     positiveText = messages.getOrElse(
                         "dialog_button_settings",
                         defaultValue = { videoDefault }),
                     negativeText = messages.getOrElse(
                         "dialog_button_not_now",
                         defaultValue = { videoDefault }),
                     neutralText = "New choice",
                     neutralCallback = {
                         requireActivity().requestPermissions(
                             appPerms,
                             STORAGE_PERMISSIONS_RESULT
                         )
                     },
                 )*/

            }
        }
    }

    private fun openManageBottomSheet(newChoiceText: String?, settingsText: String?) {
        if (dialogShown) return
        activity?.let { activity ->
            val dialog = BottomSheetDialog(activity)
            val bottomSheetLayout = layoutInflater.inflate(R.layout.cs_bottom_sheet_dialog, null)
            val newChoice = bottomSheetLayout.findViewById<TextView>(R.id.newChoice)
            val settings = bottomSheetLayout.findViewById<TextView>(R.id.settings)
            newChoiceText?.let { text -> newChoice.text = text }
            settingsText?.let { text -> settings.text = text }
            newChoice.setOnClickListener {
                dialog.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(
                        appPerms,
                        STORAGE_PERMISSIONS_RESULT
                    )
                }
            }
            settings.setOnClickListener {
                dialog.dismiss()
                openSettingsScreen()
            }
            dialog.setContentView(bottomSheetLayout)
            dialog.setOnDismissListener {
                dialogShown = false
            }
            dialog.show()
            dialogShown = true
        }

    }

    private fun convertFiles(): ArrayList<String> {
        val resultFiles = arrayListOf<String>()
        selectedFiles.forEach {
            if (it.fileType == "video") {
                val currentFile = File(it.filePath)
                currentFile.extension
                val file = File(
                    "${requireContext().filesDir}/converted",
                    "${UUID.randomUUID()}_${it.filePath}"
                )
                val fs = FastStart(currentFile.absolutePath, file.absolutePath).fastStart()
                if (fs) {
                    resultFiles.add(file.absolutePath)
                } else {
                    resultFiles.add(currentFile.absolutePath)
                }
            } else {
                resultFiles.add(it.filePath)
            }
        }
        return resultFiles
    }

    override fun onStart() {
        super.onStart()
        checkStoragePermissions()
    }

    private var galleryAccessText = "Tap to allow access to your Gallery"
    private val messages = hashMapOf<String, String>()
    private val storageDefault =
        "You need storage access to load photos and videos. Tap Settings > Permissions and turn \'Files and media\' on"
    private val videoDefault =
        "You need camera and microphone access to make photos and videos. Tap Settings > Permissions and turn 'Camera' and 'Microphone' on"
    private val openSettings = "Open settings"
    private val newChoice = "Change the choice..."
    private val manageButtonText = "Manage"
    private val manageHintText = "You did not allow the app to access the entire gallery"

    fun requestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        var allGranted = true;
        val positiveText = messages["dialog_button_settings"]
        val negativeText = messages["dialog_button_not_now"]
        if (requestCode == STORAGE_PERMISSIONS_RESULT
            || requestCode == CAMERA_PERMISSIONS_RESULT
        ) {
            var partialGranted = true;
            if (grantResults.isNotEmpty()) {
                permissions.forEachIndexed { index, permission ->
                    if (permission.orEmpty() == "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") {
                        if (grantResults[index] != 0) {
                            partialGranted = false;
                        }
                    } else {
                        if (grantResults[index] != 0) {
                            allGranted = false;
                        }
                    }

                }
            } else {
                return;
            }
            when (requestCode) {
                STORAGE_PERMISSIONS_RESULT -> {
                    if (!allGranted)
                        if (!partialGranted) {
                            openSettingsDialog(
                                text = messages.getOrElse(
                                    "dialog_storage_permission_warning",
                                    defaultValue = { storageDefault }),
                                positiveText = positiveText,
                                negativeText = negativeText,
                                neutralText = null,
                                neutralCallback = null,
                            ) {
                                loadPreviews(
                                    filesAccess =
                                    FilesAccess.NONE
                                )
                            }
                        } else {
                            loadPreviews(
                                filesAccess =
                                FilesAccess.PARTIAL
                            )
                        }
                    else
                        loadPreviews(filesAccess = FilesAccess.FULL)
                }

                CAMERA_PERMISSIONS_RESULT -> {
                    if (!allGranted)
                        openSettingsDialog(
                            text = messages.getOrElse(
                                "dialog_video_permissions_warning",
                                defaultValue = { videoDefault }),
                            positiveText = positiveText,
                            negativeText = negativeText,
                            neutralText = null,
                            neutralCallback = null,
                        )
                    else
                        openCameraScreen()
                }
            }
        }
    }

    private fun loadPreviews(filesAccess: FilesAccess) {
        loaded = (filesAccess != FilesAccess.NONE)
        if (filesAccess == FilesAccess.PARTIAL) {
            partialLayout.visibility = View.VISIBLE
        } else {
            partialLayout.visibility = View.GONE
        }
        val galleryFileLimitText =
            messages["warns_file_picker_files_limit"] ?: "You can select up to 10 files"
        val allowMultiple = arguments?.getBoolean("allowMultiple") ?: false
        val filePickerFilesLimit = arguments?.getInt("filePickerFilesLimit") ?: 10
        val filePickerPhotoSizeLimit =
            arguments?.getLong("filePickerImageMaxSizeInBytes") ?: 10000000L
        val filePickerVideoSizeLimit =
            arguments?.getLong("filePickerVideoMaxSizeInBytes") ?: 10000000L
        val filePickerFileDurationLimit =
            arguments?.getLong("filePickerVideoMaxLengthInSeconds") ?: 10
        val fileLimitPhotoSize = messages["title_image_max_size_limit"] ?: "File is too large"
        val fileLimitVideoSize = messages["title_video_max_size_limit"] ?: "File is too large"
        val fileLimitVideoDuration =
            messages["title_video_max_duration_limit"] ?: "File is too large"
        val hasVideo = arguments?.getBoolean("hasVideo") ?: false
        val translations = mapOf(
            "galleryFileLimitText" to galleryFileLimitText,
            "galleryAccessText" to galleryAccessText,
            "fileLimitPhotoSize" to fileLimitPhotoSize,
            "fileLimitVideoSize" to fileLimitVideoSize,
            "fileLimitVideoDuration" to fileLimitVideoDuration
        )
        previews.load(
            filesAccess = filesAccess,
            allowMultipleSelection = allowMultiple,
            mimeTypes = acceptTypes,
            clickCallback = object : FileClickCallback {
                override fun select(file: SelectedFile) {
                    selectedFiles.add(file)
                    //selectedFile = filePath
                    uploadButton.show()
                }

                override fun unselect(file: SelectedFile) {
                    selectedFiles.remove(file)
                    if (selectedFiles.isEmpty())
                        uploadButton.hide()
                }
            },
            cameraCallback = object : OpenCameraClickCallback {
                override fun open() {
                    checkCameraPermissions(hasVideo)
                    //openCameraScreen(isVideo)
                }
            },
            noAccessCallback = object : NoAccessCallback {
                override fun click() {
                    checkStoragePermissions()
                }
            },
            galleryFileMaxCount = filePickerFilesLimit,
            pickerFilter = PickerFilter(
                filePickerPhotoSizeLimit,
                filePickerVideoSizeLimit,
                1000L * filePickerFileDurationLimit
            ),
            translations = translations
        )
    }

    private fun openSettingsDialog(
        text: String,
        positiveText: String? = null,
        negativeText: String? = null,
        neutralText: String? = null,
        neutralCallback: (() -> Unit)? = null,
        negativeCallback: () -> Unit = {},
    ) {
        if (dialogShown) return
        activity?.apply {
            val builder = AlertDialog.Builder(this)
                .setMessage(text)
                .setCancelable(true)
                .setPositiveButton(positiveText ?: "Settings") { dialog, which ->
                    dialog?.dismiss()
                    dialogShown = false
                    openSettingsScreen()
                }
                .setNegativeButton(negativeText ?: "Not now") { dialog, which ->
                    dialog?.dismiss()
                    dialogShown = false
                    negativeCallback.invoke()
                }
            if (neutralCallback != null) {
                builder.setNeutralButton(neutralText ?: "New choice") { dialog, which ->
                    dialog?.dismiss()
                    dialogShown = false
                    neutralCallback.invoke()
                }
            }
            builder.create().show()
            dialogShown = true
        }
    }

    private fun openSettingsScreen() {
        activity?.apply {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

    }

    private fun openCameraScreen() {
        if (parentFragment is FilePickerMainFragment) {
            loaded = false
            (parentFragment as FilePickerMainFragment).openFileCameraScreen(
                Bundle().also {
                    it.putString(
                        "cameraHint",
                        messages["title_camera_button"] ?: "Tap for photo, hold for video"
                    )
                    it.putInt("contentType", arguments?.getInt("contentType", 0) ?: 0)
                }
            )
        }
    }

}