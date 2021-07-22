package com.example.voteon

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_add_group.*
import kotlinx.android.synthetic.main.activity_add_group.loading_1
import kotlinx.android.synthetic.main.activity_add_group.txt_error
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register_page.*
import kotlinx.android.synthetic.main.set_access_code.*
import java.io.File
import java.util.*

class AddGroup : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var toolbar: Toolbar? = null
    private lateinit var img: ImageView
    private var CODE_IMG_GALLERY: Int = 1
    private var SAMPLE_CROPPED_IMG_NAME: String = "SampleCropImg"
    var pic_update : Int = 0
    var selectedProfileUri : Uri? = null
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
        auth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener{
            finish()
        }

        edt_access_code.setOnClickListener {
            showAccessCode()
        }

        init()

        img.setOnClickListener(View.OnClickListener(){
            startActivityForResult(Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), CODE_IMG_GALLERY)
        })
    }

    private fun init() {
        this.img = findViewById(R.id.user_profile)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.save_group, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_save -> {
                loading_1.visibility = View.VISIBLE
                var admin = auth.currentUser?.uid.toString()
                var name = edt_group_name.text.toString()
                var tag = edt_group_tag.text.toString()
                var code = edt_access_code.text.toString()
                var description = textArea_information.text.toString()
                val date = GregorianCalendar().timeInMillis.toString()

                if(name != "" && tag != "" && code != "" && description != ""){
                    if (selectedProfileUri != null){
                        val ref = FirebaseStorage.getInstance().getReference("/GroupsPhoto/$name")

                        ref.putFile(selectedProfileUri!!).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener {
                                var uri = it.toString()

                                if(uri != "") {
                                    //Toast.makeText(this, "Profile photo uploaded!", Toast.LENGTH_SHORT).show()
                                    writeNewUser(admin,code, date, description, name, tag, uri)
                                }else
                                {
                                    loading_1.visibility = View.GONE
                                    Toast.makeText(this, "Error uploading", Toast.LENGTH_SHORT).show()
                                    val desertRef = FirebaseStorage.getInstance().getReference("GroupsPhoto/$name.jpg")

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
                    else{
                        writeNewUser(admin,code, date, description, name, tag, "")
                    }
                }
                else{
                    txt_error.text = "Fill all inputs!"
                }


                return true
            }
            R.id.btn_discard ->{
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    private fun showAccessCode(){
            val dialog = Dialog(this)
            dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
            //dialog .setCancelable(false)
            dialog .setContentView(R.layout.set_access_code)
            val cancel = dialog .findViewById(R.id.cancel) as Button
            val select = dialog .findViewById(R.id.select) as Button
            var pin = dialog.findViewById(R.id.pin) as EditText
            cancel.setOnClickListener {
                dialog .dismiss()
            }
            select.setOnClickListener {
                if(pin.text.length != 4){
                    Toast.makeText(this, "Pin code must be four letters!", Toast.LENGTH_SHORT).show()
                }
                else{
                    edt_access_code.text = pin.text
                    dialog .dismiss()
                }
            }
            dialog .show()
    }

    private fun writeNewUser(admin: String ,code: String, date: String, description: String, name:String, tag: String, uri: String) {
        val user = Groups(admin, code, date, description, name, tag, uri)
        database = FirebaseDatabase.getInstance().reference

        database.child("groups").child(name).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Write successful.", Toast.LENGTH_SHORT).show()
            writeGroupMembers(name, admin)
        }.addOnFailureListener {
            loading_1.visibility = View.GONE
            Toast.makeText(this, "Write not successful.", Toast.LENGTH_SHORT).show()
        }
        //loading_1.visibility = View.GONE
    }

    private fun writeGroupMembers(name: String, admin: String){
        var user = Members(admin)
        database = FirebaseDatabase.getInstance().reference

        database.child("group_members").child(name).child(admin).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Write successful.", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            loading_1.visibility = View.GONE
            Toast.makeText(this, "Write not successful.", Toast.LENGTH_SHORT).show()
        }
        loading_1.visibility = View.GONE
    }


}
