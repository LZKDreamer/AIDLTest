package com.flyaudio.aidltest

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Author: LiaoZhongKai
 * Date: 2020/8/4 15:07
 * Description:
 */
class HttpRepository {

    companion object{
        private const val BASE_URL = "https://gank.io/api/v2/"

    }

    private val mHttpClient: OkHttpClient by lazy {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()
    }

    private val mApiService: ApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Retrofit.Builder()
            .client(mHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun requestGirlList(page: Int): Observable<ArticleResponse>{
        return mApiService.getDataList(page)
    }


}