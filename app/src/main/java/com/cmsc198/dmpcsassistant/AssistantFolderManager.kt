package com.cmsc198.dmpcsassistant

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.CSVWriterBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


public class AssistantFolderManager(private val context: Context) {

    // directory and filename definitions
    val dmpcsName = "DMPCS Assistant Files"
    val imagesName = "images"
    val csvName = "faculty_info.csv"
    val charterName = "dmpcs_citizens_charter.pdf"
    val readMeName = "README.txt"
    val basePath = Environment.getExternalStorageDirectory().toString()
    val dmpcsPath = basePath + File.separator + dmpcsName
    val csvPath = dmpcsPath + File.separator + csvName
    val charterPath = dmpcsPath + File.separator + charterName
    val imagesPath = dmpcsPath + File.separator + imagesName
    val readMePath = dmpcsPath + File.separator + readMeName

    val allowedExtensions = listOf("jpg", "jpeg", "png", "webp", "bmp", "gif")

    // header indices
    val ID = 0
    val LAST_NAME = 1
    val FIRST_NAME = 2
    val MIDDLE_NAME = 3
    val SUFFIX = 4
    val POSITION = 5
    val CONSULTATION_TIME = 6
    val EMAIL = 7
    val LOCATION = 8
    val LAST_UPDATED = 9

    // INITIALIZATION

    fun initializeAssistantFiles() {
        val dmpcsDirectory = File(dmpcsPath)

        // get current time in the specified format
        val formatter = DateTimeFormatter.ofPattern("h:mm a, d MMMM uuuu", Locale.ENGLISH)
        val currentTime = LocalDateTime.now().format(formatter)

        // check if directory exists, otherwise create it and build necessary app files
        if (!dmpcsDirectory.exists()) {
            val created = dmpcsDirectory.mkdirs()
            if (created) {
                // build faculty info file and update Last Updated column
                buildFileFromAsset(csvName, csvPath)
                buildFileFromAsset(charterName, charterPath)
                buildFileFromAsset(readMeName, readMePath)
                updateColumn(currentTime, LAST_UPDATED)

                createImagesFolder()
            } else {
                Log.e("MainActivity", "Failed to create directory")
            }
        } else {
            // if faculty CSV doesn't exist, build it
            val csvFile = File(dmpcsPath, csvName)
            if (!csvFile.exists()) {
                buildFileFromAsset(csvName, csvPath)
                updateColumn(currentTime, LAST_UPDATED)
            }

            // do the same for citizens charter
            val charterFile = File(dmpcsPath, charterName)
            if (!charterFile.exists()) {
                buildFileFromAsset(charterName, charterPath)
            }

            // create images folder
            createImagesFolder()

            // if README doesn't exist, build it
            val readmeFile = File(dmpcsPath, readMeName)
            if (!readmeFile.exists()) {
                buildFileFromAsset(readMeName, readMePath)
            }
        }
    }

    private fun buildFileFromAsset(sourceName: String, targetPath: String) {
        try {
            // copy the asset file into a file output placed in target directory
            try {
                FileOutputStream(targetPath).use { out ->
                    context.assets.open(sourceName).use {
                        it.copyTo(out)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to create CSV", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "CSV not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImagesFolder() {
        // create images folder if it doesn't exist
        val imageDirectory = File(dmpcsPath, imagesName)
        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs()
        }
    }

    // OPERATIONS

    fun getValueFromId(id: String, column: Int): String {
        var targetValue = ""

        try {
            val csvFile = File(dmpcsPath, csvName)

            val reader = CSVReader(FileReader(csvFile))
            val lines = reader.readAll()

            for (element in lines) {
                if (element[ID].equals(id)) {
                    targetValue = element[column]
                    break
                }
            }

            reader.close()
            Log.d("FacultyCsvManager", "Value successfully retrieved.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FacultyCsvManager", "Cell retrieval failed", e)
        }

        return targetValue
    }

    fun getRowFromId(id: String): Array<String> {
        var targetRow = arrayOf(String()) // initialize empty string array

        try {
            val csvFile = File(dmpcsPath, csvName)

            // read csv and read all lines
            val reader = CSVReader(FileReader(csvFile))
            val lines = reader.readAll()

            // get all values from the row
            for (element in lines) {
                if (element[ID].equals(id)) {
                    targetRow = element
                    break
                }
            }
            reader.close()
            Log.d("FacultyCsvManager", "Row values successfully retrieved.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FacultyCsvManager", "Row values retrieval failed", e)
        }

        return targetRow
    }

    fun getCsvAllData(): MutableList<CardItem> {
        val facultyInfoList = mutableListOf<CardItem>()
        try {
            val csvFile = File(dmpcsPath, csvName)

            val reader = CSVReader(FileReader(csvFile))
            for (line in reader.iterator()) {
                // skip comment lines
                if (line[ID] == "*" || line[ID].isBlank()) continue

                // only needed info for Faculty Locator card view
                val card = CardItem(
                    line[ID],
                    line[LAST_NAME], // last name
                    line[FIRST_NAME], // first name
                    line[MIDDLE_NAME], // middle name
                    line[SUFFIX], // suffix
                    line[LOCATION] // location
                )
                facultyInfoList.add(card)
            }
            reader.close()
            Log.e("MainActivity", "CSV data collected.")
        } catch (e: Exception) {
            e.printStackTrace();
            Log.e("MainActivity", "CSV File not found", e)
        }

        return facultyInfoList
    }

    fun updateCell(newValue: String, column: Int, id: String) {
        try {
            // open csv
            val reader = CSVReaderBuilder(FileReader(csvPath)).build()
            val csvBody = reader.readAll()
            var index = 0

            for (element in csvBody) {
                // upon reaching target row, get index of desired row
                if (element[ID].equals(id)) {
                    index = csvBody.indexOf(element)
                    break
                }
            }

            // change value of column in target row
            csvBody[index][column] = newValue
            reader.close()

            // write changes to file
            val writer = CSVWriterBuilder(FileWriter(csvPath)).withSeparator(',').build()
            writer.writeAll(csvBody);
            writer.flush();
            writer.close();

            Log.d("FacultyCsvManager", "Cell update successful.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FacultyCsvManager", "Cell update failed", e)
        }
    }

    // function to update value of ALL rows for a target column
    private fun updateColumn(newValue: String, column: Int) {
        try {
            val reader = CSVReaderBuilder(FileReader(csvPath)).build()
            val csvBody = reader.readAll()
            for (element in csvBody) {
                // skip row if row ID is * or blank
                if (element[ID] == "*" || element[ID].isBlank() || element[ID] == "ID") continue

                element[column] = newValue
            }
            reader.close()

            // write changes to file
            val writer = CSVWriterBuilder(FileWriter(csvPath)).withSeparator(',').build()
            writer.writeAll(csvBody);
            writer.flush();
            writer.close();

            Log.d("FacultyCsvManager", "Column updated succesfully.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FacultyCsvManager", "Mass update for column $column to value $newValue failed.", e)
        }
    }

    fun getImageFileName(fileName: String): String {
        val directory = File(imagesPath)

        // directory doesn't exist
        if (!directory.exists() || !directory.isDirectory) {
            return ""
        }

        // get all files that match the file name and has an allowed extension
        val files = directory.listFiles() { dir, name ->
            allowedExtensions.any { name.equals("$fileName.$it", ignoreCase = true) }
        }

        // if there is a file, return the filename
        if (files.isNotEmpty()) {
            return files?.get(0)?.name ?: ""
        }

        return ""
    }
}