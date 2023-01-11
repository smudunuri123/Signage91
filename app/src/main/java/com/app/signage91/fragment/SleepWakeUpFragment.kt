package com.app.signage91.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.signage91.databinding.FragmentSleepWakeUpBinding
import com.app.signage91.receivers.ScreenOffAdminReceiver
import com.app.signage91.receivers.SystemSleepWakeUpReceiver
import com.app.signage91.utils.Constants
import com.app.signage91.utils.RandomIntUtil
import com.app.signage91.utils.extensions.installApk
import com.app.signage91.utils.extensions.startSleepWakeUpWorker
import com.app.signage91.utils.extensions.turnOffScreen
import com.app.signage91.utils.extensions.turnOnScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SleepWakeUpFragment : Fragment() {

    private var _binding: FragmentSleepWakeUpBinding? = null
    private val binding get() = _binding!!
    private var alarmManager: AlarmManager? = null

    var dialog: AlertDialog? = null

    companion object {
        private const val REQUEST_CODE = 1
        private const val DRAW_OVER_OTHER_APP_PERMISSION = 2
        fun newInstance(): SleepWakeUpFragment {
            return SleepWakeUpFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepWakeUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //runAsSystemAdmin()
        //askForSystemOverlayPermission()

        binding.apply {
            turnOffScreenButton.setOnClickListener {
                requireActivity().turnOffScreen()
            }

            turnOnScreenButton.setOnClickListener {
                requireActivity().turnOnScreen()
            }

            turnOffScreenAfterSomeTimeButton.setOnClickListener {
                requireActivity().startSleepWakeUpWorker()
            }

            installApk.setOnClickListener {
                /*showProgressDialog()
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        dismissProgressDialog()
                    }
                }*/
                requireActivity().installApk()
            }
        }

        /*Handler(Looper.getMainLooper()).postDelayed({
            requireActivity().turnOnScreen()
        }, 30000)*/


        //startAlarmManager(System.currentTimeMillis() + (5 * 1000), Constants.ALARM_MANAGER.ACTION_SLEEP)
        //startAlarmManager(System.currentTimeMillis() + (10 * 1000), Constants.ALARM_MANAGER.ACTION_WAKE_UP)
    }


    private fun showProgressDialog() {
        val llPadding = 30
        val ll = LinearLayout(requireContext())
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam
        val progressBar = ProgressBar(requireContext())
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(requireContext())
        tvText.text = "Loading ..."
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setView(ll)
        dialog = builder.create()
        dialog?.show()
        val window: Window? = dialog?.window
        if (window != null) {
            val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog?.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog?.window?.attributes = layoutParams
        }
    }

    private fun dismissProgressDialog(){
        if (dialog != null && dialog?.isShowing!!){
            dialog?.dismiss()
            dialog = null
        }
    }

    private fun startAlarmManager(timeInMillis: Long, actionType: String) {
        val flags = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }
        val intent = Intent(context, SystemSleepWakeUpReceiver::class.java)
        intent.action = actionType
        intent.putExtra(Constants.ALARM_MANAGER.KEY_TIME_IN_MILLIS, timeInMillis)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            RandomIntUtil.getRandomInt(),
            intent,
            flags
        )
        alarmManager?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager?.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun runAsSystemAdmin() {
        val policyManager = requireActivity()
            .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminReceiver = ComponentName(
            requireActivity(),
            ScreenOffAdminReceiver::class.java
        )
        val admin = policyManager.isAdminActive(adminReceiver)

        if (!admin) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                adminReceiver
            )
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "ANY EXTRA DESCRIPTION")
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private fun askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                requireContext()
            )
        ) {
            //If the draw over permission is not available to open the settings screen
            //to grant the permission.
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + requireActivity().packageName)
            )
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION)
        }
    }
}