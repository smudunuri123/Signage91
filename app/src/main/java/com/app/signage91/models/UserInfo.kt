package com.app.signage91.models

import android.content.pm.ActivityInfo
import com.chibatching.kotpref.KotprefModel

object UserInfo : KotprefModel() {
    var registrationStatus by booleanPref(default = false)
    var lastTimeAppRestartedTime by longPref(default = 0L)
    var lastTimeSystemRestartedTime by longPref(default = 0L)
    var requestedOrientation by intPref(default = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
}