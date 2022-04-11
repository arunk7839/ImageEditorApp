package com.c1ctech.imageeditorapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.c1ctech.imageeditorapp.databinding.ActivityMainBinding
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding
    var imageActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    var saveImageActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        activityMainBinding.btnPickImage.setOnClickListener {
            checkPermission()
        }

        imageActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult>() {

                if (it.resultCode == Activity.RESULT_OK) {

                    val uri: Uri? = it.data?.data
                    var dsPhotoEditorIntent = Intent(this, DsPhotoEditorActivity::class.java)
                    dsPhotoEditorIntent.data = uri

                    // An optional parameter, to specify the directory to save the output image on device's external storage.
                    // If the output directory is omitted, the edited photo will be saved into a folder called "DS_Photo_Editor" by default.
                    dsPhotoEditorIntent.putExtra(
                        DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY,
                        "Images"
                    )

                    val toolsToHide = intArrayOf(
                        DsPhotoEditorActivity.TOOL_WARMTH,
                        DsPhotoEditorActivity.TOOL_SATURATION,
                        DsPhotoEditorActivity.TOOL_VIGNETTE,
                        DsPhotoEditorActivity.TOOL_EXPOSURE
                    )

                    // if you don't want some of the tools to show up.
                    // Just simply pass in the tools to hide in the UI.
                    dsPhotoEditorIntent.putExtra(
                        DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE,
                        toolsToHide
                    )

                    saveImageActivityResultLauncher?.launch(dsPhotoEditorIntent)

                }
            })

        saveImageActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult>() {

                if (it.resultCode == Activity.RESULT_OK) {

                    //handle the result uri ,by display it in an imageView
                    val uri: Uri? = it.data?.data

                    activityMainBinding.imageView.setImageURI(uri)
                    Toast.makeText(this, "Photo Saved", Toast.LENGTH_SHORT).show()

                }
            })
    }

    private fun checkPermission() {
        var permission = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        //for android version Q and above, we are directly calling the pickImage() method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pickImage()
        } else {
            //requesting permission, if permission is not granted
            if (permission != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    100
                )
            else
                pickImage()
        }
    }

    private fun pickImage() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        setResult(100, intent)
        imageActivityResultLauncher?.launch(intent)

    }

    //Callback for the result from requesting permissions.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            pickImage()
        else
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
    }

}