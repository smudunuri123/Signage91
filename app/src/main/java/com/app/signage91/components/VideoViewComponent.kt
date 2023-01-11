package com.app.signage91.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.app.signage91.VideoSettingsModel
import com.app.signage91.helpers.*
import com.app.signage91.receivers.DataListener
import com.app.signage91.utils.view.ScalableVideoView
import java.io.File


class VideoViewComponent @JvmOverloads
constructor(
    context: Context?,
    attrs: AttributeSet?,
    var videoSettingsModel: VideoSettingsModel,
    defStyle: Int = 0,
) : ScalableVideoView(context!!, attrs), DataListener, DefaultLifecycleObserver {

    private var downloadService: DownloadService? = null
    private var stopPosition = 0

    init {
        downloadService = DownloadService(context, this)
        setMargins()
        setVideoPathFromLocal()
    }

    private fun setMargins() {
        x = getWidthByPercent(context, videoSettingsModel.xValue!!.toDouble())
            .toFloat()
        y = getHeightByPercent(context, videoSettingsModel.yValue!!.toDouble())
            .toFloat()
    }

    private fun setVideoPathFromLocal() {
        videoSettingsModel.url.let {
            if (context.isFileExist("Videos/", videoSettingsModel.fileName) && isFileCorrupted(context, "Videos/" , videoSettingsModel.fileName!!)) {
                // Load from local
                initializePlayer(
                    context.getExternalFilesDir("Signage91/Videos/" + videoSettingsModel.fileName)?.path!!
                )
            } else {
                // Download new one
                initializePlayer(videoSettingsModel.url)
                downloadService?.let {
                    val file =
                        context.createImageFile("Videos/", videoSettingsModel.fileName)
                    if (file == null) {
                        // Do nothing
                    } else {
                        it.downloadUrlAndSaveLocal(
                            videoSettingsModel.url,
                            videoSettingsModel.fileName,
                            context.getExternalFilesDir("Signage91/Videos/")?.path!!
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun initializePlayer(path: String) {
        val mediacontroller = MediaController(context)
        mediacontroller.hide()
        mediacontroller.visibility = View.GONE
        setMediaController(mediacontroller)
        setVideoURI(Uri.parse(path))
        requestFocus()

        setOnPreparedListener { mp ->
            mp.setOnVideoSizeChangedListener { mp, width, height ->
                setMediaController(mediacontroller)
                mediacontroller.setAnchorView(this)
            }
        }
        setOnCompletionListener { mp ->
            Toast.makeText(
                context,
                "Video over",
                Toast.LENGTH_SHORT
            ).show()
            mp?.release()
            Toast.makeText(
                context,
                "Videos completed",
                Toast.LENGTH_SHORT
            ).show()
        }
        setOnErrorListener { mp, what, extra ->
            Log.d("API123", "What $what extra $extra")
            mp.release()
            true
        }
        start()
    }

    override fun onDataRetrieved(data: Any?, whichUrl: String?, fileName: String?) {
        val status: String = (data) as String
        /*if (status == "Success") {
            // Load from local
            setVideoPathFromLocal()
            initializePlayer(
                context.getExternalFilesDir("Signage91/Videos/")?.path!!
            )
            setVideoPath(
                "file://" + context.getExternalFilesDir("Signage91/Videos/" + videoSettingsModel.fileName)?.path!!
            )
        } else {
            initializePlayer(whichUrl!!)
        }*/
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
//        initializePlayer(whichUrl!!)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.i("__DETACHED_INVISIBLE", currentPosition.toString())
    }

    /*override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            Log.i("__VISIBLE", stopPosition.toString())

        } else if (visibility == View.INVISIBLE){
            //stopPosition = videoView?.currentPosition!!
            //Log.i("__INVISIBLE", videoView?.currentPosition.toString())
        }
    }*/

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
            getWidthByPercent(context, videoSettingsModel.width),
            getHeightByPercent(context, videoSettingsModel.height)
        )
    }

}