package com.app.signage91.utils.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import com.app.signage91.helpers.createImageFile
import java.io.FileOutputStream


fun Context.takeScreenshot(view: View) {
    Handler(Looper.getMainLooper()).postDelayed({
        try {
            val file = createImageFile(
                "Screenshots/",
                "Screenshot-" + System.currentTimeMillis().toString() + ".jpg"
            )
            if (file?.exists()!!)
                file.delete()
            file.createNewFile()

            //val bitmap: Bitmap? = getBitmapFromView(view)
            val bitmap = Bitmap.createBitmap(
                view.width,
                view.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val outputStream = FileOutputStream(file)
            val quality = 100
            bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }, 3000)
}


fun Context.takeCroppedScreenshot(view: View) {
    Handler(Looper.getMainLooper()).postDelayed({
        try {
            val file = createImageFile(
                "Screenshots/",
                "Screenshot-" + System.currentTimeMillis().toString() + ".jpg"
            )
            if (file?.exists()!!)
                file.delete()
            file.createNewFile()

            //val bitmap: Bitmap? = getBitmapFromView(view)
            val bitmap = Bitmap.createBitmap(
                view.width,
                view.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val resizedBmp: Bitmap = Bitmap.createBitmap(bitmap, 0, 500, 800, 1000)
            val outputStream = FileOutputStream(file)
            val quality = 100
            resizedBmp.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }, 3000)
}


fun Context.takeCroppedScreenshot2(view: View) {
    Handler(Looper.getMainLooper()).postDelayed({
        try {
            val file = createImageFile(
                "Screenshots/",
                "Screenshot-" + System.currentTimeMillis().toString() + ".jpg"
            )
            if (file?.exists()!!)
                file.delete()
            file.createNewFile()

            //val bitmap: Bitmap? = getBitmapFromView(view)
            val bitmap = Bitmap.createBitmap(
                view.width,
                view.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val resizedBmp: Bitmap = Bitmap.createBitmap(bitmap, 0, 800, 400, 500)
            val outputStream = FileOutputStream(file)
            val quality = 100
            resizedBmp.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }, 3000)
}


/*fun Context.takeCroppedScreenshot(view: View) {
    Handler(Looper.getMainLooper()).postDelayed({
        try {
            val file = createImageFile(
                "Screenshots/",
                "Screenshot-" + System.currentTimeMillis().toString() + ".jpg"
            )
            if (file?.exists()!!)
                file.delete()
            file.createNewFile()

            //val bitmap: Bitmap? = getBitmapFromView(view)
            val bitmap = Bitmap.createBitmap(
                view.width,
                view.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)


            val rect = Rect(100, 100, 900, 900)
            //  Be sure that there is at least 1px to slice.
            //assert(rect.left < rect.right && rect.top < rect.bottom)
            val resizedBmp: Bitmap = Bitmap.createBitmap(
                rect.right - rect.left,
                rect.bottom - rect.top,
                Bitmap.Config.ARGB_8888
            )
            Canvas(resizedBmp).drawBitmap(bitmap, -rect.left.toFloat(), -rect.top.toFloat(), null);


            val outputStream = FileOutputStream(file)
            val quality = 100
            resizedBmp.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }, 3000)
}*/


private fun getBitmapFromView(view: View): Bitmap? {
    val returnedBitmap =
        Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(returnedBitmap)
    val bgDrawable = view.background
    if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
    view.draw(canvas)
    return returnedBitmap
}