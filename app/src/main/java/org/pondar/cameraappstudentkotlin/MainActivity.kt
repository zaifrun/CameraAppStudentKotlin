package org.pondar.cameraappstudentkotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var context: Context
    private lateinit var imageBitmap: Bitmap
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageView: ImageView //for displaying the image.
    private var mCurrentPhotoPath: String? = null //for storing the path to the image taken.

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //NOTICE we do not get any data directly back from the intent
            //but the camera app will take the picture and save it in the uri
            //we have specified earlier and we can use that path to read the file
            //and convert it into a bitmap.

            if (mCurrentPhotoPath != null) {
                //decoding the file into a bitmap
                imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
                //setting the bitmap on the image file.
                imageView.setImageBitmap(imageBitmap)
            } else {
                val toast = Toast.makeText(this, "no image filed saved", Toast.LENGTH_SHORT)
                toast.show()
            }

        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //Make sure we have an app that can hanlde the IMAGE_CAPTURE intent.
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()  //create an image file to use
            } catch (ex: IOException) {
                // Error occurred while creating the File
                val toast = Toast.makeText(this, "Could not create file", Toast.LENGTH_SHORT)
                toast.show()
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                //This creates a URI for our file, using the provider we have
                //made in the manifest file.
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "org.pondar.cameraappstudentkotlin.fileprovider",
                    photoFile
                )
                //putting the URI in the Intent for the camera app to use
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                //start the intent and wait for the result.
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }

        } else {
            //Make a toast if there is no camera app installed.
            val toast = Toast.makeText(this, "No program to take pictures", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name - we want a unique filename
        //so we are using the current time as a timestamp to include in the filename.
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        //This specifies that we store the images in the PRIVATE folder
        //that ONLY our app has access to (this is the effect of getExternalFilesDir).
        //and the Environment.DIRECTORY_PICTURES constant will make sure that
        //we store the file in the subdirectory called "Pictures" within that
        //directory.
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        //you can take a look at this path in the android monitor - it will give you
        //some idea of what the path looks like - this is a VERY good idea.
        Log.d("myphoto", "storagedirectory:" + storageDir!!)

        //This creates an empty file.
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents used later
        mCurrentPhotoPath = image.absolutePath
        Log.d("myphto", "photo path:" + mCurrentPhotoPath!!)
        return image
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        context = this

        //putting a clicklistener on the button.
        pictureButton.setOnClickListener { dispatchTakePictureIntent() }

        clearButton.setOnClickListener { imageView.setImageResource(0) }
    }


}
