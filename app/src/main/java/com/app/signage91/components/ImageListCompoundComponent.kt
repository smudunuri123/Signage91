package com.app.signage91.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AbsoluteLayout
import android.widget.LinearLayout
import com.app.signage91.adapter.ImageSliderAdapter
import com.app.signage91.helpers.getHeightByPercent
import com.app.signage91.helpers.getWidthByPercent
import com.app.signage91.models.ImageListCompoundModel
import com.app.signage91.utils.Constants
import com.smarteist.autoimageslider.SliderView

class ImageListCompoundComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, imageListCompoundModel: ImageListCompoundModel
) : LinearLayout(context, attrs) {

    private lateinit var sliderView: SliderView
    lateinit var imageSliderAdapter: ImageSliderAdapter
    private val listModel = imageListCompoundModel

    init {
        applyStyles()
        setImageSlider()
    }

    private fun applyStyles() {
//        x = basicUtilis!!.getWidthByPercent(context, listModel.xValue!!.toDouble())
//            .toFloat()
//        y = basicUtilis!!.getHeightByPercent(context, listModel.yValue!!.toDouble())
//            .toFloat()

        val height: Int = getHeightByPercent(context, listModel.height)
        val width: Int = getWidthByPercent(context, listModel.width)
        val abslayoutParams: AbsoluteLayout.LayoutParams

        if (width != 0 && height != 0) {
            abslayoutParams = AbsoluteLayout.LayoutParams(
                width,
                height,
                getWidthByPercent(
                    context,
                    listModel.xValue!!.toDouble()
                ),
                getHeightByPercent(
                    context,
                    listModel.yValue!!.toDouble()
                ),
            )
        } else if (width == 0 && height != 0) {
            abslayoutParams = AbsoluteLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height,
                getWidthByPercent(
                    context,
                    listModel.xValue!!.toDouble()
                ),
                getHeightByPercent(
                    context,
                    listModel.yValue!!.toDouble()
                ),
            )
        } else if (width != 0 && height == 0) {
            abslayoutParams = AbsoluteLayout.LayoutParams(
                width,
                LinearLayout.LayoutParams.MATCH_PARENT,
                getWidthByPercent(
                    context,
                    listModel.xValue!!.toDouble()
                ),
                getHeightByPercent(
                    context,
                    listModel.yValue!!.toDouble()
                ),
            )
        } else {
            abslayoutParams = AbsoluteLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                getWidthByPercent(
                    context,
                    listModel.xValue!!.toDouble()
                ),
                getHeightByPercent(
                    context,
                    listModel.yValue!!.toDouble()
                ),
            )
        }
        this.layoutParams = abslayoutParams
    }

    private fun setImageSlider() {
        sliderView = SliderView(context)
        sliderView.apply {
            setIndicatorEnabled(false)
            isAutoCycle = true
            isClickable = false
            startAutoCycle()
            listModel.let {
                imageSliderAdapter =
                    ImageSliderAdapter(context, imageListCompoundModel = listModel, Constants.IMAGE)
                autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
                setSliderAdapter(imageSliderAdapter)
                listModel.duration.let {
                    scrollTimeInSec = if (it == 0) {
                        3
                    } else {
                        it
                    }
                }
            }

            setOnTouchListener(
                View.OnTouchListener
                { view, motionEvent -> false })
        }
        addView(sliderView)
    }
}