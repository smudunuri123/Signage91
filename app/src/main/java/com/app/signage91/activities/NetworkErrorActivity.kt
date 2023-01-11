package com.app.signage91.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.app.signage91.R
import com.app.signage91.base.BaseActivity
import com.app.signage91.receivers.ConnectivityReceiver.ConnectivityReceiverListener

class NetworkErrorActivity : BaseActivity(), ConnectivityReceiverListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_error)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        // If internet is connected, go back to screen
        Log.e("InternetConnectivity", "Is network connected $isConnected")
        Toast.makeText(this, "Is network connected $isConnected", Toast.LENGTH_LONG).show()
        if (isConnected){
            finish()
        }
    }
}