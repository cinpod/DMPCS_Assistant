package com.cmsc198.dmpcsassistant

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class ImageItem(
    val imageResId: Int,
    val label: String
)

class QuickLinksDialogFragment : DialogFragment() {
    private var imageList = mutableListOf<ImageItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.quick_links_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true) // close fragment when empty space is clicked
        view.setOnClickListener { dismiss() } // close fragment when done clicked

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // preset qr codes + labels
        imageList.add(ImageItem(R.drawable.upmin, requireActivity().getString(R.string.ql_upmin)))
        imageList.add(ImageItem(R.drawable.dmpcs, requireActivity().getString(R.string.ql_dmpcs)))
        imageList.add(ImageItem(R.drawable.dmpcs_faculty, requireActivity().getString(R.string.ql_dmpcs_faculty)))
        imageList.add(ImageItem(R.drawable.request_to_use, requireActivity().getString(R.string.ql_request_to_use)))
        imageList.add(ImageItem(R.drawable.online_resources, requireActivity().getString(R.string.ql_online_resources)))
        imageList.add(ImageItem(R.drawable.academic_calendar, requireActivity().getString(R.string.ql_academic_calendar)))

        val recyclerView: RecyclerView = view.findViewById(R.id.ql_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = QuickLinksGridAdapter(imageList)
        recyclerView.suppressLayout(true)
    }

    companion object {
        const val TAG = "Quick Links"
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        dismiss()
    }
}
