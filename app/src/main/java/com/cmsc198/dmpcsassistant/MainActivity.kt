package com.cmsc198.dmpcsassistant

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig


class MainActivity : AppCompatActivity() {

    private lateinit var manageStoragePermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var recyclerView: RecyclerView
    private lateinit var availableLocations: MutableList<String> // array of available locations from string xml

    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    private val scanQrCode = registerForActivityResult(ScanCustomCode(), ::scanResult)
    private val qrPasscode = "DMPCS_FACULTY_MEMBER" // use a QR Code generator to convert text to QR Code (several online)

    companion object {
        var isLoggedIn: Boolean = false

        // scuffed
        // make drawFacultyLocator() invokable from anywhere because how do i do it more elegantly
        fun redrawFacultyLocator(mainActivity: MainActivity, updatedCardItems: MutableList<CardItem>) {
            mainActivity.drawFacultyLocator(updatedCardItems)
        }

        fun logout(mainActivity: MainActivity) {
            mainActivity.logInOut()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // OS UI stuff
        hideSystemUI()

        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        // Settling permission stuff for app
        manageStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Log.d("MainActivity", "MANAGE_EXTERNAL_STORAGE Permission granted.")
                } else {
                    Log.e("MainActivity", "Permission denied.")
                }
            }
        }

        // request permission for Android 10 and below
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            checkAndRequestStoragePermissions() // if permission is not yet granted, app will ask for permission
            showRestartDialog()
        }

        // request permission for Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // make sure this only runs while permission has not been granted yet
            // once permission has been granted, condition below will always evaluate to true
            if (!Environment.isExternalStorageManager()) {
                checkAndRequestManageStoragePermission()
                showRestartDialog()

            }
        }

        // create related files/directory
        val afm = AssistantFolderManager(this)

        // add available locations to mutable list
        addLocations()

        // side buttons and event listener assignment
        val quickLinksCardView = findViewById<CardView>(R.id.quickLinks)
        val citizensCharterCardView = findViewById<CardView>(R.id.citizensCharter)
        val loginCardView = findViewById<CardView>(R.id.login)
        quickLinksCardView.setOnTouchListener(onTouch)
        citizensCharterCardView.setOnTouchListener(onTouch)
        loginCardView.setOnTouchListener(onTouch)

        // generate faculty cards from CSV file
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        afm.initializeAssistantFiles()
        val facultyCsvData = afm.getCsvAllData()
        drawFacultyLocator(facultyCsvData)
    }

    // event listener for side buttons
    private val onTouch: View.OnTouchListener = View.OnTouchListener() { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().cancel()
                v.scaleX = 0.97f
                v.scaleY = 0.97f
                (v as CardView).setCardBackgroundColor(getColor(R.color.light_gray))
            }
            MotionEvent.ACTION_UP -> {
                v.animate().cancel()
                v.animate().scaleX(1f).setDuration(250).start()
                v.animate().scaleY(1f).setDuration(250).start()
                (v as CardView).setCardBackgroundColor(getColor(R.color.white))

                // code above for visual feedback
                // actual functional part
                val x = v.left + event.x
                val y = v.top + event.y
                // check if tap is still within View bounds, otherwise do nothing
                if ((x >= v.left && x <= v.right) && (y >= v.top && y <= v.bottom)) {
                    v.performClick()

                    when (v.id) {
                        R.id.quickLinks -> {
                            // quick links
                            val dialog = QuickLinksDialogFragment()
                            if (!hasOpenedDialogs(this)) dialog.show(supportFragmentManager, QuickLinksDialogFragment.TAG)
                        }
                        R.id.citizensCharter -> {
                            // citizens charter
                            startActivity(Intent(this, PdfActivity::class.java))
                        }
                        R.id.login -> {
                            // login
                            if (!isLoggedIn) {
                                scanQrCode.launch(
                                    ScannerConfig.build {
                                        setBarcodeFormats(listOf(BarcodeFormat.FORMAT_QR_CODE)) // set interested barcode formats
                                        setShowTorchToggle(false) // show or hide (default) torch/flashlight toggle button
                                        setShowCloseButton(true) // show or hide (default) close button
                                        setUseFrontCamera(true) // use the front camera
                                        setKeepScreenOn(true) // keep the device's screen turne d on
                                    }
                                )
                            } else {
                                logInOut()
                            }

                            // idea: when logged in, hide quicklinks and CC buttons to make space for faculty-only menu
                            // - add faculty member, etc
                        }
                    }
                }
            }
        }
        false
    }

    // helper functions

    private fun addLocations() {
        availableLocations = mutableListOf(
            getString(R.string.loc_in_office),
            getString(R.string.loc_in_class_meeting),
            getString(R.string.loc_admin_bldg),
            getString(R.string.loc_off_campus_leave),
            getString(R.string.loc_off_campus_travel)
        )
    }

    private fun logInOut() {
        isLoggedIn = !isLoggedIn // just flip it

        val loginTextView: TextView = findViewById(R.id.loginTextView)
        if (isLoggedIn) {
            loginTextView.text = getString(R.string.card_logout)
        } else {
            loginTextView.text = getString(R.string.card_login)
        }
    }
    private fun drawFacultyLocator(cardItems: MutableList<CardItem>) {
        // cardItems are mapped according to their corresponding location and sorted respectively
        val groupedItems = availableLocations.map { location ->
            LocationGroupItem(
                location = location,
                cardItems = cardItems.filter { it.location == location }.sortedWith(compareBy { it.lastName })
            )
        }

        // reassigning adapter makes recyclerview redraw itself to show changes
        val adapter = LocatorParentAdapter(groupedItems, this)
        recyclerView.adapter = adapter
    }

    private fun scanResult(result: QRResult) {
        val text = when (result) {
            is QRResult.QRSuccess -> {
                result.content.rawValue
                    ?: result.content.rawBytes?.let { String(it) }.orEmpty()
            }
            QRResult.QRUserCanceled -> "User canceled"
            QRResult.QRMissingPermission -> "Missing permission"
            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }

        if (text == qrPasscode) {
            Toast.makeText(this, getString(R.string.login_sucess), Toast.LENGTH_LONG).show()
            logInOut()
        } else {
            Toast.makeText(this, getString(R.string.login_fail), Toast.LENGTH_LONG).show()
        }
    }

    private fun hasOpenedDialogs(activity: FragmentActivity): Boolean {
        // returns true if there is a dialog open, return false otherwise
        val fragments: List<Fragment> = activity.supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is DialogFragment) {
                return true
            }
        }

        return false
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById(R.id.main)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, findViewById(R.id.main)).show(WindowInsetsCompat.Type.systemBars())
    }

    // permissions

    // legacy external storage permissions (Android 10 and below)
    private fun checkAndRequestStoragePermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            }
        }
    }

    // external storage permission for Android 11 and above
    private fun checkAndRequestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${packageName}"))
                manageStoragePermissionLauncher.launch(intent)
            }
        }
    }

    private fun showRestartDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Granted")
            .setMessage("The app will now restart to apply the new permission.")
            .setPositiveButton("Confirm") { _, _ ->
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                Runtime.getRuntime().exit(0)
            }
            .setCancelable(false)
            .show()
    }

    // test functions

    private fun generateTestFaculty() : MutableList<CardItem> {
        val returnList = mutableListOf<CardItem>()
        val nameList = arrayOf(
            arrayOf("Mark Andrae A. Sijera", "Assistant Professor 7"),
            arrayOf("Hanna Mae L. Limpag", "Professor 3"),
            arrayOf("Tobias O. Suico", "Senior Lecturer"),
            arrayOf("Chad Luis L. Bayquen", "Senior Lecturer"))

        for (i in 0..<nameList.count()) {
            val memberName = nameList[i][0]
            val splitName = memberName.split(' ')
            val lastName = splitName[splitName.count() - 1]
            val firstName = splitName.take(splitName.count() - 2).joinToString(" ")
            val middleName = splitName[splitName.count() - 2]

            returnList.add(CardItem("test", lastName, firstName, middleName, "",  availableLocations.random()))
        }

        return returnList
    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
