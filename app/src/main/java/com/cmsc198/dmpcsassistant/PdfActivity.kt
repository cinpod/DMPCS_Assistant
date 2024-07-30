package com.cmsc198.dmpcsassistant

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.shockwave.pdfium.PdfDocument
import java.io.File

class PdfActivity : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {
    private lateinit var pdfView: PDFView
    private lateinit var pdfFileName: String

    private val TAG: String = PdfActivity::class.java.simpleName

    private var pageNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hide UI
        hideSystemUI()

        // open pdf_view layout, adding event listener to close 'button' to finish activity when clicked
        setContentView(R.layout.pdf_view)
        pdfView = findViewById(R.id.pdfView)
        val closeView: ImageView = findViewById(R.id.pdfClose)
        closeView.setOnClickListener{ finish() }

        val afm = AssistantFolderManager(this)
        val file = File(afm.dmpcsPath, afm.charterName)
        try {
            pdfFileName = afm.charterName

            // library manages all that stuff very cool
            pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(DefaultScrollHandle(this))
                .spacing(10) // in dp, space between pages
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH) // fits PDF with regards to Width, Height, or Both
                .load()

            title = pdfFileName
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // this was in the sample code from the library so i just added it
    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
        title = "$pdfFileName ${page + 1} / $pageCount"
    }

    override fun loadComplete(nbPages: Int) {
        val meta: PdfDocument.Meta = pdfView.documentMeta
        Log.e(TAG, "title = " + meta.title)
        Log.e(TAG, "author = " + meta.author)
        Log.e(TAG, "subject = " + meta.subject)
        Log.e(TAG, "keywords = " + meta.keywords)
        Log.e(TAG, "creator = " + meta.creator)
        Log.e(TAG, "producer = " + meta.producer)
        Log.e(TAG, "creationDate = " + meta.creationDate)
        Log.e(TAG, "modDate = " + meta.modDate)

        printBookmarksTree(pdfView.tableOfContents, "-")
    }

    private fun printBookmarksTree(tree: List<PdfDocument.Bookmark>, sep: String) {
        for (b in tree) {
            Log.e(TAG, "$sep $sep, ${b.title} ${b.pageIdx}")

            if (b.hasChildren()) {
                printBookmarksTree(b.children, "$sep-")
            }
        }
    }

    override fun onPageError(page: Int, t: Throwable?) {
        Log.e(TAG, "Cannot load page $page")
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById(R.id.activity_pdf)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}