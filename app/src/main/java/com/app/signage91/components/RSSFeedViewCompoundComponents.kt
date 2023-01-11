package com.app.signage91.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.AbsoluteLayout
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.app.signage91.R
import com.app.signage91.RSSFeedCompoundSettingsModel
import com.app.signage91.app.MyApplication
import com.app.signage91.helpers.getHeightByPercent
import com.app.signage91.helpers.getWidthByPercent
import com.app.signage91.models.xml_parser.Article
import com.app.signage91.utils.isNetworkAvailable
import com.app.signage91.utils.retrofit.RssFeedApiService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RSSFeedViewCompoundComponents @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    var rssFeedCompoundSettingsModel: RSSFeedCompoundSettingsModel
) :
    FrameLayout(context, attrs) {

    private var mRssFeedApiService: RssFeedApiService

    private var articleList = ArrayList<Article>()
    private var url = ""
    private var currentIndex = 0

    lateinit var insideImageView: AppCompatImageView
    lateinit var titleTextView: AppCompatTextView
    lateinit var textViewDate: AppCompatTextView
    lateinit var rssIconImageView: AppCompatImageView

    private var timer : Timer? = null

    init {
        mRssFeedApiService = (context.applicationContext as MyApplication).rssFeedApiService
        setValues()
        applyStyles()
        background = ContextCompat.getDrawable(context, R.drawable.textview_background)

        if (context.isNetworkAvailable())
            getAllFeeds(context)
    }

    private fun setValues() {
        val height: Int =
            getHeightByPercent(context, rssFeedCompoundSettingsModel.height)
        height.also { rssFeedCompoundSettingsModel.heightValue = it }
        val width: Int =
            getWidthByPercent(context, rssFeedCompoundSettingsModel.width)
        width.also { rssFeedCompoundSettingsModel.widthValue = it }
    }

    @SuppressLint("ResourceType")
    private fun applyStyles() {
        this.setBackgroundColor(Color.parseColor("#000000"))
//        this.setBackgroundResource(ContextCompat.getColor(context, R.color.black))
        rssFeedCompoundSettingsModel.widthValue.let {
            if (it != 0 && rssFeedCompoundSettingsModel.heightValue != 0) {
                layoutParams = AbsoluteLayout.LayoutParams(
                    rssFeedCompoundSettingsModel.widthValue!!,
                    rssFeedCompoundSettingsModel.heightValue!!,
                    getWidthByPercent(context, rssFeedCompoundSettingsModel.xValue!!.toDouble()),
                    getHeightByPercent(context, rssFeedCompoundSettingsModel.yValue!!.toDouble()),
                )
            } else if (it == 0 && rssFeedCompoundSettingsModel.heightValue != 0) {
                layoutParams = AbsoluteLayout.LayoutParams(
                    AbsoluteLayout.LayoutParams.MATCH_PARENT,
                    rssFeedCompoundSettingsModel.heightValue!!,
                    getWidthByPercent(
                        context,
                        rssFeedCompoundSettingsModel.xValue!!.toDouble()
                    ),
                    getHeightByPercent(
                        context,
                        rssFeedCompoundSettingsModel.yValue!!.toDouble()
                    ),
                )
            } else if (it != 0 && rssFeedCompoundSettingsModel.heightValue == 0) {
                layoutParams = AbsoluteLayout.LayoutParams(
                    rssFeedCompoundSettingsModel.widthValue!!,
                    AbsoluteLayout.LayoutParams.MATCH_PARENT,
                    getWidthByPercent(
                        context,
                        rssFeedCompoundSettingsModel.xValue!!.toDouble()
                    ),
                    getHeightByPercent(
                        context,
                        rssFeedCompoundSettingsModel.yValue!!.toDouble()
                    ),
                )
            } else {
                layoutParams = AbsoluteLayout.LayoutParams(
                    AbsoluteLayout.LayoutParams.MATCH_PARENT,
                    AbsoluteLayout.LayoutParams.MATCH_PARENT,
                    getWidthByPercent(
                        context,
                        rssFeedCompoundSettingsModel.xValue!!.toDouble()
                    ),
                    getHeightByPercent(
                        context,
                        rssFeedCompoundSettingsModel.yValue!!.toDouble()
                    ),
                )
            }
        }
    }

    private fun getAllFeeds(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = mRssFeedApiService.getFeed(rssFeedCompoundSettingsModel.rssFeedUrl)
            withContext(Dispatchers.Main) {
                if (response.articleList != null) {
                    url = response.feedImage?.url ?: ""
                    articleList.addAll(response.articleList!!)
                }
                var wholeData = ""
                for (i in articleList.indices) {
                    wholeData = wholeData + articleList.get(i).title
                }

                insideImageView = AppCompatImageView(context)
                val lp = ViewGroup.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
                insideImageView.layoutParams = lp
                addView(insideImageView)

                val linearLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    val insideLinearLayout1 = LinearLayout(context).apply {
                        val lParams = LinearLayout.LayoutParams(
                            MATCH_PARENT,
                            0
                        )
                        lParams.weight = 1f
                        layoutParams = lParams

                        rssIconImageView = AppCompatImageView(context).apply {
                            layoutParams =
                                LinearLayout.LayoutParams(150, WRAP_CONTENT)
                            gravity = Gravity.END
                        }
                        addView(rssIconImageView)
                    }
                    val insideLinearLayout2 = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        val lParams = LinearLayout.LayoutParams(
                            MATCH_PARENT,
                            WRAP_CONTENT
                        )
                        layoutParams = lParams
                        gravity = Gravity.CENTER_HORIZONTAL
                        setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.black_transparent_75
                            )
                        )

                        addView(LinearLayout(context).apply {
                            gravity = Gravity.RIGHT
                            val lParams = LinearLayout.LayoutParams(
                                MATCH_PARENT,
                                WRAP_CONTENT
                            )
                            layoutParams = lParams
                        })

                        val linearLayoutInside = LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL
                            val lParams = LinearLayout.LayoutParams(
                                MATCH_PARENT,
                                0
                            )
                            lParams.weight = 1f
                            layoutParams = lParams
                            gravity = Gravity.CENTER
                            setPadding(16, 16, 16, 0)

                            titleTextView = AppCompatTextView(context).apply {
                                gravity = Gravity.CENTER
                                setTextColor(ContextCompat.getColor(context, R.color.white))
                                textSize = 15f
                            }
                            addView(titleTextView)
                        }
                        addView(linearLayoutInside)

                        textViewDate = AppCompatTextView(context).apply {
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                            textSize = 8f
                            gravity = Gravity.CENTER
                            setPadding(16, 16, 16, 16)
                        }
                        addView(textViewDate)
                    }
                    addView(insideLinearLayout1)
                    addView(insideLinearLayout2)
                }
                addView(linearLayout)


                if (timer == null){
                    timer = Timer()
                }
                timer?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            if (currentIndex < articleList.size) {
                                val article = articleList[currentIndex]
                                val doc: Document =
                                    Jsoup.parse(article.description.toString())
                                val elements: Elements = doc.select("body *")
                                for (element in elements) {
                                    val urlSrc = element.attr("src")
                                    if (urlSrc.isNotEmpty()) {
                                        Glide.with(context.applicationContext).load(urlSrc)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true).into(insideImageView)
                                        insideImageView.visibility = VISIBLE
                                    } else {
                                        //imageView.visibility = View.INVISIBLE
                                    }
                                }


                                val finalTimeInMillis = kotlin.math.abs(
                                    getCurrentTimeInMillis() - getFormattedData(
                                        article.pubDate.toString()
                                    )
                                )
                                var outputText = ""
                                var output = TimeUnit.MILLISECONDS.toHours(finalTimeInMillis)
                                if (output > 0L) {
                                    outputText = "$output hours ago"
                                } else {
                                    output = TimeUnit.MILLISECONDS.toMinutes(finalTimeInMillis)
                                    outputText = "$output minutes ago"
                                }
                                titleTextView.text = article.title
                                textViewDate.text = outputText
                                Glide.with(context.applicationContext).load(url)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true).into(rssIconImageView)

                                currentIndex++
                            } else {
                                //timer.cancel()
                                currentIndex = 0
                            }
                        }
                    }
                }, 0, rssFeedCompoundSettingsModel.slidingDuration)

            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer?.cancel()
        timer = null
    }


    private fun getCurrentTimeInMillis(): Long {
        val currentDate = Date()
        return currentDate.time
    }

    private fun getFormattedData(date: String): Long {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date: Date = inputFormat.parse(date)!!
        return date.time
    }
}