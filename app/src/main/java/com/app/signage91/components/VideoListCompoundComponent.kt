package com.app.signage91.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AbsoluteLayout
import android.widget.LinearLayout
import android.widget.MediaController
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.app.signage91.VideoListSettingsModel
import com.app.signage91.helpers.*
import com.app.signage91.receivers.DataListener
import com.app.signage91.utils.view.ScalableVideoView
import java.io.File


class VideoListCompoundComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    videoListCompoundModel: VideoListSettingsModel
) : ScalableVideoView(context, attrs), DataListener, DefaultLifecycleObserver {

    private var downloadService: DownloadService? = null
    private var stopPosition = 0
    private val listModel = videoListCompoundModel
    private var videoIndex = 0

    init {
        downloadService = DownloadService(context, this)
        setMargins()
        applyStyles()
        setVideosOneByOne()
    }

    private fun setMargins() {
        x = getWidthByPercent(context, listModel.xValue!!.toDouble())
            .toFloat()
        y = getHeightByPercent(context, listModel.yValue!!.toDouble())
            .toFloat()
    }

    private fun setVideosOneByOne() {
        val mediacontroller = MediaController(context)
        mediacontroller.hide()
        mediacontroller.visibility = View.GONE
        setMediaController(mediacontroller)
        setVideoPathFromLocal(listModel.urls[videoIndex].url, listModel.urls[videoIndex].filename)
        requestFocus()

        setOnPreparedListener { mp ->
            mp.start()
            mp.setOnVideoSizeChangedListener { mp, width, height ->
                setMediaController(mediacontroller)
                mediacontroller.setAnchorView(this)
            }
        }
        setOnCompletionListener { mp ->
            if (videoIndex == listModel.urls.size - 1) {
                videoIndex = 0
            } else {
                videoIndex++
            }
            setVideoPathFromLocal(listModel.urls[videoIndex].url, listModel.urls[videoIndex].filename)
            start()
        }
        setOnErrorListener { mp, what, extra ->
            Log.d("API123", "What $what extra $extra")
            mp.release()
            true
        }
        /*setOnErrorListener(MediaPlayer.OnErrorListener { mp, what, extra ->
            when (extra) {
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {}
                MediaPlayer.MEDIA_ERROR_IO -> {}
                MediaPlayer.MEDIA_ERROR_TIMED_OUT -> {}
            }
            true
        })*/
        start()
    }

    private fun setVideoPathFromLocal(url: String, filename: String) {
        if (context.isFileExist("Videos/", filename)) {
            // Load from local
            setVideoURI(Uri.parse(context.getExternalFilesDir("Signage91/Videos/" + filename)?.path!!))
        } else {
            // Download new one
            setVideoURI(Uri.parse(listModel.urls[videoIndex].url))
            downloadService?.let {
                val file =
                    context.createImageFile("Videos/",filename)
                if (file == null) {
                    // D nothing
                } else {
                    it.downloadUrlAndSaveLocal(
                        url,
                        filename,
                        context.getExternalFilesDir("Signage91/Videos/")?.path!!
                    )
                }
            }
        }
    }


    private fun applyStyles() {
        val height: Int = getHeightByPercent(context, listModel.height)
        val width: Int = getWidthByPercent(context, listModel.width)
        val abslayoutParams: AbsoluteLayout.LayoutParams

        if (width != 0 && height != 0) {
            abslayoutParams = AbsoluteLayout.LayoutParams(
                width,
                height,
                getWidthByPercent(
                    context,
                    listModel.xValue!!.toDouble()
                ),
                getHeightByPercent(
                    context,
                    listModel.yValue!!.toDouble()
                ),
            )
        } else if (width == 0 && height != 0) {
            abslayoutParams = AbsoluteLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height,
                getWidthByPercent(
                    context,
                    listModel.xValue!!.toDouble()
                ),
                getHeightByPercent(
                    context,
                    listModel.yValue!!.toDouble()
                ),
            )
        } else if (width != 0 && height == 0) {
            abslayoutParams = AbsoluteLayout.LayoutParams(
                width,
                LinearLayout.LayoutParams.MATCH_PARENT,
                getWidthByPercent(
                    context,
                    listModel.xValue!!.toDouble()
                ),
                getHeightByPercent(
                    context,
                    listModel.yValue!!.toDouble()
                ),
            )
        } else {
            abslayoutParams = AbsoluteLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                getWidthByPercent(
                    context,
                    listModel.xValue!!.toDouble()
                ),
                getHeightByPercent(
                    context,
                    listModel.yValue!!.toDouble()
                ),
            )
        }
        this.layoutParams = abslayoutParams
    }

    override fun onDataRetrieved(data: Any?, whichUrl: String?, fileName: String?) {
    }

    override fun onError(data: Any?, whichUrl: String?, fileName: String?) {
        try {
            val dir = context.getExternalFilesDir("Signage91/" + "Videos/")
            val file = File(dir, fileName)
            if (file.exists()) {
                file.delete();
            }
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.i("__DETACHED_INVISIBLE", currentPosition.toString())
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        if (isPlaying) {
            stopPosition = currentPosition!!
            pause()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (isPlaying) {
            seekTo(stopPosition)
            start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getWidthByPercent(context, listModel.width),
            getHeightByPercent(context, listModel.height)
        )
    }

}