package com.app.signage91.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

data class YoutubeViewModel(
    val url: String?,
    val width: Double? = 0.0,
    var height: Double? = 0.0,
   var heightValue:Int?=0,
    var widthValue:Int?=0,
    val xValue:Double?=0.0,
    val yValue:Double? = 0.0,
    val duration: Int = 0,
    val isPrimary: Boolean = false
) : Parcelable {
    @SuppressLint("NewApi")
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
      parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    constructor() : this("")


    @SuppressLint("NewApi")
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeDouble(width!!)
        parcel.writeDouble(height!!)
        parcel.writeInt(heightValue!!)
        parcel.writeInt(widthValue!!)
        parcel.writeDouble(xValue!!)
        parcel.writeDouble(yValue!!)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<YoutubeViewModel> {
        override fun createFromParcel(parcel: Parcel): YoutubeViewModel {
            return YoutubeViewModel(parcel)
        }

        override fun newArray(size: Int): Array<YoutubeViewModel?> {
            return arrayOfNulls(size)
        }
    }
}