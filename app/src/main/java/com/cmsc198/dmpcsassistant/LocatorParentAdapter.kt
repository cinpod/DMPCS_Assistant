package com.cmsc198.dmpcsassistant

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LocatorParentAdapter(private val groupedItems: List<LocationGroupItem>, val context: Context) : RecyclerView.Adapter<LocatorParentAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groupedItems[position])
    }

    override fun getItemCount(): Int = groupedItems.size

    inner class GroupViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val locationHeaderView: ConstraintLayout = itemView.findViewById(R.id.locationGroupHeader)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationGroupLabel)
        private val locationRecyclerView: RecyclerView = itemView.findViewById(R.id.locationGroupRecyclerView)

        fun bind(groupedItem: LocationGroupItem) {
            locationTextView.text = groupedItem.location
            locationRecyclerView.layoutManager = GridLayoutManager(itemView.context, 5)
            locationRecyclerView.adapter = LocatorGroupAdapter(groupedItems.first { it.location == groupedItem.location }.cardItems, groupedItems, itemView.context)

            // different header design per location group depending on loc
            var backgroundColor = 0
            var textColor = 0
            var textIcon = 0
            when (groupedItem.location) {
                context.getString(R.string.loc_in_office) -> {
                    textColor = R.color.black
                    backgroundColor = R.color.yellow
                    textIcon = R.drawable.ic_location_header_office
                }
                context.getString(R.string.loc_in_class_meeting) -> {
                    textColor = R.color.black
                    backgroundColor = R.color.yellow
                    textIcon = R.drawable.ic_location_header_class
                }
                context.getString(R.string.loc_admin_bldg) -> {
                    textColor = R.color.white
                    backgroundColor = R.color.terracotta
                    textIcon = R.drawable.ic_location_header_admin
                }
                context.getString(R.string.loc_off_campus_leave) -> {
                    textColor = R.color.white
                    backgroundColor = R.color.dark_gray
                    textIcon = R.drawable.ic_location_header_leave
                }

                itemView.context.getString(R.string.loc_off_campus_travel) -> {
                    textColor = R.color.white
                    backgroundColor = R.color.gray
                    textIcon = R.drawable.ic_location_header_travel
                }

                // default for safety
                else -> {
                    textColor = R.color.black
                    backgroundColor = R.color.white
                    textIcon = R.drawable.ic_location_header_office
                }
            }

            locationTextView.setTextColor(ContextCompat.getColor(context, textColor))
            locationTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(textIcon, 0, textIcon, 0)
            locationHeaderView.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
        }
    }
}