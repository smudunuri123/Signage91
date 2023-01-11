package com.app.signage91.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.signage91.R
import com.app.signage91.databinding.FragmentScreenshotAndRecordingBinding
import com.app.signage91.utils.extensions.takeCroppedScreenshot
import com.app.signage91.utils.extensions.takeScreenshot
import com.app.signage91.utils.service.RecordService
import com.google.android.material.snackbar.Snackbar

class ScreenshotAndRecordingTestFragment : Fragment() {

    private var _binding: FragmentScreenshotAndRecordingBinding? = null
    private val binding get() = _binding!!

    companion object{
        fun newInstance(): ScreenshotAndRecordingTestFragment {
            return ScreenshotAndRecordingTestFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScreenshotAndRecordingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            screenshotButton.setOnClickListener {
                requireContext().takeScreenshot(mainLayout)
            }
            takePartialScreenShot.setOnClickListener {
                requireContext().takeCroppedScreenshot(mainLayout)
            }
            startRecordingButton.setOnClickListener {
                screenRecordingsResultLauncher.launch(neededRuntimePermissionsForScreenRecordings)
                /*Handler(Looper.getMainLooper()).postDelayed({
                    stopScreenRecording()
                }, 10000)*/
            }
            stopRecordingButton.setOnClickListener {
                stopScreenRecording()
            }
        }
    }


    val RECORD_REQUEST_CODE = 101

    private val neededRuntimePermissionsForScreenRecordings = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private val screenRecordingsResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = false
            permissions.entries.forEach {
                Log.d("TAG", ": registerForActivityResult: ${it.key}=${it.value}")
                if (!it.value) {
                    // do anything if needed: ex) display about limitation
                    Snackbar.make(binding.root, R.string.permissions_request, Snackbar.LENGTH_SHORT)
                        .show()
                    permissionGranted = false
                } else {
                    permissionGranted = true
                }
            }
            if (permissionGranted){
                startMediaProjectionRequest()
            }
        }


    private fun stopScreenRecording() {
        /*val intent = Intent(requireContext(), RecordService::class.java).apply {
            action = RecordService.ACTION_STOP
        }
        ContextCompat.startForegroundService(requireContext(), intent)*/

        val intent = Intent(requireContext(), RecordService::class.java)
        requireContext().stopService(intent)
    }

    private fun startMediaProjectionRequest() {
        val projectionManager =
            requireActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(projectionManager.createScreenCaptureIntent(), RECORD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val intent = Intent(requireContext(), RecordService::class.java).apply {
                action = RecordService.ACTION_START
                putExtra(RecordService.EXTRA_RESULT_DATA, data!!)
            }
            ContextCompat.startForegroundService(requireContext(), intent)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}