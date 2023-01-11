package com.app.signage91.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.AbsoluteLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.app.signage91.R
import com.app.signage91.RSSFeedSettingsModel
import com.app.signage91.TEXT_VIEW_SCROLLING_SPPED
import com.app.signage91.app.MyApplication
import com.app.signage91.helpers.getHeightByPercent
import com.app.signage91.helpers.getWidthByPercent
import com.app.signage91.models.xml_parser.Article
import com.app.signage91.utils.isNetworkAvailable
import com.app.signage91.utils.retrofit.RssFeedApiService
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RSSFeedViewComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    var rssFeedSettingsModel: RSSFeedSettingsModel
) :
    LinearLayout(context, attrs) {

    private var mApiService: RssFeedApiService
    private lateinit var textViewComponents: TextViewComponent
    private lateinit var imageView: AppCompatImageView

    private var articleList = ArrayList<Article>()
    private var url = ""

    init {
        mApiService = (context.applicationContext as MyApplication).rssFeedApiService
        background = ContextCompat.getDrawable(context, R.drawable.textview_background)
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        applyStyles()

        if (context.isNetworkAvailable())
            getAllFeeds()
    }

    fun getAllFeeds() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = mApiService.getFeed(rssFeedSettingsModel.rssFeedUrl)
            withContext(Dispatchers.Main) {
                if (response.articleList != null) {
                    url = response.feedImage?.url ?: ""
                    articleList.addAll(response.articleList!!)
                }
                var wholeData = ""
                for (i in articleList.indices) {
                    wholeData = wholeData + articleList.get(i).title
                }

                val linearLayout = LinearLayout(context).apply {
                    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    orientation = HORIZONTAL

                    imageView = AppCompatImageView(context).apply {
                        layoutParams = LayoutParams(240, MATCH_PARENT)
                    }
                    if (url.isNotEmpty()) {
                        Glide.with(context).load(url).into(imageView)
                        imageView.visibility = View.VISIBLE
                    } else {
                        imageView.visibility = View.INVISIBLE
                    }
                    addView(imageView)

                    textViewComponents = TextViewComponent(
                        context
                    ).apply {
                        setDirection(TextViewComponent.Direction.LEFT)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        textSize = 20f
                        background =
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.textview_background
                            )
                        setDelayed(0)
                        setSpeed(TEXT_VIEW_SCROLLING_SPPED.LOW.value)
                        text = wholeData
                    }
                    addView(textViewComponents)
                    //setBackgroundResource(R.drawable.textview_background)
                }
                addView(linearLayout)
            }
        }
    }

    private fun applyStyles() {
        this.apply {
            val height: Int = getHeightByPercent(context, rssFeedSettingsModel.height)
            val width: Int = getWidthByPercent(context, rssFeedSettingsModel.width)

            rssFeedSettingsModel.width.let {
                val abslayoutParams: AbsoluteLayout.LayoutParams
                if (width != 0 && height != 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        width,
                        height,
                        getWidthByPercent(context, rssFeedSettingsModel.xValue!!.toDouble()),
                        getHeightByPercent(context, rssFeedSettingsModel.yValue!!.toDouble()),
                    )
                } else if (width == 0 && height != 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        height,
                        getWidthByPercent(context, rssFeedSettingsModel.xValue!!.toDouble()),
                        getHeightByPercent(context, rssFeedSettingsModel.yValue!!.toDouble()),
                    )
                } else if (width != 0 && height == 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        width,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getWidthByPercent(
                            context,
                            rssFeedSettingsModel.xValue!!.toDouble()
                        ),
                        getHeightByPercent(
                            context,
                            rssFeedSettingsModel.yValue!!.toDouble()
                        ),
                    )
                } else {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getWidthByPercent(
                            context,
                            rssFeedSettingsModel.xValue!!.toDouble()
                        ),
                        getHeightByPercent(
                            context,
                            rssFeedSettingsModel.yValue!!.toDouble()
                        ),
                    )
                }
                layoutParams = abslayoutParams
            }
        }
    }
}