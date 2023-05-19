package io.mavsdk.androidclient.photo_gallery

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import io.mavsdk.androidclient.R

class GalleryListAdapter(
    private val imageList: ArrayList<String>
) : RecyclerView.Adapter<GalleryListAdapter.ViewHolder>() {

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val title: TextView = rootView.findViewById(R.id.item_title)
        val image: SimpleDraweeView = rootView.findViewById(R.id.item_image)
    }

    companion object {
        private const val TAG = "GalleryListAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(
            R.layout.gallery_item, parent, false
        )
        return ViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = imageList[position]
        holder.title.text = "Photo $position"
        holder.image.setImageURI(url)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<String>) {
        imageList.clear()
        imageList.addAll(list)
        notifyDataSetChanged()
    }
}