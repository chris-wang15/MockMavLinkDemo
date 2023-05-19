package io.mavsdk.androidclient.photo_gallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.mavsdk.androidclient.photo_gallery.net.RetrofitBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class GalleryViewModel : ViewModel() {
    companion object {
        private const val TAG = "GalleryViewModel"
    }

    private val _imgUrlList: MutableLiveData<ArrayList<String>> = MutableLiveData(ArrayList())
    val imgUrlList: LiveData<ArrayList<String>> = _imgUrlList
    private var netDisposable: Disposable? = null

    fun fetchUrlList() {
        netDisposable?.dispose()
        netDisposable = RetrofitBuilder.getFakeData()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.d(TAG, "fetchUrlList ${Thread.currentThread().name}")
                    val list = ArrayList<String>()
                    list.addAll(it.urlList)
                    _imgUrlList.value = list
                },
                {
                    Log.e(TAG, "net work error", it)
                }
            )
    }

    override fun onCleared() {
        super.onCleared()
        netDisposable?.dispose()
        netDisposable = null
    }
}