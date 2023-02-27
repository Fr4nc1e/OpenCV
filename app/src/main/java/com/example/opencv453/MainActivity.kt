package com.example.opencv453

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class MainActivity : Activity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private val mIsColorSelected = false
    lateinit var mOpenCvCameraView: CameraBridgeViewBase

    // Initialize OpenCV manager.
    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
    override fun onCameraViewStarted(width: Int, height: Int) {}
    override fun onCameraViewStopped() {}
    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
//        return inputFrame.rgba()

        val frame = inputFrame.rgba()
        // Perform Canny Edge Detect
        Core.flip(frame, frame, 1)
        val img_result = frame.clone()
        Imgproc.Canny(frame, img_result, 80.0, 90.0)
        return img_result
    }

    // Activity Cycle Call Backs
    // Called when the activity is first created.
    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        Log.d(TAG, "OpenCV Loading status: ${OpenCVLoader.initDebug()}")
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
        mOpenCvCameraView = findViewById(R.id.javaCameraView)
        mOpenCvCameraView.setCameraPermissionGranted()
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
    }
    public override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }
    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(
                OpenCVLoader.OPENCV_VERSION_3_0_0,
                this,
                mLoaderCallback,
            )
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
    public override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }
    companion object {
        private const val TAG = "MainActivity"
    }
}
