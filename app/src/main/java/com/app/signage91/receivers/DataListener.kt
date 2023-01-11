package com.app.signage91.receivers

interface DataListener {
    fun onDataRetrieved(data: Any?, whichUrl: String?, fileName:String?)
    fun onError(data: Any?, whichUrl: String?, fileName:String?)
}