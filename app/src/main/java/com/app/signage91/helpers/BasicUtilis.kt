package com.app.signage91.helpers

//import com.efraespada.jsondiff.JSONDiff
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.Switch
import android.widget.TextView
import com.app.signage91.components.ImageViewComponent
import com.app.signage91.utils.extensions.size
import com.app.signage91.utils.extensions.sizeInMb
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fasterxml.jackson.databind.ObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.roundToInt


fun Context.loadFromLocal(imageView: ImageViewComponent, fileName: String) {
    Glide.with(imageView)
        .load(getFile("Images/", fileName))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(imageView)
}

fun Context.getFile(folder: String, fileNameToSave: String): File {
    val dir = getExternalFilesDir("Signage91/" + folder)
    return File(dir, fileNameToSave)
}

@SuppressLint("MissingPermission")
fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return !(((activeNetworkInfo == null) || !activeNetworkInfo.isConnected))
}

fun Context.createImageFile(folder: String, fileName: String): File? {
    try {
        val dir = getExternalFilesDir("Signage91/" + folder)
        if (!dir?.exists()!!) {
            dir.mkdirs()
        }
        val file = File(dir, fileName)
        file.parentFile.mkdirs();
        if (!file.createNewFile()) {
            file.delete();
            file.createNewFile();
        }
        return file
    } catch (exp: Exception) {
        exp.printStackTrace()
        return null
    }
}

fun Context.isFileExist(folder: String, fileName: String): Boolean {
    val dir = getExternalFilesDir("Signage91/" + folder)
    if (!dir?.exists()!!) {
        return false // If directory is not available, means file is not there
    }
    val file = File(dir, fileName)
    return file.exists() && file.size > 0
}

fun getHeightByPercent(context: Context?, heightPercent: Double?): Int {
    var mathHeight: Int = 0
    if (heightPercent!! != 0.0) {
        val windowManager: android.view.WindowManager = context!!
            .getSystemService(WINDOW_SERVICE) as android.view.WindowManager
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val width: Int = size.x
        val screenHeight: Int = size.y

//            val metrics = context!!.resources.displayMetrics
//            val realHeight = metrics.heightPixels
        var height = (((heightPercent.toDouble() * screenHeight)) / 100).toFloat()
        mathHeight = height.roundToInt()
        Log.i("Height", "$height  & total $screenHeight  & Math Height $mathHeight")
    }
    return mathHeight
}

fun getWidthByPercent(context: Context?, widthPercent: Double?): Int {
    var mathWidth: Int = 0
    if (widthPercent!! != 0.0) {
        val windowManager: android.view.WindowManager = context!!
            .getSystemService(WINDOW_SERVICE) as android.view.WindowManager
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val screenWidth: Int = size.x
        val screenHeight: Int = size.y

//            val metrics = context!!.resources.displayMetrics
//            val realHeight = metrics.heightPixels
        var width = (((widthPercent.toDouble() * screenWidth)) / 100).toFloat()
        mathWidth = width.roundToInt()
        Log.i("Width", "$width  & total $screenHeight  & Math width $mathWidth")
    }
    return mathWidth
}

