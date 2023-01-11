package com.app.signage91.utils


object Constants {
    const val TEXT_VIEW_COMPONENT_DATA =
        "private var textViewComponentData = \"Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\\\\'s standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\\n\" +\n" +
                "            \"    *It\\\\'s not his fault. I know you\\\\'re going to want to, but you can\\\\'t blame him. He really has no idea how it happened. I kept trying to come up with excuses I could say to mom that would keep her calm when she found out what happened, but the more I tried, the more I could see none of them would work. He was going to get her wrath and there was nothing I could say to prevent it.\\n\" +\n" +
                "            \"    *I recollect that my first exploit in squirrel-shooting was in a grove of tall walnut-trees that shades one side of the valley. I had wandered into it at noontime, when all nature is peculiarly quiet, and was startled by the roar of my own gun, as it broke the Sabbath stillness around and was prolonged and reverberated by the angry echoes.\\n\" +\n" +
                "            \"    *The young man wanted a role model. He looked long and hard in his youth, but that role model never materialized. His only choice was to embrace all the people in his life he didn't want to be like.\\n\" +\n" +
                "            \"    *The amber droplet hung from the branch, reaching fullness and ready to drop. It waited. While many of the other droplets were satisfied to form as big as they could and release, this droplet had other plans. It wanted to be part of history. It wanted to be remembered long after all the other droplets had dissolved into history. So it waited for the perfect specimen to fly by to trap and capture that it hoped would eventually be discovered hundreds of years in the future.\""


    val RSS_FEED_BASE_URL = "http://timesofindia.indiatimes.com/"
    val BASE_URL = "https://signage.free.beeceptor.com/"
    //val BASE_URL = "http://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms"

    const val IMAGE = "image"
    const val VIDEO = "video"
    const val YOUTUBE = "youtube"
    const val RSS_FEED = "rssfeed"
    const val RSS_IMAGE_FEED = "rssimagefeed"
    const val TEXTSCROLL = "textScroll"
    const val IMAGE_LIST = "imageList"
    const val VIDEO_LIST = "videoList"

    const val TOP_LEFT = "topleft"
    const val TOP_RIGHT = "topright"
    const val BOTTOM_LEFT = "bottomleft"
    const val BOTTOM_RIGHT = "bottomright"


    object WORKER_TAG {
        const val SLEEP_WAKE_WORKER_TAG = "sleep_wake_worker_tag"
    }

    object NOTIFICATION_CHANNEL {
        const val SYSTEM_WAKE_UP = "SYSTEM_WAKE_UP"
        const val SYSTEM_WAKE_UP_CHANNEL_ID = "SYSTEM_WAKE_UP_CHANNEL_ID"

        const val SYSTEM_RESTART = "SYSTEM_RESTART"
        const val SYSTEM_RESTART_CHANNEL_ID = "SYSTEM_RESTART_CHANNEL_ID"
    }

    object ALARM_MANAGER{
        const val KEY_TIME_IN_MILLIS = "KEY_TIME_IN_MILLIS"
        const val ACTION_SLEEP = "ACTION_SLEEP"
        const val ACTION_WAKE_UP = "ACTION_WAKE_UP"
    }

    const val YES = "Yes"
    const val NO = "No"
    const val TRUE = "True"
    const val FALSE = "FALSE"
    const val SYMBOL_HYPHEN = "-"
    const val DEVICE_ROOTED="Your device is rooted"
}