package com.app.signage91.utils.retrofit

import android.content.Context
import com.app.signage91.models.xml_parser.Feed
import com.app.signage91.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit


interface RssFeedApiService {

    /*@GET("rssfeeds/{id}")
    suspend fun getFeed(@Path("id") id: String): Feed*/

    /*@GET("rssfeeds/-2128936835.cms")
    suspend fun getFeed(): Feed*/

    @GET
    suspend fun getFeed(@Url url: String?): Feed

    companion object Factory {
        private val TIMEOUT: Long = 120
        var gson = GsonBuilder()
            .setLenient().create()

        private val builder = Retrofit.Builder()
            .baseUrl(Constants.RSS_FEED_BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())

        private val httpClient = OkHttpClient.Builder()
        private var retrofit: Retrofit? = null
        fun create(applicationContext: Context): RssFeedApiService {
            httpClient.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            httpClient.writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            httpClient.readTimeout(TIMEOUT, TimeUnit.SECONDS)
            //if (BuildConfig.DEBUG)
            //    httpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            retrofit = builder.client(httpClient.build())
                .build()
            return retrofit!!.create(RssFeedApiService::class.java)
        }
    }
}