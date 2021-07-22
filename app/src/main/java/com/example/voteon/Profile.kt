package com.example.voteon

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var toolbar: Toolbar? = null
    private lateinit var img: ImageView
    private var CODE_IMG_GALLERY: Int = 1
    private var SAMPLE_CROPPED_IMG_NAME: String = "SampleCropImg"
    var pic_update : Int = 0
    var selectedProfileUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        toolbar = findViewById(R.id.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener{
            finish()
        }

        init()
        display()
        auth = FirebaseAuth.getInstance()

        var email = auth.currentUser?.email

        txt_user_email.text = "$email"

        img.setOnClickListener(View.OnClickListener(){
            pic_select()
        })
    }

    private fun init() {
        this.img = findViewById(R.id.user_profile)
    }

    private fun display(){
        var ref = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val post = dataSnapshot.getValue(User_detail::class.java)
                // [START_EXCLUDE]
                post?.let {
                    var profile : ImageView = user_profile

                    var name = it.name.toString()
                    txt_profile_name.text = name
                    var uri = it.uri.toString()
                    if(uri != ""){
                        Picasso.get()
                            .load(uri)
                            .into(profile)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }

        }
        ref.addValueEventListener(postListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.save_profile, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_save -> {

                if(selectedProfileUri != null){
                    uploadImageToFirebase()
                }
                else finish()

                return true
            }
            R.id.btn_discard ->{
                Toast.makeText(applicationContext, "click on share", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.btn_signout ->{
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, LoginPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun pic_select(){
        val dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog .setCancelable(false)
        dialog .setContentView(R.layout.profile_option)
        val update = dialog .findViewById(R.id.btn_update_photo) as Button
        val delete = dialog .findViewById(R.id.btn_delete_photo) as Button
        update.setOnClickListener {
            startActivityForResult(Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), CODE_IMG_GALLERY)
            dialog .dismiss()
        }
        delete.setOnClickListener {
            //Delete profile picture
            dialog .dismiss()
        }
        dialog .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CODE_IMG_GALLERY && resultCode == Activity.RESULT_OK ){
            //Proceed and check what the selected image is
            var imageUri : Uri? = data!!.getData()
            if(imageUri != null){
                //img.setImageResource(R.drawable.profile_background)
                startCrop(imageUri)
                //Toast.makeText(this, "yh", Toast.LENGTH_SHORT).show()
            }
        }else if(requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK){
            var imageResourceCrop : Uri? = data?.let { UCrop.getOutput(it) }
            if(imageResourceCrop != null){
                img.setImageURI(imageResourceCrop)
                pic_update = 1
                selectedProfileUri = imageResourceCrop
                //Toast.makeText(this, "yh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCrop(uri: Uri){
        var destinationFileName = SAMPLE_CROPPED_IMG_NAME
        destinationFileName += ".jpg"
        var uCrop : UCrop = UCrop.of(uri,Uri.fromFile(File(cacheDir, destinationFileName)))

        uCrop.withAspectRatio(1F, 1F)
        //uCrop.withAspectRatio(3F, 4F)
        //uCrop.withAspectRatio(2F, 3F)
        //uCrop.withAspectRatio(16F, 9F)
        //uCrop.useSourceImageAspectRatio()

        //uCrop.withMaxResultSize(450,450)

        uCrop.withOptions(getCropOptions())
        uCrop.start(this)
    }

    private fun getCropOptions() : UCrop.Options{
        var options: UCrop.Options = UCrop.Options()

        //options.setCompressionQuality(100)

        //CompressType
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
        //options.setCompressionFormat(Bitmap.CompressFormat.JPEG)

        //UI
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(true)

        //Colors
        options.setStatusBarColor(getResources().getColor(R.color.white))
        options.setToolbarColor(getResources().getColor(R.color.real_black))

        options.setToolbarTitle("")

        return options
    }

    private fun uploadImageToFirebase(){
        loading_1.visibility = View.VISIBLE
        if(selectedProfileUri == null){
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val name = uid


        val ref = FirebaseStorage.getInstance().getReference("/ProfilePhoto/$name")

        ref.putFile(selectedProfileUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                var uri = it.toString()

                if(uri != "") {
                    //Toast.makeText(this, "Profile photo uploaded!", Toast.LENGTH_SHORT).show()
                    ref.getMetadata()
                        .addOnSuccessListener(OnSuccessListener<StorageMetadata> { storageMetadata ->
                            var ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("uri")
                            ref.setValue(uri).addOnSuccessListener {
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, "error writing_1", Toast.LENGTH_SHORT).show()
                            }
                            }).addOnFailureListener {
                            loading_1.visibility = View.GONE
                            Toast.makeText(this, "error writing_2", Toast.LENGTH_SHORT).show()
                        }
                }else
                {
                    loading_1.visibility = View.GONE
                    Toast.makeText(this, "Error uploading", Toast.LENGTH_SHORT).show()
                    val desertRef = FirebaseStorage.getInstance().getReference("ProfilePhoto/$name.jpg")

                    // Delete the file
                    desertRef.delete().addOnSuccessListener {
                        Toast.makeText(this, "Try uploading again", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        // Uh-oh, an error occurred!
                    }
                }

            }

        }.addOnFailureListener{
            Toast.makeText(this, "Failed uploading", Toast.LENGTH_SHORT).show()
        }

    }
}
