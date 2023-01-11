package com.app.signage91.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.signage91.COMPONENT_TYPE
import com.app.signage91.KEY_INTENT
import com.app.signage91.base.BaseActivity
import com.app.signage91.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirmButton.setOnClickListener {
            val selectedId: Int = binding.radioGroup.checkedRadioButtonId
            val intent = Intent(this, MainActivity::class.java)
            if (selectedId == binding.androidDefaultVideoViewButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.ANDROID_DEFAULT)
            } else if (selectedId == binding.exoPlayerButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.EXO_PLAYER)
            } else if (selectedId == binding.textViewButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.TEXT_VIEW)
            } else if (selectedId == binding.rssFeedButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.RSS_FEED)
            } else if (selectedId == binding.rssFeedCompoundButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.RSS_FEED_COMPOUND)
            } else if (selectedId == binding.restApiCompoundButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.REST_API)
            } else if (selectedId == binding.gRPCCompoundButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.GRPC)
            } else if (selectedId == binding.orientationCompoundButton.id){
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.ORIENTATION)
            }else if (selectedId == binding.adbCommandsCompoundButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.ADB_COMMANDS)
            } else if (selectedId == binding.screenShotAndRecordingButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.SCREENSHOT_AND_RECORDING)
            } else if (selectedId == binding.sleepAndWakeUpButton.id) {
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.SLEEP_AND_WAKE_UP)
            } else{
                intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.COMPONENT)
            }
            startActivity(intent)
        }
    }
}