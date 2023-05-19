package io.mavsdk.androidclient.photo_gallery.net

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResponseData(
    @SerializedName("response_code")
    val responseCode: Int,
    @SerializedName("url_list")
    val urlList: List<String>
) : Serializable {
    companion object {
        private const val serialVersionUID = -2159767L
    }
}