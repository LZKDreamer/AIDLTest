package com.flyaudio.aidltest

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Author: LiaoZhongKai
 * Date: 2020/8/4 15:04
 * Description:
 */
interface ApiService {

    @GET("data/category/Girl/type/Girl/page/{page}/count/10")
    fun getDataList(@Path("page") page: Int): Observable<ArticleResponse>


}