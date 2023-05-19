package io.mavsdk.androidclient.photo_gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.mavsdk.androidclient.R

class GalleryFragment : Fragment() {
    companion object {
        private const val TAG = "GalleryFragment"
    }

    private val viewModel: GalleryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return LayoutInflater.from(context).inflate(
            R.layout.gallery_fg, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.gallery_list)
        val adapter = GalleryListAdapter(viewModel.imgUrlList.value ?: ArrayList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)
        viewModel.imgUrlList.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
        viewModel.fetchUrlList()
    }
}