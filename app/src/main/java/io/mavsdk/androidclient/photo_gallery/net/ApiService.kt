package io.mavsdk.androidclient.photo_gallery.net

import io.reactivex.Observable
import retrofit2.http.GET

interface ApiService {

    @GET("gallery")
    fun getImgUrls(): Observable<ResponseData>
}