fun pxFromDp(context: Context, dp: Float): Float {
    return dp / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun convertDpToPixel(context: Context, dp: Float): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.addLog(data: String, fileName: String = "logs.txt") {
    if (isFileExist("Logs/", fileName)) {
        val file = getExternalFilesDir("Signage91/Logs/" + fileName)
        val sizeInMb = file?.sizeInMb
        if (file?.exists()!!)
            writeToFile(file, data)
        if (sizeInMb!! >= 1 && file.exists()) {
            val zipFileName = System.currentTimeMillis().toString() + "_logs.zip"
            val zipFile = createImageFile("Logs/", zipFileName)
            val filePaths = arrayListOf<String>().apply {
                add(file.path)
            }
            zip(filePaths, zipFile?.path)
            file.delete()
        }
    } else {
        val file = createImageFile("Logs/", fileName)
        if (file?.exists() == true) {
            writeToFile(file, data)
        }
    }


    if (isFileExist("Logs/", fileName)) {
        val file = getExternalFilesDir("Signage91/Logs/" + fileName)
        if (file?.exists() == true)
            writeToFile(file, data)
    } else {
        val file = createImageFile("Logs/", fileName)
        if (file?.exists() == true) {
            writeToFile(file, data)
        }
    }
}

fun writeToFile(file: File, data: String) {
    try {
        val cal: Calendar = Calendar.getInstance()
        val currentLocalTime: Date = cal.time
        //val date: DateFormat = SimpleDateFormat("dd-MM-yyy HH:mm:ss z")
        val date: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        val localTime: String = date.format(currentLocalTime)

        val buf = BufferedWriter(FileWriter(file, true))
        buf.append("$localTime - $data")
        buf.newLine()
        buf.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}


val BUFFER = 2048
fun zip(_files: ArrayList<String>, zipFileName: String?) {
    try {
        var origin: BufferedInputStream? = null
        val dest = FileOutputStream(zipFileName)
        val out = ZipOutputStream(
            BufferedOutputStream(
                dest
            )
        )
        val data = ByteArray(BUFFER)
        for (i in _files.indices) {
            Log.v("Compress", "Adding: " + _files[i])
            val fi = FileInputStream(_files[i])
            origin = BufferedInputStream(fi, BUFFER)
            val entry = ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1))
            out.putNextEntry(entry)
            var count: Int
            while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                out.write(data, 0, count)
            }
            origin.close()
        }
        out.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun isFileCorrupted(context: Context, folder: String, path: String): Boolean {
    val retriever = MediaMetadataRetriever()
    var hasVideo : String?
    try {
        val dir = context.getExternalFilesDir("Signage91/$folder")
        if (!dir?.exists()!!) {
            return false // If directory is not available, means file is not there
        }
        val file = File(dir, path)
        retriever.setDataSource(Uri.parse(file.absolutePath).toString())
        hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return "yes" == hasVideo
}

fun isValidImage(context: Context, folder: String, path: String) : Boolean{
    val retriever = MediaMetadataRetriever()
    val dir = context.getExternalFilesDir("Signage91/$folder")
    if (!dir?.exists()!!) {
        return false // If directory is not available, means file is not there
    }
    val file = File(dir, path)
    try {
        retriever.setDataSource(file.path)
        val art = retriever.embeddedPicture
        if (art != null) {
            return true
        }
    } catch (e:Exception){
        e.printStackTrace()
    }
    return false
}

fun Context.checkIsJsonSame(oldJson: String, newJson: String){
    val mapper = ObjectMapper()
    val beforeNode = mapper.readTree(oldJson)
    val afterNode = mapper.readTree(newJson)
    val patch = JsonDiff.asJson(beforeNode, afterNode)
    val diffs = patch.toString()
    val diffJsonArray = JSONArray(diffs)
    if (diffJsonArray.length() > 0){
        // Since the length is > 0, differences are there.
       for (i in 0 until diffJsonArray.length()){
            val diffObject = diffJsonArray.getJSONObject(i)
           val opt = diffObject.getString("op")
           val oldJSONObject = JSONObject(oldJson)
           if (diffObject.getString("path").contains("filename") ||
               diffObject.getString("path").contains("urls")){
               // If path has filename or urls, need to delete the files
               // when op is remove & replace
               when(opt){
                   "replace" -> {
                       Log.d("JsonDiff", "replace")
                       if (diffObject.has("value")) {
                          // Get the filename based on the path

                       }
                   }
                   "remove"->{
                       Log.d("JsonDiff", "remove")
                       if (diffObject.has("value")) {
                           // Delete the file directly based on filename
                           if (diffObject.getString("value")
                                   .contains(".jpg")
                               && diffObject.getString("value")
                                   .contains(".jpeg") && diffObject.getString("value")
                                   .contains(".png")
                           ) {
                               deleteFileName("Images/", diffObject.getString("value"))
                           } else {
                               deleteFileName("Videos/", diffObject.getString("value"))
                           }
                       } else {
                           // Delete based on the path

                       }
                   }
                   "add" -> {
                       Log.d("JsonDiff", "add")

                   }
                   "move" -> {
                       Log.d("JsonDiff", "move")

                   }
                   "copy" -> {
                       Log.d("JsonDiff", "copy")

                   }
               }
           }
       }
    }
}

fun Context.deleteFileName(folder: String, filename: String) {
    try {
        val dir = getExternalFilesDir("Signage91/" + folder)
        val file = File(dir, filename)
        if (file.exists()) {
            file.delete();
        }
    } catch (exp: Exception) {
        exp.printStackTrace()
    }
}

