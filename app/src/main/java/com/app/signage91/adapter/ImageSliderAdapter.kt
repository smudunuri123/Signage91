package com.app.signage91.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.app.signage91.R
import com.app.signage91.VideoListSettingsModel
import com.app.signage91.VideoSettingsModel
import com.app.signage91.components.ImageViewComponent
import com.app.signage91.components.VideoViewComponent
import com.app.signage91.models.ImageListCompoundModel
import com.app.signage91.models.ImageViewModel
import com.app.signage91.utils.Constants
import com.smarteist.autoimageslider.SliderViewAdapter

class ImageSliderAdapter(
    context: Context,
    imageListCompoundModel: ImageListCompoundModel,
    componentName: String
) :
    SliderViewAdapter<ImageSliderAdapter.SliderViewHolder>() {

    var sliderList = imageListCompoundModel.urls
    var listModel = imageListCompoundModel
    var context = context
    var component = componentName

    override fun getCount(): Int {
        return sliderList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): ImageSliderAdapter.SliderViewHolder {
        val inflate: View =
            LayoutInflater.from(parent!!.context).inflate(R.layout.slider_item, null)
        return SliderViewHolder(inflate)
    }

    override fun onBindViewHolder(viewHolder: ImageSliderAdapter.SliderViewHolder?, position: Int) {
        viewHolder?.layout?.apply {
            if (component == Constants.IMAGE) {
                var imageViewModel = ImageViewModel(
                    url = sliderList[position].url,
                    fileName = sliderList[position].filename,
                    width = listModel.width,
                    height = listModel.height
                )
                val imageViewComponent: ImageViewComponent? =
                    context?.let { it1 -> ImageViewComponent(it1, null, imageViewModel) }
                this.addView(imageViewComponent)
            }
        }
    }

    class SliderViewHolder(itemView: View?) : SliderViewAdapter.ViewHolder(itemView) {
        var layout: RelativeLayout = itemView!!.findViewById(R.id.slide_layout)
    }
}