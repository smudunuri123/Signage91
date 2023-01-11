package com.app.signage91

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsoluteLayout
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.app.signage91.app.MyApplication
import com.app.signage91.components.*
import com.app.signage91.databinding.FragmentComponentBinding
import com.app.signage91.helpers.*
import com.app.signage91.models.*
import com.app.signage91.receivers.NetworkConnectionLiveData
import com.app.signage91.utils.Constants
import com.app.signage91.utils.Constants.IMAGE
import com.app.signage91.utils.Constants.IMAGE_LIST
import com.app.signage91.utils.Constants.RSS_FEED
import com.app.signage91.utils.Constants.RSS_IMAGE_FEED
import com.app.signage91.utils.Constants.TEXTSCROLL
import com.app.signage91.utils.Constants.VIDEO
import com.app.signage91.utils.Constants.VIDEO_LIST
import com.app.signage91.utils.Constants.YOUTUBE
import com.app.signage91.utils.room.MyDatabase
import com.app.signage91.utils.room.models.ZoneDataEntity
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject


class ComponentFragment : Fragment() {

    private lateinit var mDatabase: MyDatabase
    private var _binding: FragmentComponentBinding? = null
    private val binding get() = _binding
    var youtubeViewComponent: YoutubeViewComponent? = null
    var isPause: Boolean = false
    var dataList: MutableList<CampaignObject>? = mutableListOf()
    var secondaryDataList: MutableList<CampaignObject>? = mutableListOf()
    var assetList: MutableList<Any>? = null
    var doNotBreak: Boolean = true
    lateinit var job: Job
    lateinit var secondaryJob: Job
    lateinit var primaryLayout: AbsoluteLayout
    lateinit var secondaryLayout: AbsoluteLayout
    var imageFilesList: MutableList<URLDataModel>? = mutableListOf()
    var videoFilesList: MutableList<URLDataModel>? = mutableListOf()

