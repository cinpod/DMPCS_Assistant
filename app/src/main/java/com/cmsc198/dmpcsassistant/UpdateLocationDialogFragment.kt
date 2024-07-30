package com.cmsc198.dmpcsassistant

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class UpdateLocationDialogFragment(val id: String, val location: String, private val groupedItems: List<LocationGroupItem>, private val context: Context) : DialogFragment() {

    companion object {
        const val TAG = "Update Location Dialog"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.update_location_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView: ImageView = view.findViewById(R.id.update_image)
        val textView: TextView = view.findViewById(R.id.update_name)
        val officeButton: Button = view.findViewById(R.id.update_office_button)
        val classButton: Button = view.findViewById(R.id.update_class_button)
        val adminButton: Button = view.findViewById(R.id.update_admin_button)
        val leaveButton: Button = view.findViewById(R.id.update_leave_button)
        val travelButton: Button = view.findViewById(R.id.update_travel_button)

        val inOffice = getString(R.string.loc_in_office)
        val inClass = getString(R.string.loc_in_class_meeting)
        val inAdmin = getString(R.string.loc_admin_bldg)
        val inLeave = getString(R.string.loc_off_campus_leave)
        val inTravel = getString(R.string.loc_off_campus_travel)

        val afm = AssistantFolderManager(context)
        // set image
        val imageFileName = afm.getImageFileName(id)
        if (imageFileName.isNotBlank()) {
            val imageFile = File(afm.imagesPath, imageFileName)
            Glide.with(this).load(imageFile).into(imageView)
        }

        // set text
        val csvData = afm.getRowFromId(id)
        textView.text = formatFullName(csvData[afm.LAST_NAME], csvData[afm.FIRST_NAME], csvData[afm.MIDDLE_NAME], csvData[afm.SUFFIX])

        // disable button for current location
        var currentButton: Button? = null
        when (location) {
            inOffice -> {
                currentButton = officeButton
            }
            inClass -> {
                currentButton = classButton
            }
            inAdmin -> {
                currentButton = adminButton
            }
            inLeave -> {
                currentButton = leaveButton
            }
            inTravel -> {
                currentButton = travelButton
            }
        }
        currentButton?.isEnabled = false
        currentButton?.setTextColor(context.getColor(R.color.gray))
        currentButton?.setCompoundDrawableTintList(ColorStateList.valueOf(context.getColor(R.color.gray)))
        currentButton?.setBackgroundColor(context.getColor(R.color.light_gray))

        // attach onclick listeners
        officeButton.setOnClickListener(onClick)
        classButton.setOnClickListener(onClick)
        adminButton.setOnClickListener(onClick)
        leaveButton.setOnClickListener(onClick)
        travelButton.setOnClickListener(onClick)
    }

    private val onClick = View.OnClickListener { view ->
        val afm = AssistantFolderManager(context)
        val updatedCardItems = mutableListOf<CardItem>()

        for (groupedItem in groupedItems) {
            updatedCardItems.addAll(groupedItem.cardItems.toMutableList())
        }

        var updatedLocation = ""
        when (view.id) {
            R.id.update_office_button -> {
                updatedLocation = resources.getString(R.string.loc_in_office)
            }
            R.id.update_class_button -> {
                updatedLocation = resources.getString(R.string.loc_in_class_meeting)
            }
            R.id.update_admin_button -> {
                updatedLocation = resources.getString(R.string.loc_admin_bldg)
            }
            R.id.update_leave_button -> {
                updatedLocation = resources.getString(R.string.loc_off_campus_leave)
            }
            R.id.update_travel_button -> {
                updatedLocation = resources.getString(R.string.loc_off_campus_travel)
            }
        }

        // search for the updated card in a now ungrouped card list and then update its cell in CSV
        updatedCardItems.first{ it.id == id }.location = updatedLocation
        afm.updateCell(updatedLocation, afm.LOCATION, id)

        // update 'last updated' cell for updated card
        val formatter = DateTimeFormatter.ofPattern("h:mm a, d MMMM uuuu", Locale.ENGLISH)
        val currentTime = LocalDateTime.now().format(formatter)
        afm.updateCell(currentTime, afm.LAST_UPDATED, id) // update time last updated in CSV

        // force recyclerView in MainActivity to redraw itself with modified groupedItems to reflect the update
        MainActivity.redrawFacultyLocator(context as MainActivity, updatedCardItems)
        MainActivity.logout(context)
        dismiss()
    }

    // helper functions

    private fun formatFullName(lastName: String, firstName: String, middleName: String, suffix: String): String {
        return "${lastName.uppercase()}, $firstName ${middleName.firstOrNull()?.toString()?.plus(".") ?: ""}${if (suffix.isNotBlank()) " $suffix" else ""}"
    }
}