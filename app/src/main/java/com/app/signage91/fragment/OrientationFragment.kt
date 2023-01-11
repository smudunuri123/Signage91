package com.app.signage91.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.AssetManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.app.signage91.BuildConfig
import com.app.signage91.databinding.FragmentOrientationBinding
import com.app.signage91.helpers.createImageFile
import com.app.signage91.models.UserInfo
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class OrientationFragment : Fragment() {
    private var _binding: FragmentOrientationBinding? = null
    private val binding get() = _binding

    companion object{
        fun newInstance() : OrientationFragment{
            val fragment = OrientationFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrientationBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding?.apply {
            portraitButton.setOnClickListener {
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                UserInfo.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            landscapeButton.setOnClickListener {
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                UserInfo.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            automaticButton.setOnClickListener {
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                UserInfo.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            installApkButton.setOnClickListener {
                requireActivity().installApk()
            }
        }
    }


    private fun Context.installApk(){
        val apkName = "myapk.apk"

        val assetManager: AssetManager = requireActivity().assets

        var `in`: InputStream? = null
        var out: OutputStream? = null

        try {
            val file = createImageFile(
                "Apks/",
                apkName
            )
            if (file?.exists()!!)
                file.delete()
            file.createNewFile()


            `in` = assetManager.open(apkName)
            out = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null
            out.flush()
            out.close()
            out = null


            /*val intent = Intent(Intent.ACTION_VIEW)
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            intent.setDataAndType(
                Uri.fromFile(file),
                "application/vnd.android.package-archive"
            )
            startActivity(intent)*/

            var fileUri = Uri.fromFile(file)
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(applicationContext,
                    BuildConfig.APPLICATION_ID + ".provider", file);
            }
            val intent = Intent(Intent.ACTION_VIEW, fileUri)
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            intent.setDataAndType(fileUri, "application/vnd.android" + ".package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
            requireActivity().finish()


        } catch (e: Exception) {
            Log.i("__ERROR", e.message.toString())
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }


}