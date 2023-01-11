package com.app.signage91

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.app.signage91", appContext.packageName)
    }

    @Test
    fun checkIsJsonSame(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val mapper = ObjectMapper()
        val fileOldString: String =
            appContext.applicationContext.assets.open("primaryjson.json").bufferedReader()
                .use { it.readText() }
        val fileNewString: String =
            appContext.applicationContext.assets.open("primaryjsonnew.json").bufferedReader()
                .use { it.readText() }
        assertEquals(mapper.readTree(fileOldString), mapper.readTree(fileNewString));
    }
}