package com.inappstory.utils.iasfilepicker.file

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.inappstory.utils.iasfilepicker.FilePickerMainFragment
import com.inappstory.utils.iasfilepicker.R
import com.inappstory.utils.iasfilepicker.utils.BackPressedFragment
import com.inappstory.utils.iasfilepicker.utils.faststart.FastStart
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


internal class FilePickerFragment : BackPressedFragment() {
    override fun onBackPressed(): Boolean {
        if (accessDialog.handleBackPress()) return true
        return super.onBackPressed()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickSingleVisualMediaCallback =
            registerForActivityResult(
                ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                // Callback is invoked after the user selects media items or closes the
                // photo picker.
                selectedFiles.clear()
                uri?.let {
                    val mimeType = it.getMimeType(requireContext())
                    val file = writeToTempFile(requireContext(), it, mimeType)
                    selectedFiles.add(
                        SelectedFile(
                            file.absolutePath,
                            mimeType
                        )
                    )
                    if (parentFragment is FilePickerMainFragment && selectedFiles.isNotEmpty()) {
                        (parentFragment as FilePickerMainFragment).sendResult(convertFiles().toTypedArray())
                    }
                }
            }
        pickMultipleVisualMediaCallback =
            registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(10)
            ) { uris ->
                // Callback is invoked after the user selects media items or closes the
                // photo picker.
                selectedFiles.clear()
                if (uris.isNotEmpty()) {
                    uris.forEach {
                        val mimeType = it.getMimeType(requireContext())
                        val file = writeToTempFile(requireContext(), it, mimeType)
                        selectedFiles.add(
                            SelectedFile(
                                file.absolutePath,
                                mimeType
                            )
                        )


                    }
                    if (parentFragment is FilePickerMainFragment && selectedFiles.isNotEmpty()) {
                        (parentFragment as FilePickerMainFragment).sendResult(convertFiles().toTypedArray())
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected")

                }
            }
    }

    private fun Uri.getMimeType(context: Context): String = context.contentResolver.getType(this)
        ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
        ?: "tmp"

    private fun writeToTempFile(context: Context, uri: Uri, mimeType: String): File {
        val dir = File(context.cacheDir.absolutePath + File.separator + "ias_fp_temp")
        dir.mkdirs()
        val tempFile = File.createTempFile("ias_fp_temp_file", ".$mimeType", dir)
        context.contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        }
        return tempFile
    }


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
    private lateinit var accessDialog: AccessBottomSheetDialog

    var acceptTypes = arrayListOf<String>()
    val selectedFiles = arrayListOf<SelectedFile>()


    private val STORAGE_PERMISSIONS_RESULT = 888
    private val CAMERA_PERMISSIONS_RESULT = 890

    private fun checkStoragePermissions() {
        activity?.apply {
            if (!loaded || previews.adapter?.itemCount == 0)
                loadPreviews(FilesAccess.FULL)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadButton = view.findViewById(R.id.upload)
        previews = view.findViewById(R.id.previews)
        partialLayout = view.findViewById(R.id.partialAccessLayout)
        manageButton = view.findViewById(R.id.manageAccess)
        manageHint = view.findViewById(R.id.partialAccessHint)
        accessDialog = view.findViewById(R.id.accessDialog)
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
        accessDialog.initButtons(
            newChoiceText = messages.getOrElse(
                "android_gallery_permission_select_other_files_button",
                defaultValue = { newChoice }),
            settingsText = messages.getOrElse(
                "android_gallery_permission_open_settings_button",
                defaultValue = { openSettings }),
            cancelText = messages.getOrElse(
                "dialog_button_not_now",
                defaultValue = { cancelText }),
            newChoiceClick = {

            },
            settingsClick = {
                openSettingsScreen()
            }
        )
        manageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                accessDialog.show()
            }
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
    private val cancelText = "Cancel"
    private val manageHintText = "You did not allow the app to access the entire gallery"

    fun requestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        checkCameraPermissions()
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
            filePickerClickCallback = object : OpenFilePickerClickCallback {
                override fun open() {
                    openGooglePhotoPicker(allowMultiple)
                }
            },
            cameraCallback = object : OpenCameraClickCallback {
                override fun open() {
                    checkCameraPermissions(hasVideo)
                }
            }
        )
    }


    private lateinit var pickSingleVisualMediaCallback: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var pickMultipleVisualMediaCallback: ActivityResultLauncher<PickVisualMediaRequest>

    private fun openGooglePhotoPicker(allowMultiple: Boolean) {
        var allowVideo = false
        var allowPhoto = false
        acceptTypes.forEach {
            if (it.contains("video")) allowVideo = true
            if (it.contains("image")) allowPhoto = true
        }
        val allowAll = allowPhoto && allowVideo

        val visualMediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType =
            if (allowAll) {
                ActivityResultContracts.PickVisualMedia.ImageAndVideo
            } else if (allowPhoto) {
                ActivityResultContracts.PickVisualMedia.ImageOnly
            } else if (allowVideo) {
                ActivityResultContracts.PickVisualMedia.VideoOnly
            } else {
                return
            }
        if (allowMultiple) {
            pickMultipleVisualMediaCallback.launch(PickVisualMediaRequest(visualMediaType))
        } else {
            pickSingleVisualMediaCallback.launch(PickVisualMediaRequest(visualMediaType))
        }
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