package com.cmsc198.dmpcsassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuickLinksGridAdapter(private val imageList: List<ImageItem>) : RecyclerView.Adapter<QuickLinksGridAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quick_link_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ql_imageView)
        val textView: TextView = itemView.findViewById(R.id.ql_textView)

        // set image and labels of quick link cards
        fun bind(imageItem: ImageItem) {
            imageView.setImageResource(imageItem.imageResId)
            textView.text = imageItem.label
            textView.contentDescription = imageItem.label
        }
    }
}
