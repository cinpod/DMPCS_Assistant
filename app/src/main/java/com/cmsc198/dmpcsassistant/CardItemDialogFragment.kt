package com.cmsc198.dmpcsassistant

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import java.io.File

class CardItemDialogFragment(private val id : String, private val context: Context) : DialogFragment() {

    private val csvName = "faculty_info.csv"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.card_item_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true)
        view.setOnClickListener { dismiss() } // close dialog when clicked

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView : ImageView = view.findViewById(R.id.fm_card_image)
        val nameView : TextView = view.findViewById(R.id.fm_card_name)
        val positionView : TextView = view.findViewById(R.id.fm_card_position)
        val locationView : TextView = view.findViewById(R.id.fm_card_location)
        val consultationView : TextView = view.findViewById(R.id.fm_card_consultation)
        val emailView : TextView = view.findViewById(R.id.fm_card_email)
        val updatedView : TextView = view.findViewById(R.id.fm_card_last_updated)

        val afm = AssistantFolderManager(context)
        // set image
        // get image filename to display. if it doesn't exist, use a default icon instead
        val imageFileName = afm.getImageFileName(id)
        if (imageFileName.isNotBlank()) {
            val imageFile = File(afm.imagesPath, imageFileName)
            Glide.with(this).load(imageFile).into(imageView)
        } else {
            imageView.setImageResource(R.drawable.up_mindanao_logo)
        }

        // set text according to the values in the csv
        imageView.contentDescription = "Image of ${csvName[afm.LAST_NAME]}"
        val csvData = afm.getRowFromId(id)
        val consultationText = "${resources.getString(R.string.fm_consultation_time)} ${csvData[afm.CONSULTATION_TIME].ifBlank { "N/A" }}"
        val emailText = "${resources.getString(R.string.fm_email)} ${csvData[afm.EMAIL].ifBlank { "N/A" }}"
        val updatedText = "${resources.getString(R.string.fm_last_updated)} ${csvData[afm.LAST_UPDATED]}"

        // change text to show information
        nameView.text = formatFullName(csvData[afm.LAST_NAME], csvData[afm.FIRST_NAME], csvData[afm.MIDDLE_NAME], csvData[afm.SUFFIX])
        if (csvData[afm.POSITION].isBlank()) positionView.visibility = View.GONE else positionView.text = csvData[afm.POSITION]
        locationView.text = csvData[afm.LOCATION]
        consultationView.text = consultationText
        emailView.text = emailText
        updatedView.text = updatedText
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        // not sure if it does anything but for cleanup purposes
        dismiss()
    }

    companion object {
        const val TAG = "Faculty Member Card"
    }

    // helper functions

    private fun formatFullName(lastName: String, firstName: String, middleName: String, suffix: String): String {
        return "${lastName.uppercase()}, $firstName ${middleName.firstOrNull()?.toString()?.plus(".") ?: ""}${if (suffix.isNotBlank()) " $suffix" else ""}"
    }
}
