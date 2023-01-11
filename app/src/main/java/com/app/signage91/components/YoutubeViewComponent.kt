package com.app.signage91.components

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AbsoluteLayout
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.app.signage91.helpers.getHeightByPercent
import com.app.signage91.helpers.getWidthByPercent
import com.app.signage91.models.YoutubeViewModel

class YoutubeViewComponent(
    context: Context,
    attrs: AttributeSet? = null,
    youtubeViewModel: YoutubeViewModel
) : WebView(context, attrs), DefaultLifecycleObserver {

    var youtubeViewModel: YoutubeViewModel = youtubeViewModel
    var frameVideo =
        "  <!DOCTYPE html>\n" +
                "<html>\n" +
                " <style type=\"text/css\">\n" +
                "        html, body {\n" +
                "            height: 100%;\n" +
                "            width: 100%;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            background-color: #000000;\n" +
                "            overflow: hidden;\n" +
                "            position: fixed;\n" +
                "        }\n" +
                "    </style>\n" +
                "<body>\n" +
                "\n" +
                " <div id=\"player\"></div>\n" +
                "<script>\n" +
                "       var tag = document.createElement('script');\n" +
                "       tag.src = \"https://www.youtube.com/player_api\";\n" +
                "       var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
                "       firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
                "       var player;\n" +
                "       function onYouTubePlayerAPIReady() {\n" +
                "            player = new YT.Player('player', {\n" +
                "                    height: '100%',\n" +
                "                    width: '100%',\n" +
                "                    videoId: 'CUSTOM_ID',\n" +
                "                    events: {\n" +
                "                       'onReady': onPlayerReady,\n" +
                "                       'onStateChange': onPlayerStateChange\n" +
                "                  },\n" +
                "                  playerVars: {\n" +
                "                        'autoplay': 0,\n" +
                "                        'showinfo': 1,\n" +
                "                        'mute': 1,\n" +
                "                        'controls': 1\n" +
                "                                }\n" +
                "                            });\n" +
                "                        }\n" +
                "                        function onPlayerReady(event) {\n" +
                "                            event.target.playVideo();\n" +
                "\n" +
                "                        }\n" +
                "\n" +
                "                        var done = false;\n" +
                "                        function onPlayerStateChange(event) {\n" +
                "                            if (event.data == YT.PlayerState.PLAYING && !done) {\n" +
                "                                done = true;\n" +
                "                            }\n" +
                "                        }\n" +
                "                        function stopVideo() {\n" +
                "                            player.stopVideo();\n" +
                "                        }\n" +
                "                    </script> \n" +
                "\n" +
                "</body>\n" +
                "</html>"

//        "<html><body><iframe width=\"100%\" height=\"800\" src=\"https://www.youtube.com/embed/g_1oiJqE3OI?autoplay=1\" frameborder=\"0\" allowfullscreen></iframe></body></html>"

//        "<html lang=\"en\">\n" +
//                "    <head>\n" +
//                "        <meta charset=\"utf-8\">\n" +
//                "        <style>\n" +
//                "            iframe   { border: 0; position:fixed; width:100%; height:100%; bgcolor=\"#000000\"; }\n" +
//                "            body     { margin: 0; bgcolor=\"#000000\"; }\n" +
//                "        </style>        \n" +
//                "    </head>\n" +
//                "    <body>\n" +
//                "        <iframe src=\"https://www.youtube.com/embed/47yJ2XCRLZs\" frameborder=\"0\" allowfullscreen></iframe>\n" +
//                "    </body>\n" +
//                "</html>"

    init {
        setValues()
        applyStyles()
        setYoutubeView()
    }

    private fun setValues() {
        val height: Int = getHeightByPercent(context, youtubeViewModel.height)
        youtubeViewModel.heightValue = height
        val width: Int = getWidthByPercent(context, youtubeViewModel.width)
        youtubeViewModel.widthValue = width
    }

    public fun setYoutubeView() {
        val webSettings: WebSettings = this.settings
        webSettings.javaScriptEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false;
        webSettings.javaScriptCanOpenWindowsAutomatically = true;
        webSettings.pluginState = WebSettings.PluginState.ON;
        webSettings.loadWithOverviewMode = true;
        webSettings.useWideViewPort = true;
//        this.loadData(frameVideo, "text/html", "utf-8")
        this.loadDataWithBaseURL(
            "https://www.youtube.com/", frameVideo.replace(
                "CUSTOM_ID",
                youtubeViewModel.url!!
            ), "text/html", "utf-8", null
        );
        this.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                // mimic onClick() event on the center of the WebView
                val delta: Long = 100
                val downTime: Long = SystemClock.uptimeMillis()
                val x = (view.left + view.width / 2).toFloat()
                val y = (view.top + view.height / 2).toFloat()
                val tapDownEvent: MotionEvent =
                    MotionEvent.obtain(downTime, downTime + delta, MotionEvent.ACTION_DOWN, x, y, 0)
                tapDownEvent.source = InputDevice.SOURCE_CLASS_POINTER
                val tapUpEvent: MotionEvent = MotionEvent.obtain(
                    downTime,
                    downTime + delta + 2,
                    MotionEvent.ACTION_UP,
                    x,
                    y,
                    0
                )
                tapUpEvent.source = InputDevice.SOURCE_CLASS_POINTER
                view.dispatchTouchEvent(tapDownEvent)
                view.dispatchTouchEvent(tapUpEvent)
            }
        }
    }

    private fun applyStyles() {
        this.apply {
            this.isClickable = false
            youtubeViewModel.width.let {
                val abslayoutParams: AbsoluteLayout.LayoutParams
                if (it != 0.0 && youtubeViewModel.heightValue != 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        youtubeViewModel.widthValue!!,
                        youtubeViewModel.heightValue!!,
                        getWidthByPercent(
                            context,
                            youtubeViewModel.xValue!!.toDouble()
                        ),
                        getHeightByPercent(
                            context,
                            youtubeViewModel.yValue!!.toDouble()
                        ),
                    )
                } else if (it == 0.0 && youtubeViewModel.heightValue != 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        youtubeViewModel.heightValue!!,
                        getWidthByPercent(
                            context,
                            youtubeViewModel.xValue!!.toDouble()
                        ),
                        getHeightByPercent(
                            context,
                            youtubeViewModel.yValue!!.toDouble()
                        ),
                    )
                } else if (it != 0.0 && youtubeViewModel.heightValue == 0) {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        youtubeViewModel.widthValue!!,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getWidthByPercent(
                            context,
                            youtubeViewModel.xValue!!.toDouble()
                        ),
                        getHeightByPercent(
                            context,
                            youtubeViewModel.yValue!!.toDouble()
                        ),
                    )
                } else {
                    abslayoutParams = AbsoluteLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getWidthByPercent(
                            context,
                            youtubeViewModel.xValue!!.toDouble()
                        ),
                        getHeightByPercent(
                            context,
                            youtubeViewModel.yValue!!.toDouble()
                        ),
                    )
                }
                this.layoutParams = abslayoutParams
            }
            this.setOnTouchListener(
                View.OnTouchListener
                { view, motionEvent -> true })
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onPause(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onPause(owner)
        pauseTimers()
        loadUrl("file:///android_asset/nonexistent.html");
        this.onPause(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onResume(owner)
        resumeTimers()
        this.loadDataWithBaseURL(
            "https://www.youtube.com/", frameVideo.replace(
                "CUSTOM_ID",
                youtubeViewModel.url!!
            ), "text/html", "utf-8", null
        )
        this.onResume(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        this.onDestroy(owner)
        super.onDestroy(owner)
    }
}

// Removed below code due to this error
// A YouTubePlayerView can only be created with an Activity  which extends YouTubeBaseActivity as its context. in view
//    private var youtubeViewModel: YoutubeViewModel = youtubeViewModel
//    private var layoutInflater:LayoutInflater? = null
//
//    init {
//        applyStyles()
//        setYoutubePalyer(p1)
//    }
//
//    private fun setYoutubePalyer(p1: AttributeSet?) {
//        val youTubePlayerSupportFragment : YouTubePlayerSupportFragment = YouTubePlayerSupportFragment()
//        var youTubePlayerView: YouTubePlayerView = YouTubePlayerView(context, p1)
//        youTubePlayerView.initialize(
//            youtubeViewModel.apiKey,
//            object : YouTubePlayer.OnInitializedListener {
//                override fun onInitializationSuccess(
//                    p0: YouTubePlayer.Provider?,
//                    p1: YouTubePlayer?,
//                    p2: Boolean
//                ) {
//                    p1?.loadVideo(youtubeViewModel.url) // string has to be https://www.youtube.com/watch?v=----------->9ET6R_MR1Ag<---------
//                }
//
//                override fun onInitializationFailure(
//                    p0: YouTubePlayer.Provider?,
//                    p1: YouTubeInitializationResult?
//                ) {
//                    Toast.makeText(context, "ERROR INITIATING YOUTUBE", Toast.LENGTH_SHORT).show()
//                }
//            }
//        )
//    }
//
//    private fun applyStyles() {
//        this.apply {
//
//            youtubeViewModel.width.let {
//                if (it != 0 && youtubeViewModel.height != 0) {
//                    val layoutParams = LinearLayout.LayoutParams(
//                        youtubeViewModel.width!!,
//                        youtubeViewModel.height!!
//                    )
//                    this.layoutParams = layoutParams
//                } else if (it == 0 && youtubeViewModel.height != 0) {
//                    val layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        youtubeViewModel.height!!
//                    )
//                    this.layoutParams = layoutParams
//                } else if (it != 0 && youtubeViewModel.height == 0) {
//                    val layoutParams = LinearLayout.LayoutParams(
//                        youtubeViewModel.width!!,
//                        LinearLayout.LayoutParams.MATCH_PARENT
//                    )
//                    this.layoutParams = layoutParams
//                } else {
//                    val layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT
//                    )
//                    this.layoutParams = layoutParams
//                }
//            }
//        }
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//    }

