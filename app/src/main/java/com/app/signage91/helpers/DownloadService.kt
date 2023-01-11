package com.app.signage91.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.app.signage91.receivers.DataListener
import com.androidnetworking.error.ANError

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.interfaces.DownloadListener
import com.app.signage91.ComponentFragment
import com.app.signage91.activities.MainActivity
import com.app.signage91.components.ExoPlayerViewComponent
import com.app.signage91.components.ImageViewComponent
import com.app.signage91.components.VideoListCompoundComponent
import com.app.signage91.components.VideoViewComponent
import java.lang.Exception
import java.util.*

class DownloadService {
    var dataListener : DataListener? = null
    var context : Context? = null

    constructor(context: Context?) {
        this.context = context
    }

    constructor(context: Context?, imageViewComponent: ImageViewComponent){
        this.dataListener = imageViewComponent as DataListener
        this.context = context
    }

    constructor(context: Context?, videoViewComponent: VideoViewComponent){
        this.dataListener = videoViewComponent as DataListener
        this.context = context
    }

    constructor(context: Context, exoPlayerViewComponent: ExoPlayerViewComponent){
        this.dataListener = exoPlayerViewComponent as DataListener
        this.context = context
    }

    constructor(context: Context, videoListCompoundComponent: VideoListCompoundComponent){
        this.dataListener = videoListCompoundComponent as DataListener
        this.context = context
    }

    constructor(componentFragment: ComponentFragment, context: Context?){
//        this.dataListener = componentFragment as DataListener
        this.context = context
    }

    fun downloadUrlAndSaveLocal(url: String,  fileName:String, dirName:String){
        AndroidNetworking.download(url, dirName, fileName)
            .setTag("download")
            .setPriority(Priority.HIGH)
            .build()
            .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                // Not showing progress as of now
                if(bytesDownloaded == totalBytes){
                    context!!.addLog("File Download completed $fileName")
                    Log.d("File Download", "Completed $fileName")
//                    Toast.makeText(context, "$fileName Download Completed", Toast.LENGTH_SHORT).show()
//                    dataListener?.let {
//                        it.onDataRetrieved("Success", url, fileName)
//                    }
                }
            }
            .startDownload(object : DownloadListener {
                override fun onDownloadComplete() {
                    // Send the status
                    try {
                        dataListener?.let {
                            it.onDataRetrieved("Success", url, fileName)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        dataListener?.let {
                            it.onError("Error", url, fileName)
                        }
                    }
                }

                override fun onError(error: ANError?) {
                    try {
                        dataListener?.let {
                            it.onError(error?.errorBody.toString(), url, fileName)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        dataListener?.let {
                            it.onError("Error", url, fileName)
                        }
                    }
                }
            })
    }
}