    companion object {
        fun newInstance(): ComponentFragment {
            val fragment = ComponentFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        primaryLayout = AbsoluteLayout(context)
        secondaryLayout = AbsoluteLayout(context)
        checkNetwork()
        mDatabase = (requireActivity().application as MyApplication).database
        if (checkInternetConnection(requireContext())) {
            binding?.signageLogoFull?.visibility = View.GONE
            parsePrimaryJSON()
            parseSecondaryJSON()
            downloadFiles()
            compareJsons()
        } else {
            // Load from local
            // For now showing an error screen
            if (dataList!!.isEmpty() && secondaryDataList!!.isEmpty()) {
                // If there is no data in local, show an image
                requireActivity().addLog("No internet & no data available, Showing logo")
                binding?.signageLogoFull.apply {
                    val height: Int = getHeightByPercent(context, 96.0)
                    val width: Int = getWidthByPercent(context, 100.0)
                    this!!.layoutParams = FrameLayout.LayoutParams(
                        width,
                        height
                    )
                    visibility = View.VISIBLE
                }
            } else {
                binding?.signageLogoFull?.visibility = View.GONE
                parsePrimaryJSON()
                parseSecondaryJSON()
            }
        }
        //Log.i("__TAG", (2/0).toString())
    }

    private fun compareJsons() {
        val fileOldString: String =
            requireContext().applicationContext.assets.open("primaryjson.json").bufferedReader()
                .use { it.readText() }
        val fileNewString: String =
            requireContext().applicationContext.assets.open("primaryjsonnew.json").bufferedReader()
                .use { it.readText() }
        context?.checkIsJsonSame(fileOldString, fileNewString)
    }

    private fun checkNetwork() {
        NetworkConnectionLiveData(requireContext())
            .observe(requireActivity(), Observer { isConnected ->
                if (!isConnected) {
                    binding?.networkImage?.setImageDrawable(requireContext().getDrawable(R.drawable.red_circle))
                    return@Observer
                }
                binding?.networkImage?.setImageDrawable(requireContext().getDrawable(R.drawable.green_circle))
                if (dataList!!.isNotEmpty() && secondaryDataList!!.isNotEmpty()) {
                    // Do nothing
                } else {
                    parsePrimaryJSON()
                    parseSecondaryJSON()
                }
                binding?.signageLogoFull?.visibility = View.GONE
                binding?.networkImage?.setImageDrawable(requireContext().getDrawable(R.drawable.green_circle))
            })
    }

    private fun parsePrimaryJSON() {
        requireActivity().addLog("Parsing Primary Json started.")
        val fileInString: String =
            requireContext().applicationContext.assets.open("primaryjson.json").bufferedReader()
                .use { it.readText() }

        if (mDatabase.zoneDao().findByZone("zone1") == null){
            mDatabase.zoneDao().insert(
                ZoneDataEntity(
                    zone = "zone1",
                    json = fileInString,
                    date = System.currentTimeMillis()
                )
            )
        } else{
            mDatabase.zoneDao().update(
                ZoneDataEntity(
                    zone = "zone1",
                    json = fileInString,
                    date = System.currentTimeMillis()
                )
            )
        }
        val responseObject: JSONObject = JSONObject(fileInString)

        if (responseObject.getInt("statusCode") == 200) {
            // Success scenario
            dataList = mutableListOf()
            parseList(dataList!!, responseObject)
            //handleOrientation(responseObject)
            checkForSystemRestart(responseObject)
            checkForSignageLogo(responseObject)
        }
        setDataAsPerResponse()
        requireActivity().requestedOrientation = UserInfo.requestedOrientation

        /*Handler().postDelayed(Runnable {
            val list: MutableList<CampaignObject>? = mutableListOf()
            responseObject = JSONObject(getResponseJson())
            if (responseObject.getInt("statusCode") == 200) {
                // Success scenario
                val dataArray: JSONArray = responseObject.getJSONArray("data")
                for (i in 0 until dataArray.length()) {
                    assetList = mutableListOf()
                    val dataObject: JSONObject = dataArray.getJSONObject(i)
                    val assetsArray: JSONArray = dataObject.getJSONArray("assets")
                    for (j in 0 until assetsArray.length()) {
                        val assetObject: JSONObject = assetsArray.getJSONObject(j)
                        val coordinateObject: JSONObject =
                            assetObject.getJSONObject("coordinates")
                        val coordinatesModel: CoordinatesModel = CoordinatesModel(
                            coordinateObject.getDouble("x"),
                            coordinateObject.getDouble("y"),
                            coordinateObject.getDouble("height"),
                            coordinateObject.getDouble("width")
                        )
                        when (assetObject.getString("assetType")) {
                            IMAGE -> {
                                val imageViewModel: ImageViewModel = ImageViewModel(
                                    assetObject.getString("url"),
                                    coordinatesModel.width,
                                    coordinatesModel.height,
                                    false,
                                    "FitXY",
                                    "Test1.png",
                                    coordinatesModel.x,
                                    coordinatesModel.y,
                                    assetObject.getInt("duration"),
                                    assetObject.getBoolean("isPrimary")
                                )
                                assetList!!.add(imageViewModel)
                            }
                            VIDEO -> {
                                val videoViewModel: VideoSettingsModel = VideoSettingsModel(
                                    assetObject.getString("url"),
                                    coordinatesModel.width,
                                    coordinatesModel.height,
                                    "Test1.png",
                                    0,
                                    0,
                                    coordinatesModel.x,
                                    coordinatesModel.y,
                                    assetObject.getInt("duration"),
                                    assetObject.getBoolean("isPrimary")
                                )
                                assetList!!.add(videoViewModel)
                            }
                            YOUTUBE -> {
                                val youtubeViewModel: YoutubeViewModel = YoutubeViewModel(
                                    assetObject.getString("url"),
                                    coordinatesModel.width,
                                    coordinatesModel.height,
                                    0,
                                    0,
                                    coordinatesModel.x,
                                    coordinatesModel.y,
                                    assetObject.getInt("duration"),
                                    assetObject.getBoolean("isPrimary")
                                )
                                assetList!!.add(youtubeViewModel)
                            }
                            TEXTSCROLL -> {
                                var textViewScrollingSpeed = TEXT_VIEW_SCROLLING_SPPED.LOW
                                if (assetObject.has("speed")) {
                                    when (assetObject.getString("speed")) {
                                        "LOW" -> textViewScrollingSpeed =
                                            TEXT_VIEW_SCROLLING_SPPED.LOW
                                        "HIGH" -> textViewScrollingSpeed =
                                            TEXT_VIEW_SCROLLING_SPPED.HIGH
                                        "MEDIUM" -> textViewScrollingSpeed =
                                            TEXT_VIEW_SCROLLING_SPPED.MEDIUM
                                    }
                                }
                                var textViewDirection = TextViewComponent.Direction.LEFT
                                if (assetObject.has("direction")) {
                                    val direction = assetObject.getString("direction")
                                    if (direction == "LEFT_TO_RIGHT") {
                                        textViewDirection = TextViewComponent.Direction.RIGHT
                                    } else if (direction == "RIGHT_TO_LEFT") {
                                        textViewDirection = TextViewComponent.Direction.LEFT
                                    }
                                }
                                val textViewSettingModel: TextViewSettingModel =
                                    TextViewSettingModel(
                                        assetObject.getString("text"),
                                        coordinatesModel.width,
                                        coordinatesModel.height,
                                        textViewDirection,
                                        assetObject.getInt("duration"),
                                        textViewScrollingSpeed,
                                        Color.WHITE,
                                        16f,
                                        Color.BLACK,
                                        0,
                                        0,
                                        assetObject.getBoolean("isPrimary"),
                                        coordinatesModel.x,
                                        coordinatesModel.y
                                    )
                                assetList!!.add(textViewSettingModel)
                            }
                            RSS_FEED -> {
                                val rssFeedSettingsModel: RSSFeedSettingsModel =
                                    RSSFeedSettingsModel(
                                        assetObject.getString("url"),
                                        coordinatesModel.width,
                                        coordinatesModel.height,
                                        0,
                                        0,
                                        coordinatesModel.x,
                                        coordinatesModel.y,
                                        assetObject.getInt("duration"),
                                        assetObject.getBoolean("isPrimary")
                                    )
                                assetList!!.add(rssFeedSettingsModel)
                            }
                            RSS_IMAGE_FEED -> {
                                val rssFeedCompoundSettingsModel: RSSFeedCompoundSettingsModel =
                                    RSSFeedCompoundSettingsModel(
                                        assetObject.getString("url"),
                                        coordinatesModel.width,
                                        coordinatesModel.height,
                                        assetObject.getLong("slidingDuration"),
                                        0,
                                        0,
                                        coordinatesModel.x,
                                        coordinatesModel.y,
                                        assetObject.getInt("duration"),
                                        assetObject.getBoolean("isPrimary")
                                    )
                                assetList!!.add(rssFeedCompoundSettingsModel)
                            }
                            IMAGE_LIST -> {
                                val urlList = mutableListOf<URLDataModel>()
                                val urlJSONArray: JSONArray = assetObject.getJSONArray("urls")
                                for (u in 0 until urlJSONArray.length()) {
                                    val urlObject = urlJSONArray.getJSONObject(u)
                                    val urlDataModel = URLDataModel(
                                        urlObject.getString("url"),
                                        urlObject.getString("filename")
                                    )
                                    urlList.add(urlDataModel)
                                }
                                val imageListCompoundModel: ImageListCompoundModel =
                                    ImageListCompoundModel(
                                        urlList,
                                        coordinatesModel.width,
                                        coordinatesModel.height,
                                        false,
                                        "FitXY",
                                        "Test1.png",
                                        coordinatesModel.x,
                                        coordinatesModel.y,
                                        assetObject.getInt("duration"),
                                        assetObject.getBoolean("isPrimary")
                                    )
                                assetList!!.add(imageListCompoundModel)
                            }
                            VIDEO_LIST -> {
                                val urlList = mutableListOf<URLDataModel>()
                                val urlJSONArray: JSONArray = assetObject.getJSONArray("urls")
                                for (u in 0 until urlJSONArray.length()) {
                                    val urlObject = urlJSONArray.getJSONObject(u)
                                    val urlDataModel = URLDataModel(
                                        urlObject.getString("url"),
                                        urlObject.getString("filename")
                                    )
                                    urlList.add(urlDataModel)
                                }
                                val videoListSettingsModel: VideoListSettingsModel =
                                    VideoListSettingsModel(
                                        urlList,
                                        coordinatesModel.width,
                                        coordinatesModel.height,
                                        "Test1.png",
                                        0,
                                        0,
                                        coordinatesModel.x,
                                        coordinatesModel.y,
                                        assetObject.getInt("duration"),
                                        assetObject.getBoolean("isPrimary")
                                    )
                                assetList!!.add(videoListSettingsModel)
                            }
                        }
                    }

                    val campaignObject: CampaignObject = CampaignObject(
                        dataObject.getString("campaignName"),
                        dataObject.getString("advertiser"),
                        dataObject.getLong("duration"),
                        assetList!!
                    )
                    list!!.add(campaignObject)
                }
            }
            dataList = mutableListOf()
            dataList = list
            Log.e("Component", "List updated  ${dataList!!.size}")
            doNotBreak = false
        }, 80000)*/
    }

    private fun checkForSignageLogo(responseObject: JSONObject) {
        if (responseObject.has("signageLogo")) {
            requireActivity().addLog("Signage logo position object received")
            binding?.signageLogo?.apply {
                var logoObject = responseObject.getJSONObject("signageLogo")
                visibility = View.VISIBLE
                when (logoObject.getString("position")) {
                    Constants.TOP_LEFT -> {
                        x = getWidthByPercent(context, 0.0)
                            .toFloat()
                        y = getHeightByPercent(context, 0.0)
                            .toFloat()
                    }
                    Constants.TOP_RIGHT -> {
                        x = getWidthByPercent(context, 85.0)
                            .toFloat()
                        y = getHeightByPercent(context, 0.0)
                            .toFloat()
                    }
                    Constants.BOTTOM_LEFT -> {
                        x = getWidthByPercent(context, 0.0)
                            .toFloat()
                        y = getHeightByPercent(context, 85.0)
                            .toFloat()
                    }
                    Constants.BOTTOM_RIGHT -> {
                        x = getWidthByPercent(context, 85.0)
                            .toFloat()
                        y = getHeightByPercent(context, 85.0)
                            .toFloat()
                    }
                }
            }
        } else {
            binding?.signageLogo?.visibility = View.GONE
        }
    }

    private fun handleOrientation(responseObject: JSONObject) {
        when (responseObject.getInt("orientation")) {
            0 -> requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            1 -> requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            -1 -> requireActivity().requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkForSystemRestart(responseObject: JSONObject) {
        if (responseObject.has("restartSystem")) {
            when (responseObject.getBoolean("restartSystem")) {
                true -> {
                    if ((System.currentTimeMillis() - UserInfo.lastTimeSystemRestartedTime) > 1000 * 60) {
                        try {
                            /*Runtime.getRuntime().exec("su")
                            Runtime.getRuntime().exec("reboot")*/
                            val pm =
                                requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager?
                            pm!!.reboot(null)
                            UserInfo.lastTimeSystemRestartedTime = System.currentTimeMillis()
                        } catch (e: Exception) {
                            checkForAppRestart(responseObject)
                        }
                    } else {
                        checkForAppRestart(responseObject)
                    }
                }
            }
        }
    }

    private fun checkForAppRestart(responseObject: JSONObject) {
        if (responseObject.has("restartApp")) {
            when (responseObject.getBoolean("restartApp")) {
                true -> {
                    if ((System.currentTimeMillis() - UserInfo.lastTimeAppRestartedTime) > 1000 * 60) {
                        UserInfo.lastTimeAppRestartedTime = System.currentTimeMillis()
                        Handler(Looper.getMainLooper()).postDelayed({
                            //primaryLayout.removeAllViews()
                            //secondaryLayout.removeAllViews()
                            ProcessPhoenix.triggerRebirth(requireContext())
                        }, 3000)
                    }
                }
            }
        }
    }

    private fun parseSecondaryJSON() {
        val fileInString: String =
            requireContext().applicationContext.assets.open("secondaryjson.json").bufferedReader()
                .use { it.readText() }
        var responseObject: JSONObject = JSONObject(fileInString)

        if (responseObject.getInt("statusCode") == 200) {
            // Success scenario
            secondaryDataList = mutableListOf()
            parseList(secondaryDataList!!, responseObject)
        }
        setSecondaryDataAsPerResponse()
        requireActivity().requestedOrientation = UserInfo.requestedOrientation

    }

    private fun parseList(
        list: MutableList<CampaignObject>,
        responseObject: JSONObject
    ) {
        val dataArray: JSONArray = responseObject.getJSONArray("data")
        for (i in 0 until dataArray.length()) {
            assetList = mutableListOf()
            val dataObject: JSONObject = dataArray.getJSONObject(i)
            val assetsArray: JSONArray = dataObject.getJSONArray("assets")
            for (j in 0 until assetsArray.length()) {
                val assetObject: JSONObject = assetsArray.getJSONObject(j)
                val coordinateObject: JSONObject = assetObject.getJSONObject("coordinates")
                var coordinatesModel: CoordinatesModel = CoordinatesModel(
                    coordinateObject.getDouble("x"),
                    coordinateObject.getDouble("y"),
                    coordinateObject.getDouble("height"),
                    coordinateObject.getDouble("width")
                )
                when (assetObject.getString("assetType")) {
                    IMAGE -> {
                        var imageViewModel: ImageViewModel = ImageViewModel(
                            assetObject.getString("url"),
                            coordinatesModel.width,
                            coordinatesModel.height,
                            false,
                            "FitXY",
                            getFileName(assetObject, assetObject.getString("url")),
                            coordinatesModel.x,
                            coordinatesModel.y,
                            assetObject.getInt("duration"),
                            assetObject.getBoolean("isPrimary")
                        )
                        assetList!!.add(imageViewModel)
                        var urlDataModel = URLDataModel(
                            assetObject.getString("url"),
                            getFileName(assetObject, assetObject.getString("url"))
                        )
                        imageFilesList!!.add(urlDataModel)
                    }
                    VIDEO -> {
                        var videoViewModel: VideoSettingsModel = VideoSettingsModel(
                            assetObject.getString("url"),
                            coordinatesModel.width,
                            coordinatesModel.height,
                            getFileName(assetObject, assetObject.getString("url")),
                            0,
                            0,
                            coordinatesModel.x,
                            coordinatesModel.y,
                            assetObject.getInt("duration"),
                            assetObject.getBoolean("isPrimary")
                        )
                        assetList!!.add(videoViewModel)
                        var urlDataModel = URLDataModel(
                            assetObject.getString("url"),
                            getFileName(assetObject, assetObject.getString("url"))
                        )
                        videoFilesList!!.add(urlDataModel)
                    }
                    YOUTUBE -> {
                        var youtubeViewModel: YoutubeViewModel = YoutubeViewModel(
                            assetObject.getString("url"),
                            coordinatesModel.width,
                            coordinatesModel.height,
                            0,
                            0,
                            coordinatesModel.x,
                            coordinatesModel.y,
                            assetObject.getInt("duration"),
                            assetObject.getBoolean("isPrimary")
                        )
                        assetList!!.add(youtubeViewModel)
                    }
                    TEXTSCROLL -> {
                        var textViewScrollingSpeed = TEXT_VIEW_SCROLLING_SPPED.LOW
                        if (assetObject.has("speed")) {
                            when (assetObject.getString("speed")) {
                                "LOW" -> textViewScrollingSpeed = TEXT_VIEW_SCROLLING_SPPED.LOW
                                "HIGH" -> textViewScrollingSpeed =
                                    TEXT_VIEW_SCROLLING_SPPED.HIGH
                                "MEDIUM" -> textViewScrollingSpeed =
                                    TEXT_VIEW_SCROLLING_SPPED.MEDIUM
                            }
                        }
                        var textViewDirection = TextViewComponent.Direction.LEFT
                        if (assetObject.has("direction")) {
                            val direction = assetObject.getString("direction")
                            if (direction == "LEFT_TO_RIGHT") {
                                textViewDirection = TextViewComponent.Direction.RIGHT
                            } else if (direction == "RIGHT_TO_LEFT") {
                                textViewDirection = TextViewComponent.Direction.LEFT
                            } else if (direction == "TOP_TO_BOTTOM") {
                                textViewDirection = TextViewComponent.Direction.DOWN
                            } else if (direction == "BOTTOM_TO_TOP") {
                                textViewDirection = TextViewComponent.Direction.UP
                            }
                        }
                        val textViewSettingModel: TextViewSettingModel = TextViewSettingModel(
                            assetObject.getString("text"),
                            coordinatesModel.width,
                            coordinatesModel.height,
                            textViewDirection,
                            assetObject.getInt("duration"),
                            textViewScrollingSpeed,
                            Color.WHITE,
                            16f,
                            Color.BLACK,
                            0,
                            0,
                            assetObject.getBoolean("isPrimary"),
                            coordinatesModel.x,
                            coordinatesModel.y
                        )
                        assetList!!.add(textViewSettingModel)
                    }
                    RSS_FEED -> {
                        var rssFeedSettingsModel: RSSFeedSettingsModel = RSSFeedSettingsModel(
                            assetObject.getString("url"),
                            coordinatesModel.width,
                            coordinatesModel.height,
                            0,
                            0,
                            coordinatesModel.x,
                            coordinatesModel.y,
                            assetObject.getInt("duration"),
                            assetObject.getBoolean("isPrimary")
                        )
                        assetList!!.add(rssFeedSettingsModel)
                    }
                    RSS_IMAGE_FEED -> {
                        var rssFeedCompoundSettingsModel: RSSFeedCompoundSettingsModel =
                            RSSFeedCompoundSettingsModel(
                                assetObject.getString("url"),
                                coordinatesModel.width,
                                coordinatesModel.height,
                                assetObject.getLong("slidingDuration"),
                                0,
                                0,
                                coordinatesModel.x,
                                coordinatesModel.y,
                                assetObject.getInt("duration"),
                                assetObject.getBoolean("isPrimary")
                            )
                        assetList!!.add(rssFeedCompoundSettingsModel)
                    }
                    IMAGE_LIST -> {
                        var urlList = mutableListOf<URLDataModel>()
                        var urlJSONArray: JSONArray = assetObject.getJSONArray("urls")
                        for (u in 0 until urlJSONArray.length()) {
                            var urlObject = urlJSONArray.getJSONObject(u)
                            var urlDataModel = URLDataModel(
                                urlObject.getString("url"),
                                getFileName(urlObject, urlObject.getString("url"))
                            )
                            urlList.add(urlDataModel)
                            imageFilesList!!.add(urlDataModel)
                        }
                        var imageDuration = (dataObject.getLong("duration") / urlList.size).toInt()
                        Log.d("ImageDurationCal", "Camp duration ${dataObject.getLong("duration")}  ImageList Size ${urlList.size}")
                        Log.d("ImageDuration", "Image duration $imageDuration")
                        var imageListCompoundModel: ImageListCompoundModel =
                            ImageListCompoundModel(
                                urlList,
                                coordinatesModel.width,
                                coordinatesModel.height,
                                false,
                                "FitXY",
                                "",
                                coordinatesModel.x,
                                coordinatesModel.y,
                                imageDuration,
                                assetObject.getBoolean("isPrimary")
                            )
                        assetList!!.add(imageListCompoundModel)
                    }
                    VIDEO_LIST -> {
                        var urlList = mutableListOf<URLDataModel>()
                        var urlJSONArray: JSONArray = assetObject.getJSONArray("urls")
                        for (u in 0 until urlJSONArray.length()) {
                            var urlObject = urlJSONArray.getJSONObject(u)
                            var urlDataModel = URLDataModel(
                                urlObject.getString("url"),
                                getFileName(urlObject, urlObject.getString("url"))
                            )
                            urlList.add(urlDataModel)
                            videoFilesList!!.add(urlDataModel)
                        }
                        var videoListSettingsModel: VideoListSettingsModel =
                            VideoListSettingsModel(
                                urlList,
                                coordinatesModel.width,
                                coordinatesModel.height,
                                "",
                                0,
                                0,
                                coordinatesModel.x,
                                coordinatesModel.y,
                                assetObject.getInt("duration"),
                                assetObject.getBoolean("isPrimary")
                            )
                        assetList!!.add(videoListSettingsModel)
                    }
                }
            }
            var campaignObject: CampaignObject = CampaignObject(
                dataObject.getString("campaignName"),
                dataObject.getString("advertiser"),
                dataObject.getLong("duration"),
                assetList!!
            )
            list!!.add(campaignObject)
        }
    }

    private fun getFileName(urlObject: JSONObject?, url: String): String {
        return if (urlObject!!.getString("filename").isNotEmpty()) {
            urlObject.getString("filename")
        } else {
            val parsedUrl = url.split("/")
            parsedUrl[parsedUrl.size - 1]
        }
    }

    private fun setSecondaryDataAsPerResponse() {
        secondaryDataList.let {
            secondaryJob = CoroutineScope(Dispatchers.Main).launch {
//                secondaryJobFunction()
            }
        }
    }

    private suspend fun secondaryJobFunction() {
        while (true) {
            Log.e("Component", "Secondary Job started")
            setDataFromList(secondaryDataList, secondaryLayout)
        }
    }

    private suspend fun setDataFromList(
        list: MutableList<CampaignObject>?,
        layout: AbsoluteLayout
    ) {
        list?.forEach { campiagnObject ->
            try {
                layout.removeAllViews()
            } catch (exp: java.lang.Exception){
                exp.printStackTrace()
            }
            try {
                binding?.mainLayout?.removeView(layout)
            } catch (exp: java.lang.Exception){
                exp.printStackTrace()
            }
            Log.e("Component", "Asset Size  ${campiagnObject.assets.size}")
            campiagnObject.assets.let { assetsList ->
                assetsList.forEach { asset ->
                    if (isAdded){
                        when (asset) {
                            is ImageListCompoundModel -> {
                                requireActivity().addLog("Setup started for ImageListCompoundModel.")
                                setUpImageListCompoundComponent(layout, asset)
                            }
                            is VideoListSettingsModel -> {
                                requireActivity().addLog("Setup started for VideoListSettingsModel.")
                                setUpVideoListCompoundComponent(layout, asset)
                            }
                            is ImageViewModel -> {
                                requireActivity().addLog("Setup started for ImageViewModel.")
                                setUpImageViewComponents(layout, asset)
                            }
                            is VideoSettingsModel -> {
                                requireActivity().addLog("Setup started for VideoSettingsModel.")
                                setUpNativeVideoViewComponents(layout, asset)
                            }
                            is YoutubeViewModel -> {
                                requireActivity().addLog("Setup started for YoutubeViewModel.")
                                setYoutubeViewComponent(layout, asset)
                            }
                            is TextViewSettingModel -> {
                                requireActivity().addLog("Setup started for TextViewSettingModel.")
                                setUpTextViewComponents(layout, asset)
                            }
                            is RSSFeedSettingsModel -> {
                                requireActivity().addLog("Setup started for RSSFeedSettingsModel.")
                                setUpRssFeedComponents(layout, asset)
                            }
                            is RSSFeedCompoundSettingsModel -> {
                                requireActivity().addLog("Setup started for RSSFeedCompoundSettingsModel.")
                                setUpRssCompoundComponent(layout, asset)
                            }
                        }
                    }
                }
            }
            binding?.mainLayout?.addView(layout)
            delay(campiagnObject.duration)
        }
    }

    private fun setDataAsPerResponse() {
        dataList.let {
            job = CoroutineScope(Dispatchers.Main).launch {
                jobFunction()
            }
        }
    }

    private suspend fun jobFunction() {
        while (true) {
            Log.e("Component", "Job function started")
            setDataFromList(dataList, primaryLayout)
            if (!doNotBreak) {
                break
            }
        }
        doNotBreak = true
        rerunJob()
    }

    private fun rerunJob() {
        //need to check without threads
        this.job.let {
            if (this.job.isActive) {
                Log.e("Component", "Job is still active")
                this.job.cancelChildren()
                this.job.cancel()
            }
            if (this.job.isCancelled || this.job.isCompleted) {
                Log.e("Component", "Job is cancelled")
                Log.e("Component", "doNtBreak  $doNotBreak")
                this.job = CoroutineScope(Dispatchers.Main).launch {
                    jobFunction()
                }
            }
        }
    }

    private fun setUpRssCompoundComponent(
        layout: AbsoluteLayout,
        asset: RSSFeedCompoundSettingsModel
    ) {
        layout.apply {
            addView(
                RSSFeedViewCompoundComponents(
                    requireContext(),
                    null,
                    asset
                )
            )
        }
    }

    private fun setYoutubeViewComponent(
        layout: AbsoluteLayout,
        youTubeViewModel: YoutubeViewModel
    ) {
        youtubeViewComponent =
            context?.let { it1 -> YoutubeViewComponent(it1, null, youTubeViewModel) }
        youtubeViewComponent?.let { youtubeViewComponent ->
            layout.addView(youtubeViewComponent)
        }
    }

    private fun setUpImageListCompoundComponent(
        layout: AbsoluteLayout,
        asset: ImageListCompoundModel
    ) {
        var imageListCompoundComponent =
            context?.let { it1 -> ImageListCompoundComponent(it1, null, asset) }
        imageListCompoundComponent?.let { imageListComponent ->
            layout.addView(imageListComponent)
        }
    }

    private fun setUpVideoListCompoundComponent(
        layout: AbsoluteLayout,
        asset: VideoListSettingsModel
    ) {
        var videoListCompoundComponent =
            context?.let { it1 -> VideoListCompoundComponent(it1, null, asset) }
        videoListCompoundComponent?.let { videoListComponent ->
            layout.addView(videoListComponent)
        }
    }

    private fun setUpRssFeedComponents(layout: AbsoluteLayout, asset: RSSFeedSettingsModel) {
        layout.apply {
            addView(
                RSSFeedViewComponent(
                    requireContext(),
                    null,
                    asset
                )
            )
        }
    }

    private fun setUpImageViewComponents(layout: AbsoluteLayout, imageViewModel: ImageViewModel) {
        val imageViewComponent: ImageViewComponent? =
            context?.let { it1 -> ImageViewComponent(it1, null, imageViewModel) }
        imageViewComponent?.let { imageViewComponent ->
            layout.addView(imageViewComponent)
        }
    }

    private fun setUpExoplayerVideoComponents(asset: AssetModel) {
        var url = asset.url.split(".")
        binding?.mainLayout?.addView(
            ExoPlayerViewComponent(
                requireContext(), null, ExoPlayerSettingsModel(
                    url = asset.url,
                    aspectRatio = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
                    height = asset.coordinates.height,
                    width = asset.coordinates.width,
                    xValue = asset.coordinates.x,
                    yValue = asset.coordinates.y,
                    fileName = "testVideoz" + ".mp4"
                )
            )
        )
    }

    private fun setUpNativeVideoViewComponents(layout: AbsoluteLayout, asset: VideoSettingsModel) {
        var url = asset.url.split(".")
        layout.apply {
            addView(
                VideoViewComponent(
                    requireContext(), null, asset
                )
            )
        }
    }

    private fun setUpTextViewComponents(layout: AbsoluteLayout, asset: TextViewSettingModel) {
        val listOfString = asset.text.split("*")
        val textViewSettingList = arrayListOf<TextViewSettingModel>().apply {
            add(
                asset
            )
        }

        for (i in textViewSettingList.indices) {
            val textViewSetting = textViewSettingList.get(i)
            layout.addView(TextViewComponent(
                requireContext()
            ).apply {
                layoutParams = setLayoutParamsOfText(textViewSetting)
                setDirection(textViewSetting.direction)
                text = textViewSetting.text
                setTextColor(
                    textViewSetting.fontColor
                )
                textSize = textViewSetting.fontSize
                setBackgroundColor(textViewSetting.background)
                setSpeed(textViewSetting.speed.value)
                setDelayed(0)
            })
        }
    }

    private fun setLayoutParamsOfText(textViewSettingModel: TextViewSettingModel): AbsoluteLayout.LayoutParams {
        val height: Int = getHeightByPercent(context, textViewSettingModel.height)
        textViewSettingModel.heightValue = height
        val width: Int = getWidthByPercent(context, textViewSettingModel.width)
        textViewSettingModel.widthValue = width
        textViewSettingModel.widthValue.let {
            val layoutParams: AbsoluteLayout.LayoutParams
            if (it != 0 && textViewSettingModel.heightValue != 0) {
                layoutParams = AbsoluteLayout.LayoutParams(
                    textViewSettingModel.widthValue!!,
                    textViewSettingModel.heightValue!!,
                    getWidthByPercent(
                        context,
                        textViewSettingModel.xValue!!.toDouble()
                    ),
                    getHeightByPercent(
                        context,
                        textViewSettingModel.yValue!!.toDouble()
                    ),
                )
            } else if (it == 0 && textViewSettingModel.heightValue != 0) {
                layoutParams = AbsoluteLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    textViewSettingModel.heightValue!!,
                    getWidthByPercent(
                        context,
                        textViewSettingModel.xValue!!.toDouble()
                    ),
                    getHeightByPercent(
                        context,
                        textViewSettingModel.yValue!!.toDouble()
                    ),
                )
            } else if (it != 0 && textViewSettingModel.heightValue == 0) {
                layoutParams = AbsoluteLayout.LayoutParams(
                    textViewSettingModel.widthValue!!,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getWidthByPercent(
                        context,
                        textViewSettingModel.xValue!!.toDouble()
                    ),
                    getHeightByPercent(
                        context,
                        textViewSettingModel.yValue!!.toDouble()
                    ),
                )
            } else {
                layoutParams = AbsoluteLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getWidthByPercent(
                        context,
                        textViewSettingModel.xValue!!.toDouble()
                    ),
                    getHeightByPercent(
                        context,
                        textViewSettingModel.yValue!!.toDouble()
                    ),
                )
            }

            return layoutParams
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPause) {
            isPause = false
            youtubeViewComponent?.let {
                it.resumeTimers()
                it.onResume()
                it.setYoutubeView()
            }
        }
//        throw RuntimeException("onResume force exception")
    }

    override fun onPause() {
        super.onPause()
        isPause = true
        youtubeViewComponent?.let {
            it.pauseTimers()
            it.onPause()
        }
    }

    private fun getResponseJson(): String {
        return "{\n" +
                "  \"statusCode\": 200,\n" +
                "  \"data\": [{\n" +
                "    \"campaignName\": \"camp1\",\n" +
                "    \"advertiser\": \"Ad1\",\n" +
                "    \"duration\": 10000,\n" +
                "    \"assets\": [{\n" +
                "      \"assetType\": \"textScroll\",\n" +
                "      \"text\": \"Finance Minister Nirmala Sitharaman, while presenting the budget today, said the Union Budget 2022-23 will lay the foundation for India economic growth and expansion for the next 25 years. Djokovic, who described the allegations as very hurtful to his family, said on Instagram that he only learned of the December 16 test result the following day, after attending a youth tennis event.\",\n" +
                "      \"direction\": \"LEFT_TO_RIGHT\",\n" +
                "      \"duration\": 0,\n" +
                "      \"speed\": \"LOW\",\n" +
                "      \"isPrimary\": false,\n" +
                "      \"coordinates\": {\n" +
                "        \"x\": 0,\n" +
                "        \"y\": 0,\n" +
                "        \"height\": 6,\n" +
                "        \"width\": 100\n" +
                "      }\n" +
                "    },\n" +
                "      {\n" +
                "        \"assetType\": \"video\",\n" +
                "        \"url\": \"http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4\",\n" +
                "        \"duration\": 30,\n" +
                "        \"mute\": false,\n" +
                "        \"isPrimary\": false,\n" +
                "        \"coordinates\": {\n" +
                "          \"x\": 0,\n" +
                "          \"y\": 6,\n" +
                "          \"height\": 28,\n" +
                "          \"width\": 100\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"assetType\": \"image\",\n" +
                "        \"url\": \"https://static.vecteezy.com/system/resources/previews/002/107/506/original/91-years-anniversary-celebration-logo-template-design-illustration-vector.jpg\",\n" +
                "        \"mute\": true,\n" +
                "        \"duration\": 0,\n" +
                "        \"isPrimary\": false,\n" +
                "        \"coordinates\": {\n" +
                "          \"x\": 0,\n" +
                "          \"y\": 34,\n" +
                "          \"height\": 28,\n" +
                "          \"width\": 100\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"assetType\": \"youtube\",\n" +
                "        \"url\": \"II_m28Bm-iM\",\n" +
                "        \"mute\": true,\n" +
                "        \"duration\": 0,\n" +
                "        \"isPrimary\": false,\n" +
                "        \"coordinates\": {\n" +
                "          \"x\": 0,\n" +
                "          \"y\": 62,\n" +
                "          \"height\": 28,\n" +
                "          \"width\": 100\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"assetType\": \"rssfeed\",\n" +
                "        \"url\": \"http://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms\",\n" +
                "        \"mute\": true,\n" +
                "        \"duration\": 0,\n" +
                "        \"isPrimary\": false,\n" +
                "        \"slidingDuration\": 30,\n" +
                "        \"coordinates\": {\n" +
                "          \"x\": 0,\n" +
                "          \"y\": 90,\n" +
                "          \"height\": 6,\n" +
                "          \"width\": 100\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "    {\n" +
                "      \"campaignName\": \"camp2\",\n" +
                "      \"advertiser\": \"Ad2\",\n" +
                "      \"duration\": 15000,\n" +
                "      \"assets\": [{\n" +
                "        \"assetType\": \"video\",\n" +
                "        \"url\": \"http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4\",\n" +
                "        \"mute\": false,\n" +
                "        \"duration\": 0,\n" +
                "        \"isPrimary\": false,\n" +
                "        \"coordinates\": {\n" +
                "          \"x\": 0,\n" +
                "          \"y\": 0,\n" +
                "          \"height\": 30,\n" +
                "          \"width\": 100\n" +
                "        }\n" +
                "      },\n" +
                "        {\n" +
                "          \"assetType\": \"imageList\",\n" +
                "          \"urls\": [{\n" +
                "            \"url\": \"https://static.vecteezy.com/system/resources/previews/002/107/506/original/91-years-anniversary-celebration-logo-template-design-illustration-vector.jpg\",\n" +
                "            \"filename\": \"illuration.jpg\"\n" +
                "          },\n" +
                "            {\n" +
                "              \"url\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_LtrVoiz7CPrelm76hhAT8CDXad3pdx6SBg&usqp=CAU\",\n" +
                "              \"filename\": \"encrypted.jpg\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"url\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTsW_0xlCJoGS16rPmIFypllMUTpP4qZTN0_Q&usqp=CAU\",\n" +
                "              \"filename\": \"gstatic.jpg\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"url\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQlx7JEiLWZY1WHcntrhD4n3E29ph9_SnTsfw&usqp=CAU\",\n" +
                "              \"filename\": \"image.jpg\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"mute\": true,\n" +
                "          \"duration\": 0,\n" +
                "          \"isPrimary\": false,\n" +
                "          \"coordinates\": {\n" +
                "            \"x\": 0,\n" +
                "            \"y\": 30,\n" +
                "            \"height\": 30,\n" +
                "            \"width\": 45\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"assetType\": \"youtube\",\n" +
                "          \"url\": \"II_m28Bm-iM\",\n" +
                "          \"mute\": true,\n" +
                "          \"duration\": 0,\n" +
                "          \"isPrimary\": false,\n" +
                "          \"coordinates\": {\n" +
                "            \"x\": 45,\n" +
                "            \"y\": 30,\n" +
                "            \"height\": 30,\n" +
                "            \"width\": 55\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}"
    }

    private fun downloadFiles() {
        if (checkInternetConnection(requireContext())) {
            requireActivity().addLog("Files Download Started")
            var downloadService = DownloadService(this, context)
            if (imageFilesList!!.isNotEmpty()) {
                for (data in imageFilesList!!) {
                    downloadService.let {
                        if (requireContext().isFileExist(
                                "Images/",
                                data.filename
                            )
                        ) {
                            requireActivity().addLog("Downloading the file ${data.filename}")
                            it.downloadUrlAndSaveLocal(
                                data.url,
                                data.filename,
                                requireContext().getExternalFilesDir("Signage91/Images/")?.path!!
                            )
                        }
                    }
                }
            }
            if (videoFilesList!!.isNotEmpty()) {
                for (videodata in videoFilesList!!) {
                    downloadService.let {
                        if (requireContext().isFileExist("Videos/", videodata.filename)) {
                            requireActivity().addLog("Downloading the file ${videodata.filename}")
                            it.downloadUrlAndSaveLocal(
                                videodata.url,
                                videodata.filename,
                                requireContext().getExternalFilesDir("Signage91/Videos/")?.path!!
                            )
                        }
                    }
                }
            }
        }
    }
}
