package com.app.signage91.utils.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.app.signage91.R
import java.io.File
import java.io.IOException


class RecordService : Service() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

    private var mediaRecorder: MediaRecorder? = null
    private var mVirtualDisplay: VirtualDisplay? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaProjectionManager =
            applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private fun createNotificationChannel() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(SERVICE_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (intent != null) {
            when (intent.action) {
                ACTION_START -> {
                    mediaProjection = mediaProjectionManager.getMediaProjection(
                        Activity.RESULT_OK,
                        intent.getParcelableExtra(EXTRA_RESULT_DATA)!!
                    ) as MediaProjection
                    startVideoCapture()
                    START_STICKY
                }
                ACTION_STOP -> {
                    stopVideoCapture()
                    START_NOT_STICKY
                }
                else -> throw IllegalArgumentException("Unexpected action received: ${intent.action}")
            }
        } else {
            START_NOT_STICKY
        }
    }

    private fun startVideoCapture() {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }

        val metrics = DisplayMetrics()
        val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealMetrics(metrics)

        val mScreenDensity = metrics.densityDpi
        val displayWidth = metrics.widthPixels
        val displayHeight = metrics.heightPixels

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC) // audio source from microphone
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setVideoEncodingBitRate(8 * 1000 * 1000)
        mediaRecorder?.setVideoFrameRate(15)

        mediaRecorder?.setVideoSize(displayWidth, displayHeight)

        val videoDir: File? = getExternalFilesDir("Signage91/ScreenRecordings/")
        if (!videoDir?.exists()!!) {
            videoDir.mkdirs()
        }
        val timestamp = System.currentTimeMillis()

        var orientation = "portrait"
        if (displayWidth > displayHeight) {
            orientation = "landscape"
        }
        val filePathAndName = timestamp.toString() + "_mode_" + orientation + ".mp4"
        val file = File(videoDir, filePathAndName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaRecorder?.setOutputFile(file)
        } else {
            mediaRecorder?.setOutputFile(file.absolutePath)
        }

        try {
            mediaRecorder?.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val surface: Surface? = mediaRecorder?.surface
        mVirtualDisplay = mediaProjection?.createVirtualDisplay(
            "RecordService",
            displayWidth,
            displayHeight,
            mScreenDensity,
            VIRTUAL_DISPLAY_FLAG_PRESENTATION,
            surface,
            null,
            null
        )
        mediaRecorder?.start()

        Log.d("RecordService", "Started recording")

    }

    private fun stopVideoCapture() {
        mediaRecorder?.stop()
        mediaProjection?.stop()
        mediaRecorder?.release()
        mVirtualDisplay?.release()
        Toast.makeText(this, "Stopped and saved", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVideoCapture()
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? = null


    companion object {
        private const val SERVICE_ID = 123
        private const val NOTIFICATION_CHANNEL_ID = "Screen Capture channel"
        const val ACTION_START = "RecordService:Start"
        const val ACTION_STOP = "RecordService:Stop"
        const val EXTRA_RESULT_DATA = "RecordService:Extra:ResultData"
    }


}