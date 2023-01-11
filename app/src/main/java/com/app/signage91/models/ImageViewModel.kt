package com.app.signage91.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.app.signage91.URLDataModel

open class ImageViewModel(
    val url: String?,
    val width: Double? = 0.0,
    var height: Double? = 0.0,
    val isSquare: Boolean? = false,
    val scaleType: String? = "FitXY",
    val fileName:String? = "",
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
        parcel.readBoolean(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    constructor() : this("")


    @SuppressLint("NewApi")
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(scaleType)
        parcel.writeDouble(width!!)
        parcel.writeDouble(height!!)
        parcel.writeString(fileName)
        parcel.writeDouble(xValue!!)
        parcel.writeDouble(yValue!!)
    }

    override fun describeContents(): Int {
        return 0
    }

    @SuppressLint("ParcelCreator")
    companion object CREATOR : Parcelable.Creator<ImageViewModel> {
        override fun createFromParcel(parcel: Parcel): ImageViewModel {
            return ImageViewModel(parcel)
        }

        override fun newArray(size: Int): Array<ImageViewModel?> {
            return arrayOfNulls(size)
        }
    }
}

data class ImageListCompoundModel(val urls: MutableList<URLDataModel>,
                                  val width: Double? = 0.0,
                                  val height: Double? = 0.0,
                                  val isSquare: Boolean? = false,
                                  val scaleType: String? = "FitXY",
                                  val fileName:String? = "",
                                  val xValue:Double?=0.0,
                                  val yValue:Double? = 0.0,
                                  val duration: Int = 0,
                                  val isPrimary: Boolean = false)
