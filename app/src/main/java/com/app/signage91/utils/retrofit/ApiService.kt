package com.app.signage91.utils.retrofit

import android.content.Context
import com.app.signage91.models.response.rest_api.user.AddUserResponse
import com.app.signage91.models.response.rest_api.user.GetUserResponseItem
import com.app.signage91.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET("user")
    suspend fun getUser(): ArrayList<GetUserResponseItem>

    @POST("user")
    suspend fun addUser(): AddUserResponse

    companion object Factory {
        private val TIMEOUT: Long = 120
        var gson = GsonBuilder()
            .setLenient().create()

        private val builder = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))

        private val httpClient = OkHttpClient.Builder()
        private var retrofit: Retrofit? = null
        fun create(applicationContext: Context): ApiService {
            httpClient.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            httpClient.writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            httpClient.readTimeout(TIMEOUT, TimeUnit.SECONDS)
            //if (BuildConfig.DEBUG)
            //    httpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            retrofit = builder.client(httpClient.build())
                .build()
            return retrofit!!.create(ApiService::class.java)
        }
    }
}