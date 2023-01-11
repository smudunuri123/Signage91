package com.app.signage91.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.AbsoluteLayout
import android.widget.LinearLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.app.signage91.ExoPlayerSettingsModel
import com.app.signage91.helpers.*
import com.app.signage91.receivers.DataListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import java.io.File


class ExoPlayerViewComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    var exoPlayerSettingsModel: ExoPlayerSettingsModel
) : StyledPlayerView(context, attrs), DataListener {

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var currentItem = 0
    private var playbackPosition = 0L
    private var exoPlayer: ExoPlayer? = null
    private var downloadService: DownloadService? = null
    var mediaItem: MediaItem? = null

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d("TAG", "changed state to $stateString")
        }
    }


    init {
        downloadService = DownloadService(context, this)
        hideSystemUi()
        applyStyles()
        if (Util.SDK_INT >= 23 || player == null) {
            setVideoPathFromLocal()
        }
    }

    private fun applyStyles() {
        this.apply {
            val height: Int = getHeightByPercent(context, exoPlayerSettingsModel.height)
            val width: Int = getWidthByPercent(context, exoPlayerSettingsModel.width)

            exoPlayerSettingsModel.width.let {
                val abslayoutParams: AbsoluteLayout.LayoutParams
                if (width != 0 && height != 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        width,
                        height,
                        getWidthByPercent(context, exoPlayerSettingsModel.xValue!!.toDouble()),
                        getHeightByPercent(context, exoPlayerSettingsModel.yValue!!.toDouble()),
                    )
                } else if (width == 0 && height != 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        height,
                        getWidthByPercent(context, exoPlayerSettingsModel.xValue!!.toDouble()),
                        getHeightByPercent(context, exoPlayerSettingsModel.yValue!!.toDouble()),
                    )
                } else if (width != 0 && height == 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        width,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getWidthByPercent(context, exoPlayerSettingsModel.xValue!!.toDouble()),
                        getHeightByPercent(context, exoPlayerSettingsModel.yValue!!.toDouble()),
                    )
                } else {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getWidthByPercent(context, exoPlayerSettingsModel.xValue!!.toDouble()),
                        getHeightByPercent(context, exoPlayerSettingsModel.yValue!!.toDouble()),
                    )
                }
                this.layoutParams = abslayoutParams
            }
        }
    }

    private fun setVideoPathFromLocal() {
        exoPlayerSettingsModel.url.let {
            if (context.isFileExist("Videos/", exoPlayerSettingsModel.fileName!!)!!) {
                // Load from local
                initializePlayer(
                    context.getExternalFilesDir("Signage91/Videos/" + exoPlayerSettingsModel.fileName)?.path!!
                )
            } else {
                // Download new one
                downloadService?.let {
                    val file = context.createImageFile("Videos/", exoPlayerSettingsModel.fileName!!)
                    if (file == null) {
                        initializePlayer(exoPlayerSettingsModel.url!!)
                    } else {
                        it.downloadUrlAndSaveLocal(
                            exoPlayerSettingsModel.url,
                            exoPlayerSettingsModel.fileName,
                            context.getExternalFilesDir("Signage91/Videos/")?.path!!
                        )
                    }
                }
            }
        }

    }

    fun initializePlayer(path: String) {
        setShowSubtitleButton(false)
        useController = false

        /*val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                player = exoPlayer
                mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(path))
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"))
                    //.setUri(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"))
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .setMimeType(MimeTypes.APPLICATION_MP4)
                    .build()

                exoPlayer.setMediaItem(mediaItem!!)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()
            }
                player?.apply {
                    setMediaItem(mediaItem)
                    playWhenReady = true
                    seekTo(currentItem, playbackPosition)
                    addListener(playbackStateListener)
                    prepare()
                }
            }*/


        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer?.playWhenReady = false

        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaItem =
            MediaItem.fromUri(path)
        val mediaSource =
            HlsMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(mediaItem)
        exoPlayer?.apply {
            setMediaSource(mediaSource)
            seekTo(playbackPosition)
            playWhenReady = true
            prepare()
            addListener(playbackStateListener)
            //resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            resizeMode = exoPlayerSettingsModel.aspectRatio
        }


        player = exoPlayer

    }


    @SuppressLint("InlinedApi")
    fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows((context as Activity).window, false)
        WindowInsetsControllerCompat((context as Activity).window, this).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /*override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(exoPlayerSettingsModel.width, exoPlayerSettingsModel.height)
    }*/

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            player.stop()
            playbackPosition = player.currentPosition
            player.release()
            exoPlayer = null
        }
    }

    override fun onDataRetrieved(data: Any?, whichUrl: String?, fileName: String?) {
        val status: String = (data) as String
        if (status == "Success") {
            // Load from local
            setVideoPathFromLocal()
            initializePlayer(context.getExternalFilesDir("Signage91/Videos/" + exoPlayerSettingsModel.fileName)?.path!!)
        } else {
            initializePlayer(whichUrl!!)
        }
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
        initializePlayer(whichUrl!!)
    }
}