package com.app.signage91.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.app.signage91.*
import com.app.signage91.app.MyApplication
import com.app.signage91.base.BaseActivity
import com.app.signage91.databinding.ActivitySplash2Binding
import com.app.signage91.helpers.addLog
import com.app.signage91.helpers.checkInternetConnection
import com.app.signage91.models.UserInfo
import com.app.signage91.receivers.NetworkConnectionLiveData
import com.app.signage91.utils.Constants
import com.app.signage91.utils.RootUtilities
import com.app.signage91.utils.retrofit.ApiService
import com.app.signage91.utils.room.MyDatabase
import com.google.android.material.snackbar.Snackbar
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.StringWriter


@OptIn(DelicateCoroutinesApi::class)
class SplashActivity2 : BaseActivity() {

    private lateinit var mApiService: ApiService
    private lateinit var mDatabase: MyDatabase
    private lateinit var binding: ActivitySplash2Binding
    private val isAutomatic: Boolean = true

    private var rooted: Boolean = false
    private var rootGivenBool: Boolean = false
    private var busyBoxInstalledBool: Boolean = false
    private var testKeys: Boolean = false
    private var superUser: Boolean = false
    private var executeCommands: Boolean = false

    // list of runtime permissions
    private val neededRuntimePermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.SYSTEM_ALERT_WINDOW
    )

    // for register contract API to request runtime permissions
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("TAG", ": registerForActivityResult: ${it.key}=${it.value}")
                if (!it.value) {
                    // do anything if needed: ex) display about limitation
                    Snackbar.make(binding.root, R.string.permissions_request, Snackbar.LENGTH_SHORT)
                        .show()
                    applicationContext.addLog("Read write permission denied.")
                } else {
                    applicationContext.addLog("Read Write permission granted.")
                    binding.mainLayout.visibility = View.VISIBLE
                    if (UserInfo.registrationStatus) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            startComponentView()
                        }, 1000)
                    } else {
                        if (isAutomatic) {
                            NetworkConnectionLiveData(this.applicationContext)
                                .observe(this, Observer { isConnected ->
                                    if (!isConnected) {
                                        binding.apply {
                                            applicationContext.addLog("Automatic registration process stopped due to no internet.")
                                            progressCircleIndeterminate.hide()
                                            responseTextView.visibility = View.VISIBLE
                                            responseTextView.text =
                                                "Please make sure internet is connected"
                                        }
                                        return@Observer
                                    }
                                    getUsers()
                                    applicationContext.addLog("Automatic registration process started.")

                                })
                        } else {
                            NetworkConnectionLiveData(this.applicationContext)
                                .observe(this, Observer { isConnected ->
                                    if (!isConnected) {
                                        applicationContext.addLog("Internet is not available")
                                        binding.networkTextView.visibility = View.VISIBLE
                                    } else {
                                        binding.networkTextView.visibility = View.GONE
                                    }
                                    applicationContext.addLog("Manual registration process selected.")
                                    binding.progressCircleIndeterminate.visibility = View.GONE
                                    binding.manualLayout.visibility = View.VISIBLE
                                    binding.submitButton.setOnClickListener {
                                        addUser()
                                        applicationContext.addLog("Submit button clicked.")
                                    }
                                })
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationContext.addLog("Started splash activty")

        applicationContext.addLog("OS version = " + (System.getProperty("os.version")?.toString() ?: ""))
        applicationContext.addLog("API Level = " + Build.VERSION.SDK_INT.toString())
        applicationContext.addLog("Device = " + Build.DEVICE.toString())
        applicationContext.addLog("Model = " + Build.MODEL.toString())
        applicationContext.addLog("Product" + Build.PRODUCT.toString())

        binding = ActivitySplash2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mainLayout.visibility = View.INVISIBLE
        binding.manualLayout.visibility = View.GONE

        mApiService = (applicationContext as MyApplication).apiService

        runAdbCommands("adb shell pm grant com.app.signage91 android.permission.SYSTEM_ALERT_WINDOW")
        runAdbCommands("device shell appops set com.joaomgcd.join SYSTEM_ALERT_WINDOW allow")
        runAdbCommands("adb shell appops set com.app.signage91 SYSTEM_ALERT_WINDOW allow")
        checkDrawOverlayPermission()

        mDatabase = (applicationContext as MyApplication).database

        resultLauncher.launch(neededRuntimePermissions)

        binding.rootText.setOnClickListener(View.OnClickListener {
            showMessageBox(rooted)
        })
        getRootData()
    }

    // code to post/handler request for permission
    @SuppressLint("NewApi")
    fun checkDrawOverlayPermission() {
        Log.v("App", "Package Name: " + applicationContext.packageName);
        if (!canDrawOverlays(this)) {
            Log.v("App", "Requesting Permission" + Settings.canDrawOverlays(this));
            var intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + applicationContext.packageName)
            )
            try {
                checkDrawOverlayPermissionResultLauncher.launch(intent)
            } catch (e: Exception) {
                intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                )
                if (intent.resolveActivity(packageManager) != null) {
                    checkDrawOverlayPermissionResultLauncher.launch(intent)
                } else {
                    resultLauncher.launch(neededRuntimePermissions)
                }
            }
        } else {
            Log.v("App", "We already have permission for it.");
            resultLauncher.launch(neededRuntimePermissions)
        }
    }

    private fun runAdbCommands(commandString: String) {
        try {
            val process = Runtime.getRuntime().exec(commandString)
            val bufferedReader = BufferedReader(
                InputStreamReader(process.inputStream)
            )
        } catch (e: Exception) {
            Log.i("_ERROR", e.message.toString())
        }
    }

    private fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Settings.canDrawOverlays(context)
        } else {
            if (Settings.canDrawOverlays(context)) return true
            try {
                val mgr = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    ?: return false
                //getSystemService might return null
                val viewToAdd = View(context)
                val params = WindowManager.LayoutParams(
                    0,
                    0,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT
                )
                viewToAdd.layoutParams = params
                mgr.addView(viewToAdd, params)
                mgr.removeView(viewToAdd)
                return true
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            false
        }
    }

    var checkDrawOverlayPermissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    resultLauncher.launch(neededRuntimePermissions)
                } else {
                    resultLauncher.launch(neededRuntimePermissions)
                }
            } else {
                resultLauncher.launch(neededRuntimePermissions)
            }
        }

    private fun addUser() {
        if (checkInternetConnection(this)) {
            binding.apply {
                progressCircleIndeterminate.show()
            }
            CoroutineScope(Dispatchers.IO).launch {
                val response = mApiService.addUser()
                withContext(Dispatchers.Main) {
                    binding.apply {
                        progressCircleIndeterminate.hide()
                        UserInfo.registrationStatus = true
                        startComponentView()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please make sure internet is connected", Toast.LENGTH_LONG).show()
        }
    }

    private fun startComponentView() {
        applicationContext.addLog("Redirected to component view.")
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(KEY_INTENT.KEY_TYPE, COMPONENT_TYPE.COMPONENT)
        startActivity(intent)
        finish()
    }


    private fun getUsers() {
        applicationContext.addLog("Get registration api call started.")
        binding.apply {
            progressCircleIndeterminate.show()
            responseTextView.visibility = View.GONE
        }
        /*CoroutineScope(Dispatchers.IO).launch {
            val response = mApiService.getUser()
            withContext(Dispatchers.Main) {

            }
        }*/

        binding.apply {
            applicationContext.addLog("Get registration api success.")
            progressCircleIndeterminate.hide()
            responseTextView.visibility = View.VISIBLE
            //responseTextView.text = GsonBuilder().setPrettyPrinting().create().toJson(response)
            responseTextView.text = "123456789" + "\n" + "Waiting for grpc response"

            progressCircleIndeterminate.show()

            applicationContext.addLog("Grpc listening started.")

            Handler(Looper.getMainLooper()).postDelayed({
                applicationContext.addLog("Grpc data received.")
                //Do something after 100ms
                progressCircleIndeterminate.hide()
                UserInfo.registrationStatus = true
                startComponentView()
            }, 10000)

            //getGrpcData(GrpcFragment.GRPC_HOST, GrpcFragment.GRPC_PORT, "This is the test.")
        }
    }

    private fun getGrpcData(host: String, portStr: String, message: String) {
        binding.progressCircleIndeterminate.show()
        binding.responseTextView.visibility = View.GONE
        GlobalScope.launch {
            var reply: Response? = null
            try {
                val port = if (portStr.isEmpty()) 0 else portStr.toInt()
                val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
                //val channel = ManagedChannelBuilder.forTarget(GRPC_BASE_URL).usePlaintext().build()
                val stub = CommunicationServiceGrpc.newBlockingStub(channel)
                val request = Request.newBuilder().apply {
                    mediaPlayerCode = message
                }
                reply = stub.communicate(request.build())
            } catch (e: Exception) {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                e.printStackTrace(pw)
                pw.flush()
                "Call failed: $sw"
                Log.i("__CALL_FAILED", "Call failed: $sw")
            }

            withContext(Dispatchers.Main) {
                binding.progressCircleIndeterminate.hide()
                binding.responseTextView.visibility = View.VISIBLE
                binding.responseTextView.text = reply?.text ?: ""
                //binding.titleTextView.text = GsonBuilder().setPrettyPrinting().create().toJson(reply)

                UserInfo.registrationStatus = true
                startComponentView()
            }
        }
    }

    /* private fun disablePullNotificationTouch() {
        try {
             Log.v("App", "Disable Pull Notification");

             var mView : View =  View (this);
             var statusBarHeight = (ceil (25 * resources.displayMetrics.density)).toInt();
             Log.v("App", "" + statusBarHeight);

             var params : WindowManager.LayoutParams =  WindowManager.LayoutParams(
                 statusBarHeight,
                 WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
             WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, //Disables status bar
             PixelFormat.TRANSPARENT); //Transparent

             params.gravity = Gravity.CENTER ;
            var wm = (getSystemService (WINDOW_SERVICE) as WindowManager).apply {
                addView(mView, params)
            }
         } catch (exp:Exception) {
             Log.v("App", "Exception: " + exp.message);
         }
     }*/

    private fun getRootData() {
        //Coroutine
        GlobalScope.launch(Dispatchers.Default) {
            try {
                rooted = RootUtilities.isRootAvailableOnDevice()
                rootGivenBool = RootUtilities.isRootGivenForDevice()
                busyBoxInstalledBool = RootUtilities.isBusyBoxInstalled()
                testKeys = RootUtilities.isTestKeysAvailable()
                superUser = RootUtilities.isSuperUserAvailable()
                executeCommands = RootUtilities.canExecuteCommands()

                //UI Thread
                withContext(Dispatchers.Main) {
                    if (rooted) {
                        binding.rootText.text = Constants.DEVICE_ROOTED
                        binding.rootText.setTextColor(Color.parseColor("#00E676"))
                        if (!testKeys || !executeCommands || !superUser) {
                            binding.rootText.text =
                                "Sorry! Root access is not properly installed in this device"
                            binding.rootText.setTextColor(Color.parseColor("#FFBA1A1A"))
                        }
                    } else {
                        binding.rootText.text = "Not Rooted"
                        binding.rootText.setTextColor(Color.parseColor("#FFBA1A1A"))
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showMessageBox(isRoot: Boolean) {
        val dialogBuilder =
            AlertDialog.Builder(this, R.style.Widget_AppCompat_ButtonBar_AlertDialog)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.rooted_dialog, null)
        dialogBuilder.setView(dialogView)
        setValues(dialogView, isRoot)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun setValues(dialog: View, isRoot: Boolean) {
        var root_given = dialog.findViewById<TextView>(R.id.Root_Given_DeviceText)
        var device_root_available =
            dialog.findViewById<TextView>(R.id.Device_Root_AvailableOnDevice)
        var busy_box = dialog.findViewById<TextView>(R.id.busy_Box_InstalledOnDevice)
        var test_keys = dialog.findViewById<TextView>(R.id.test_keys_available)
        var superuser = dialog.findViewById<TextView>(R.id.super_user_available)
        var executeCommandsText = dialog.findViewById<TextView>(R.id.execute_commands)
        var device_root = dialog.findViewById<TextView>(R.id.Device_Rooted)
        var root_path = dialog.findViewById<TextView>(R.id.Root_Path_textTV)


        if (isRoot) {
            device_root.text = Constants.YES
            root_path.text = RootUtilities.findBinaryLocationPath()
        } else {
            device_root.text = Constants.NO
            root_given.text = Constants.NO
            device_root_available.text = Constants.NO
            root_path.text = Constants.SYMBOL_HYPHEN
        }
        if (rootGivenBool) {
            root_given?.text = Constants.TRUE
        } else
            root_given?.text = Constants.FALSE

        if (rooted) {
            root_given?.text = Constants.TRUE
            device_root_available.text = Constants.YES
        } else {
            root_given?.text = Constants.FALSE
            device_root_available.text = Constants.YES
        }

        if (busyBoxInstalledBool)
            busy_box.text = Constants.YES
        else
            busy_box.text = Constants.NO

        if (testKeys)
            test_keys.text = Constants.YES
        else
            test_keys.text = Constants.NO

        if (superUser)
            superuser.text = Constants.YES
        else
            superuser.text = Constants.NO

        if (executeCommands)
            executeCommandsText.text = Constants.YES
        else
            executeCommandsText.text = Constants.NO
    }
}