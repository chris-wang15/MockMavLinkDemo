package io.mavsdk.androidclient.photo_gallery.net

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder {
    private val api: ApiService = getRetrofit().create(ApiService::class.java)

    // Mock server
    private const val BASE_URL = "https://test.server/"

    private fun getRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(
                5000, TimeUnit.MILLISECONDS
            )
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
//            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .client(client)
            .build()
    }

    @Deprecated("For test only")
    fun getFakeData(): Observable<ResponseData> {
//        return api.getImgUrls()
        return Observable.create {
            Thread.sleep(200)
            val response = Gson().fromJson(FAKE_INFO, ResponseData::class.java)
            it.onNext(response)
        }.subscribeOn(Schedulers.io())
    }

    private const val FAKE_INFO = """
        
{
	"response_code": 200,
	"url_list": [
		"https://www.nasa.gov/sites/default/files/thumbnails/image/afrc2023-0038-345.jpg",
		"https://cdn.britannica.com/86/141086-050-9D7C75EE/Gulfstream-G450-business-jet-passengers.jpg",
        "https://media.wired.com/photos/62b25f4c18e6fafaa97a6477/master/w_2560%2Cc_limit/Air-Serbia-Plane-Russian-Sanctions-Safety-Hazard-Business-1239498184.jpg",
        "https://aviataircraft.com/wp-content/uploads/2023/03/n13hc_fq_door.jpg",
        "https://www.raf.mod.uk/index.cfm/_api/asset/image/?filePath=/raf-beta/assets/Image/C8CD4977-ABE7-4924-A3DE12BA0E0D96AD-Envoy%20IV%20reaches%20full%20service%20capability%202022_Original%20Image_m16613.jpg"
	]
}
        
    """
}