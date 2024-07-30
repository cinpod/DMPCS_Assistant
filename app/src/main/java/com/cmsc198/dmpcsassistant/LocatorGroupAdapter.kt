package com.cmsc198.dmpcsassistant

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class LocatorGroupAdapter(private var cardItems: List<CardItem>, val groupedItems: List<LocationGroupItem>, private val context: Context) : RecyclerView.Adapter<LocatorGroupAdapter.ItemViewHolder>() {
//    private var cardItems = listOf<CardItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocatorGroupAdapter.ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
        view.setOnTouchListener(onTouch) // add event listener to created card

        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(cardItems[position], context)
    }

    override fun getItemCount(): Int = cardItems.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.cardImageView)
        private val nameView: TextView = itemView.findViewById(R.id.cardNameView)
        private val idView: TextView = itemView.findViewById(R.id.cardIdView)
        private val locationView: TextView = itemView.findViewById(R.id.cardLocationView)

        fun bind(cardItem: CardItem, context: Context) {
            val afm = AssistantFolderManager(context)

            // set image
            // get image filename to display. if it doesn't exist, use a default icon instead
            val imageFileName = afm.getImageFileName(cardItem.id)
            if (imageFileName.isNotBlank()) {
                val imageFile = File(afm.imagesPath, imageFileName)
                Glide.with(context).load(imageFile).into(imageView)
            } else {
                imageView.setImageResource(R.drawable.up_mindanao_logo)
            }

            imageView.contentDescription = "Image of ${cardItem.lastName}"
            nameView.text = cardItem.formatFullName()
            idView.text = cardItem.id
            locationView.text = cardItem.location
        }
    }

    private val onTouch = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().cancel()
                v.scaleX = 0.97f
                v.scaleY = 0.97f
                (v as CardView).setCardBackgroundColor(context.getColor(R.color.light_gray))
            }
            MotionEvent.ACTION_UP -> {
                v.animate().cancel();
                v.animate().scaleX(1f).setDuration(250).start()
                v.animate().scaleY(1f).setDuration(250).start()
                (v as CardView).setCardBackgroundColor(context.getColor(R.color.white))
                v.performClick()

                val id = v.findViewById<TextView>(R.id.cardIdView).text.toString()
                val location = v.findViewById<TextView>(R.id.cardLocationView).text.toString()

                // create dialog, passing context and faculty member Id of pressed card view
                if (MainActivity.isLoggedIn) {
                    val dialog = UpdateLocationDialogFragment(id, location, groupedItems, context)
                    if (!hasOpenedDialogs((context as FragmentActivity))) dialog.show((context as FragmentActivity).supportFragmentManager, UpdateLocationDialogFragment.TAG)
                } else {
                    val dialog = CardItemDialogFragment(v.findViewById<TextView>(R.id.cardIdView).text.toString(), context)
                    if (!hasOpenedDialogs((context as FragmentActivity))) dialog.show((context as FragmentActivity).supportFragmentManager, CardItemDialogFragment.TAG)
                }

            }
            MotionEvent.ACTION_CANCEL -> {
                v.animate().cancel();
                v.animate().scaleX(1f).setDuration(250).start()
                v.animate().scaleY(1f).setDuration(250).start()
                (v as CardView).setCardBackgroundColor(context.getColor(R.color.white))
            }
        }
        true
    }

    private fun hasOpenedDialogs(activity: FragmentActivity): Boolean {
        val fragments: List<Fragment> = activity.supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is DialogFragment) {
                return true
            }
        }

        return false
    }
}