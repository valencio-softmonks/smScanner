package com.valencio.smscanner

import android.os.Bundle
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.valencio.smscannermodule.*

class MainActivity : AppCompatActivity(){

    private lateinit var scanner: CodeScanner


    /*private var mTextureView: TextureView? = null
    private var mCamera: Camera? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        barCodeScanner()
        //setupTextureView()

    }

    private fun barCodeScanner() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        scanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        scanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        //scanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        scanner.formats = CodeScanner.TWO_DIMENSIONAL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        scanner.autoFocusMode = AutoFocusMode.CONTINUOUS // or CONTINUOUS or SAFE
        scanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        scanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        scanner.isFlashEnabled = false // Whether to enable flash or not
        scanner.decodeCallback = DecodeCallback { result, cropArea ->

            runOnUiThread {
                Toast.makeText(this, "Scan result: $cropArea", Toast.LENGTH_LONG).show()
                Toast.makeText(this, "Scan result: ${result.text}", Toast.LENGTH_LONG).show()

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

    override fun onResume() {
        super.onResume()
        scanner.startPreview()
    }

    override fun onPause() {
        scanner.releaseResources()
        super.onPause()
    }


    /*private fun setupTextureView() {
        mTextureView = TextureView(this)
        mTextureView!!.surfaceTextureListener = this
        setContentView(mTextureView)
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, p1: Int, p2: Int) {
        mCamera = Camera.open()

        val previewSize = mCamera!!.parameters.previewSize
        mTextureView!!.layoutParams =
            FrameLayout.LayoutParams(previewSize.width/2, previewSize.height/2, Gravity.CENTER)

        try {
            mCamera!!.setPreviewTexture(surfaceTexture)
            mCamera!!.startPreview()
        } catch (ioe: IOException) {
            // Something bad happened
        }

        mTextureView!!.alpha = 1.0f;
        mTextureView!!.rotation = 90.0f;
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
        // Ignored, Camera does all the work for us
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        mCamera!!.stopPreview()
        mCamera!!.release()
        return true
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
        // Invoked every time there's a new Camera preview frame.
    }*/

    companion object {
        val CAMERA_REQUEST_CODE = 100
    }


}