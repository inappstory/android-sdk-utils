package com.inappstory.utils.iasfilepicker.file

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.inappstory.utils.iasfilepicker.R

class AccessBottomSheetDialog : FrameLayout {
    private lateinit var cancelButton: TextView
    private lateinit var settingsButton: TextView
    private lateinit var newChoiceButton: TextView
    private lateinit var bottomSheetBackground: View
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    private fun inflateLayout(context: Context) {
        inflate(context, R.layout.cs_bottom_sheet_dialog, this);
        cancelButton = findViewById(R.id.cancel)
        newChoiceButton = findViewById(R.id.newChoice)
        settingsButton = findViewById(R.id.settings)
        bottomSheetBackground = findViewById(R.id.bsDimLayout)
        val bsLayout = findViewById<FrameLayout>(R.id.bsLayout)
        val lp: CoordinatorLayout.LayoutParams =
            bsLayout.layoutParams as CoordinatorLayout.LayoutParams
        lp.gravity = Gravity.CENTER_HORIZONTAL
        lp.behavior = BottomSheetBehavior<LinearLayout>()
        bsLayout.requestLayout()
        bottomSheetBehavior = BottomSheetBehavior.from(bsLayout);
        bottomSheetBackground.alpha = 0f
        bottomSheetBackground.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        bottomSheetBackground.isClickable = false
        bottomSheetBehavior.addBottomSheetCallback(
            object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    bottomSheetBackground.isClickable =
                        !(newState == BottomSheetBehavior.STATE_COLLAPSED ||
                                newState == BottomSheetBehavior.STATE_HIDDEN)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset in 0f..1f)
                        bottomSheetBackground.alpha = slideOffset
                }
            }
        )
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun show() {
        visibility = VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun initButtons(
        newChoiceText: String,
        settingsText: String,
        cancelText: String,
        newChoiceClick: () -> Unit,
        settingsClick: () -> Unit
    ) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        cancelButton.apply {
            text = cancelText
            setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        settingsButton.apply {
            text = settingsText
            setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                settingsClick.invoke()
            }
        }
        newChoiceButton.apply {
            text = newChoiceText
            setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                newChoiceClick.invoke()
            }
        }
    }

    constructor(context: Context) : super(context) {
        inflateLayout(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(
        context,
        attrs
    ) {
        inflateLayout(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        inflateLayout(context)
    }
}