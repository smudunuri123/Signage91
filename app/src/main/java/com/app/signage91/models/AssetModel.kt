package com.app.signage91.models

import java.util.*

data class AssetModel(
    val assetType : String,
    val url : String,
    val duration : Int,
    val mute : Boolean,
    val coordinates : CoordinatesModel
)

data class CoordinatesModel(
    val x : Double,
    val y : Double,
    val height : Double,
    val width : Double
    )

data class CampaignObject (
    val campaignName : String,
    val advertiser : String,
    val duration:Long,
    val assets : List<Any>
        )
