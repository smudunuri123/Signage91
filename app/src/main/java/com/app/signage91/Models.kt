package com.app.signage91

import com.app.signage91.components.TextViewComponent

enum class COMPONENT_TYPE {
    ANDROID_DEFAULT, EXO_PLAYER, TEXT_VIEW, COMPONENT, RSS_FEED, RSS_FEED_COMPOUND, REST_API, GRPC, ADB_COMMANDS,
    SCREENSHOT_AND_RECORDING, SLEEP_AND_WAKE_UP, ORIENTATION,
}

object KEY_INTENT {
    val KEY_TYPE = "KEY_TYPE"
}

data class VideoSettingsModel(
    var url: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    var width: Double = 1000.0,
    var height: Double = 500.0,
    var fileName: String = "TestVideo2.mp4",
    var heightValue: Int? = 0,
    var widthValue: Int? = 0,
    val xValue: Double? = 0.0,
    val yValue: Double? = 0.0,
    val duration: Int = 0,
    val isPrimary: Boolean = false
)


data class ExoPlayerSettingsModel(
    var url: String = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
    var width: Double = 1000.0,
    var height: Double = 500.0,
    var fileName: String = "ExoTextVideo2.mp4",
    var aspectRatio: Int,
    var heightValue: Int? = 0,
    var widthValue: Int? = 0,
    val xValue: Double? = 0.0,
    val yValue: Double? = 0.0,
    val duration: Int = 0,
    val isPrimary: Boolean = false
)


data class TextViewSettingModel(
    var text: String,
    var width: Double,
    var height: Double,
    var direction: TextViewComponent.Direction,
    var duration: Int,
    var speed: TEXT_VIEW_SCROLLING_SPPED,
    var fontColor: Int,
    var fontSize: Float,
    var background: Int,
    var heightValue: Int? = 0,
    var widthValue: Int? = 0,
    var isPrimary: Boolean = false,
    val xValue: Double? = 0.0,
    val yValue: Double? = 0.0
)


enum class TEXT_VIEW_SCROLLING_SPPED(val value: Float) {
    LOW(1f),
    MEDIUM(3f),
    HIGH(6f)
}


data class RSSFeedSettingsModel(
    var rssFeedUrl: String,
    var width: Double,
    var height: Double,
    var heightValue: Int? = 0,
    var widthValue: Int? = 0,
    val xValue: Double? = 0.0,
    val yValue: Double? = 0.0,
    val duration: Int = 0,
    val isPrimary: Boolean = false
)


data class RSSFeedCompoundSettingsModel(
    var rssFeedUrl: String,
    var width: Double,
    var height: Double,
    var slidingDuration: Long,
    var heightValue: Int? = 0,
    var widthValue: Int? = 0,
    val xValue: Double? = 0.0,
    val yValue: Double? = 0.0,
    val duration: Int = 0,
    val isPrimary: Boolean = false
)

data class VideoListSettingsModel(
    var urls: MutableList<URLDataModel>,
    var width: Double,
    var height: Double,
    var fileName: String,
    var heightValue: Int? = 0,
    var widthValue: Int? = 0,
    val xValue: Double? = 0.0,
    val yValue: Double? = 0.0,
    val duration: Int = 0,
    val isPrimary: Boolean = false
)

data class URLDataModel(
    var url: String,
    var filename: String
)


