package com.app.signage91.utils.view

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView


open class ScalableVideoView : VideoView {
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var displayMode = DisplayMode.ORIGINAL

    enum class DisplayMode {
        ORIGINAL,  // original aspect ratio
        FULL_SCREEN,  // fit to screen
        ZOOM // zoom in
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        mVideoWidth = 0
        mVideoHeight = 0
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = getDefaultSize(0, widthMeasureSpec)
        var height = getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (displayMode == DisplayMode.ORIGINAL) {
            if (mVideoWidth > 0 && mVideoHeight > 0) {
                if (mVideoWidth * height > width * mVideoHeight) {
                    // video height exceeds screen, shrink it
                    height = width * mVideoHeight / mVideoWidth
                } else if (mVideoWidth * height < width * mVideoHeight) {
                    // video width exceeds screen, shrink it
                    width = height * mVideoWidth / mVideoHeight
                } else {
                    // aspect ratio is correct
                }
            }
        } else if (displayMode == DisplayMode.FULL_SCREEN) {
            // just use the default screen width and screen height
        } else if (displayMode == DisplayMode.ZOOM) {
            // zoom video
            if (mVideoWidth > 0 && mVideoHeight > 0 && mVideoWidth < width) {
                height = mVideoHeight * width / mVideoWidth
            }
        }
        setMeasuredDimension(width, height)
    }

    fun changeVideoSize(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height

        // not sure whether it is useful or not but safe to do so
        holder.setFixedSize(width, height)
        requestLayout()
        invalidate() // very important, so that onMeasure will be triggered
    }

    fun setDisplayMode(mode: DisplayMode) {
        displayMode = mode
    }
}