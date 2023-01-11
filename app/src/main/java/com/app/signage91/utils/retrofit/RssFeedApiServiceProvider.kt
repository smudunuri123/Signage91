package com.app.signage91.utils.retrofit

import android.content.Context

object RssFeedApiServiceProvider {
    fun provideApiService(applicationContext: Context): RssFeedApiService {
        return RssFeedApiService.Factory.create(applicationContext)
    }
}