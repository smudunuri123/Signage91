package com.app.signage91.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.app.signage91.R
import com.app.signage91.VideoListSettingsModel
import com.app.signage91.VideoSettingsModel
import com.app.signage91.components.VideoViewComponent
import com.smarteist.autoimageslider.SliderViewAdapter

class VideoSliderAdapter(
    context: Context,
    videoListSettingsModel: VideoListSettingsModel,
    componentName: String
) :
    SliderViewAdapter<VideoSliderAdapter.SliderViewHolder>() {

    var sliderList = videoListSettingsModel.urls
    var listModel = videoListSettingsModel
    var context = context
    var component = componentName

    override fun getCount(): Int {
        return sliderList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): VideoSliderAdapter.SliderViewHolder {
        val inflate: View =
            LayoutInflater.from(parent!!.context).inflate(R.layout.slider_item, null)
        return SliderViewHolder(inflate)
    }

    override fun onBindViewHolder(viewHolder: VideoSliderAdapter.SliderViewHolder?, position: Int) {
        viewHolder?.layout?.apply {
            var videoViewModel = VideoSettingsModel(
                url = sliderList[position].url,
                fileName = sliderList[position].filename,
                width = listModel.width!!,
                height = listModel.height!!
            )
            val videoViewComponent: VideoViewComponent? =
                context?.let { it1 -> VideoViewComponent(it1, null, videoViewModel) }
            this.addView(videoViewComponent)
        }
    }

    class SliderViewHolder(itemView: View?) : SliderViewAdapter.ViewHolder(itemView) {
        var layout: RelativeLayout = itemView!!.findViewById(R.id.slide_layout)
    }
}