package com.app.signage91.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.AbsoluteLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.app.signage91.R
import com.app.signage91.helpers.*
import com.app.signage91.models.ImageViewModel
import com.app.signage91.receivers.DataListener
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@SuppressLint("AppCompatCustomView")
class ImageViewComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, imageViewModel: ImageViewModel
) : ImageView(context, attrs), DataListener {

    private var borderPaint: Paint? = null
    private var imageModel: ImageViewModel = imageViewModel
    private var downloadService: DownloadService? = null

    init {
        downloadService = DownloadService(context, this)
        applyStyles()
        setImage()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setImage() {
        if (imageModel.url!!.isEmpty()){
            setImageDrawable(context.getDrawable(R.drawable.signage91))
        } else {
            imageModel.url?.let {
                // Check if file exists or not
                if (context.isFileExist(
                        "Images/",
                        imageModel.fileName!!
                    )) {
                    // Load from local
                    context.loadFromLocal(this, imageModel.fileName!!)
                } else {
                    // Download new one
                        loadImageUsingGlide(imageModel.url!!)
                    downloadService?.let {
                        val file = context.createImageFile("Images/", imageModel.fileName!!)
                        if (file == null) {
                            // Do nothing
                        } else {
                            it.downloadUrlAndSaveLocal(
                                imageModel.url!!,
                                imageModel.fileName!!,
                                context.getExternalFilesDir("Signage91/Images/")?.path!!
                            )
                        }
                    }
                }
            }
        }
    }

    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = context.createImageFile("Images/", fileNameToSave)
            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    fun applyStyles() {
        this.apply {
            borderPaint = Paint()
            borderPaint!!.style = Paint.Style.STROKE
            borderPaint!!.isAntiAlias = true

            // By default style is Full Screen
            when {
                imageModel.scaleType.toString().lowercase() == FIT_XY -> {
                    this.scaleType = ScaleType.FIT_XY;
                }
                imageModel.scaleType.toString().lowercase() == CENTER_CROP -> {
                    this.scaleType = ScaleType.CENTER_CROP
                }
                imageModel.scaleType.toString().lowercase() == CENTER -> {
                    this.scaleType = ScaleType.FIT_CENTER
                }
            }

            val height: Int = getHeightByPercent(context, imageModel.height)
            val width: Int = getWidthByPercent(context, imageModel.width)

            imageModel.width.let {
                if (imageModel.xValue == -1.0 && imageModel.yValue == -1.0) {
                    if (width == 0 && height == 0) {
                        this.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                    } else {
                        this.layoutParams = LinearLayout.LayoutParams(
                            width,
                            height
                        )
                    }
                } else {
                    val abslayoutParams: AbsoluteLayout.LayoutParams
                    if (width != 0 && height != 0) {
                        abslayoutParams = AbsoluteLayout.LayoutParams(
                            width,
                            height,
                            getWidthByPercent(
                                context,
                                imageModel.xValue!!.toDouble()
                            ),
                            getHeightByPercent(
                                context,
                                imageModel.yValue!!.toDouble()
                            ),
                        )
                    } else if (width == 0 && height != 0) {
                        abslayoutParams = AbsoluteLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            height,
                            getWidthByPercent(
                                context,
                                imageModel.xValue!!.toDouble()
                            ),
                            getHeightByPercent(
                                context,
                                imageModel.yValue!!.toDouble()
                            ),
                        )
                    } else if (width != 0 && height == 0) {
                        abslayoutParams = AbsoluteLayout.LayoutParams(
                            width,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getWidthByPercent(
                                context,
                                imageModel.xValue!!.toDouble()
                            ),
                            getHeightByPercent(
                                context,
                                imageModel.yValue!!.toDouble()
                            ),
                        )
                    } else {
                        abslayoutParams = AbsoluteLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getWidthByPercent(
                                context,
                                imageModel.xValue!!.toDouble()
                            ),
                            getHeightByPercent(
                                context,
                                imageModel.yValue!!.toDouble()
                            ),
                        )
                    }
                    this.layoutParams = abslayoutParams
                }
            }
        }
    }

    companion object {
        const val FIT_XY = "fitxy"
        const val CENTER = "center"
        const val CENTER_CROP = "centercrop"
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (imageModel.isSquare!!) {
            val width = measuredWidth
            val height = measuredHeight
            val dimen = Math.min(width, height)
            setMeasuredDimension(dimen, dimen)
        }
    }

    override fun onDraw(canvas: Canvas) {
        //float radius = 36.0f;
        @SuppressLint("DrawAllocation") val clipPath = Path()
        @SuppressLint("DrawAllocation") val rect = RectF(
            0F, 0F, this.width.toFloat(),
            this.height.toFloat()
        )
        clipPath.addRoundRect(rect, 0F, 0F, Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.onDraw(canvas)
    }

    override fun onDataRetrieved(data: Any?, imageUrl: String?, fileName: String?) {
        val status: String = (data) as String
       /* if (status == "Success") {
            // Load from local
            context.loadFromLocal(this, fileName!!)
        } else {
            loadImageUsingGlide(imageUrl)
        }*/
    }

    override fun onError(data: Any?, imageUrl: String?, fileName: String?) {
        // Whatever the error is load from Glide
        // delete if any file is create:
        try {
            val dir = context.getExternalFilesDir("Signage91/" + "Images/")
            val file = File(dir, fileName)
            if (file.exists()) {
                file.delete();
            }
        } catch (e:Exception){
            e.printStackTrace()
        }
        loadImageUsingGlide(imageUrl)
    }

    private fun loadImageUsingGlide(imageUrl: String?) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(this)
    }
}