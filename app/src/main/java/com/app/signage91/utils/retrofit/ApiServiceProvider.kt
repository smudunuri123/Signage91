package com.app.signage91.utils.retrofit

import android.content.Context

object ApiServiceProvider {
    fun provideApiService(applicationContext: Context): ApiService {
        return ApiService.Factory.create(applicationContext)
    }
}