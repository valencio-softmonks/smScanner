package com.valencio.smscanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.valencio.smscannermodule.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.drawable.Drawable
import android.widget.ImageView


class MainActivity : AppCompatActivity() {

    private lateinit var scanner: CodeScanner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        val imageView = findViewById<ImageView>(R.id.image_view)
        val frameLayout = findViewById<FrameLayout>(R.id.frame_layout)

        scanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        scanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        scanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        scanner.formats = CodeScanner.TWO_DIMENSIONAL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        scanner.autoFocusMode = AutoFocusMode.CONTINUOUS // or CONTINUOUS or SAFE
        scanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        scanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        scanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        /*scanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
            }
        }*/


        scanner.decodeCallback = DecodeCallback { result, cropArea, surfaceArea ->

            runOnUiThread {
                Toast.makeText(this, "Scan result: $cropArea", Toast.LENGTH_LONG).show()
                Toast.makeText(this, "Scan result: ${result.text}", Toast.LENGTH_LONG).show()
                val bitmapImage = getBitmapFromView(scannerView)
                val croppedBitmap = Bitmap.createBitmap(
                    bitmapImage!!,
                    cropArea.left - 10,
                    cropArea.top - 10,
                    cropArea.width + 10,
                    cropArea.height + 10
                )
                imageView.setImageBitmap(croppedBitmap)
                imageView.visibility = View.VISIBLE
            }

        }

        scanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(
                    this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        scannerView.setOnClickListener {
            scanner.startPreview()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas) else  //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

/*
    private fun screenshot(view: View, filename: String): String {
        val date = Date()

        // Here we are initialising the format of our image name
        val format = DateFormat.format("yyyy-MM-dd_hh:mm:ss", date)
        try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val imageurl = getFilename()
            val outputStream = FileOutputStream(imageurl)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return imageurl
        } catch (io: FileNotFoundException) {
            io.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }*/

/*    private fun screenshot(view: View, filename: String): File? {
        val date = Date()
        // Here we are initialising the format of our image name
        val format = DateFormat.format("yyyyMMddhhmmss", date)
        try {
            // Initialising the directory of storage
            val dirpath = Environment.getExternalStorageDirectory().toString() + ""
            val file = File(dirpath)
            if (!file.exists()) {
                val mkdir = file.mkdir()
            }

            // File name
            val path = "$dirpath/$filename$format.jpeg"
            view.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            val imageurl = File(path)
            val outputStream = FileOutputStream(imageurl)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outputStream.flush()
            outputStream.close()
            return imageurl
        } catch (io: FileNotFoundException) {
            io.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }*/
/*
    private fun saveImageToGallery() {
        val bm =getBitmapFromView(scannerView)
        bm?.let {
            val pic_name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            var fileDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .path + "/NPAG Images"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                applicationContext.externalCacheDir?.let {
                    fileDir =
                        Objects.requireNonNull(applicationContext.externalCacheDir!!.absolutePath)
                }
            }

            val filePath = fileDir + "/" + pic_name + ".JPEG"
            val canvas = Canvas(bm)
            frameLayout.draw(canvas)
            val file = File(fileDir)

            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.d("MyCameraApp", "failed to create directory")
                    return
                }
            }
            try {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(filePath))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun getFilename(): String {
        var fileDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .path + "MyFolder/Images"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            applicationContext.externalCacheDir?.let {
                fileDir =
                    Objects.requireNonNull(applicationContext.externalCacheDir!!.absolutePath)
            }
        }
        val file = File(fileDir)
        if (!file.exists()) {
            file.mkdirs()
        }
        val uriSting = (file.absolutePath + "/" + System.currentTimeMillis() + ".jpg")
        return uriSting
    }*/

    override fun onResume() {
        super.onResume()
        scanner.startPreview()
    }

    override fun onPause() {
        scanner.releaseResources()
        super.onPause()
    }
}