package com.app.signage91.activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.work.*
import com.app.signage91.COMPONENT_TYPE
import com.app.signage91.ComponentFragment
import com.app.signage91.KEY_INTENT
import com.app.signage91.R
import com.app.signage91.base.BaseActivity
import com.app.signage91.databinding.ActivityMainBinding
import com.app.signage91.exoplayer.ExoPlayerFragment
import com.app.signage91.fragment.*
import com.app.signage91.receivers.ConnectivityReceiver
import com.app.signage91.receivers.WorkerClass
import com.app.signage91.utils.extensions.addAppRestartAlarm
import com.app.signage91.utils.extensions.stopRestartAppAlarm
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity() {
    private var currentApiVersion: Int = 0
    private var videoPlayerType = COMPONENT_TYPE.COMPONENT

    companion object {
        var isResume = true
        private const val REQUEST_CODE = 1
        private const val DRAW_OVER_OTHER_APP_PERMISSION = 2
    }


    var arrayList: ArrayList<String> = ArrayList(
        Arrays.asList(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        )
    )
    private lateinit var binding: ActivityMainBinding
    val request = OneTimeWorkRequestBuilder<WorkerClass>().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentApiVersion = android.os.Build.VERSION.SDK_INT
        fullScreenView()
        if (intent.hasExtra(KEY_INTENT.KEY_TYPE)) {
            videoPlayerType =
                (intent.getSerializableExtra(KEY_INTENT.KEY_TYPE) as COMPONENT_TYPE?)!!
        }

        if (savedInstanceState == null) {
            if (videoPlayerType == COMPONENT_TYPE.EXO_PLAYER) {
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.container,
                        ExoPlayerFragment.newInstance("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8")
                    ).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.ANDROID_DEFAULT) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, VideoPlayerFragment.newInstance(arrayList[0])).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.TEXT_VIEW) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, TestTextViewFragment.newInstance()).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.RSS_FEED) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, RssFeedFragment.newInstance()).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.RSS_FEED_COMPOUND) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, RssFeedComponentFragment.newInstance()).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.REST_API) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, RESTApiFragment.newInstance()).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.GRPC) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, GrpcFragment.newInstance()).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.ORIENTATION) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, OrientationFragment.newInstance()).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.ADB_COMMANDS) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, ADBCommandsFragment.newInstance()).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.SCREENSHOT_AND_RECORDING) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, ScreenshotAndRecordingTestFragment.newInstance()).commit()
            } else if (videoPlayerType == COMPONENT_TYPE.SLEEP_AND_WAKE_UP) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, SleepWakeUpFragment.newInstance()).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, ComponentFragment.newInstance()).commit()
            }
            //takeCroppedScreenshot(binding.mainLayout)
//            supportFragmentManager.beginTransaction()
//                .add(R.id.container, VideoPlayerFragment.newInstance(arrayList[0])).commit()

//            supportFragmentManager.beginTransaction()
//                .add(R.id.container, ExoPlayerFragment.newInstance("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8")).commit()
        }

        //stopRestartAppAlarm()
        addAppRestartAlarm()
        //setAlarm()

        binding.openAppInfoButton.setOnClickListener {
            //throw RuntimeException("This is a crash")
            openAppInfoSettingsPage()
        }
    }

    private fun openAppInfoSettingsPage() {
        try {
            /*val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.data = Uri.parse("package:$packageName");
            startActivity(intent);*/

            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivity(intent);
        }
    }

    @SuppressLint("ServiceCast")
    private fun setAlarm() {
        val mScreenStateReceiver = ConnectivityReceiver()
        val screenStateFilter = IntentFilter()
        screenStateFilter.addAction("alarm.running")
        registerReceiver(mScreenStateReceiver, screenStateFilter)
        var alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ConnectivityReceiver::class.java)
        intent.action = "alarm.running"
        var alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance()
        alarmMgr.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            60 * 1000,
            alarmIntent
        )

        /*val intent = Intent(this, SplashActivity2::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            MyApplication.getInstance().baseContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )*/

    }

    private fun workerMethods() {
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(request.id)
            .observe(this, androidx.lifecycle.Observer {
                val status = it.state.name
                Log.d("MainActivity", "Worker Status $status")
            })

        val work = createWorkRequest(Data.EMPTY)
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("Sleep work", ExistingPeriodicWorkPolicy.REPLACE, work)
    }

    fun createWorkRequest(data: Data) =
        PeriodicWorkRequestBuilder<WorkerClass>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

    private fun fullScreenView() {
        val flags: Int = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // This work only for android 4.4+

        // This work only for android 4.4+
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = flags

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            val decorView: View = window.decorView
            decorView
                .setOnSystemUiVisibilityChangeListener { visibility ->
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN === 0) {
                        decorView.setSystemUiVisibility(flags)
                    }
                }
        }
    }

    @SuppressLint("NewApi")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onResume() {
        super.onResume()
        isResume = true
        fullScreenResumecall()
        //WorkManager.getInstance(this).cancelAllWork()
    }

    override fun onPause() {
        super.onPause()
        isResume = false
        /*workerMethods()
        WorkManager.getInstance(this).enqueue(request)*/
    }

    private fun fullScreenResumecall() {
        if (Build.VERSION.SDK_INT in 12..18) { // lower api
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val decorView = window.decorView
            val uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView.systemUiVisibility = uiOptions
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        showApopupDialog()
    }

    private fun showApopupDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.edit_text_dialog)
        val window: Window? = dialog.getWindow()
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val editText = dialog.findViewById(R.id.edit_txt) as EditText
        val yesBtn = dialog.findViewById(R.id.btn_submit) as Button
        val noBtn = dialog.findViewById(R.id.btn_cancel) as Button
        yesBtn.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            showOptionDialog()
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showOptionDialog() {
        val dialogOption = Dialog(this)
        dialogOption.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogOption.setCancelable(false)
        dialogOption.setContentView(R.layout.option_dialog)
        val window: Window? = dialogOption.getWindow()
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val homeBtn = dialogOption.findViewById(R.id.btn_home) as Button
        val settingBtn = dialogOption.findViewById(R.id.btn_settings) as Button
        val noBtn = dialogOption.findViewById(R.id.btn_cancel) as Button
        homeBtn.setOnClickListener {
            dialogOption.dismiss()
            dialogOption.cancel()
            val i = Intent()
            i.action = Intent.ACTION_MAIN
            i.addCategory(Intent.CATEGORY_HOME)
            this.startActivity(i)

            stopRestartAppAlarm()
        }
        settingBtn.setOnClickListener {
            dialogOption.dismiss()
            startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0);

            stopRestartAppAlarm()
        }
        noBtn.setOnClickListener { dialogOption.dismiss() }
        dialogOption.show()
    }


}