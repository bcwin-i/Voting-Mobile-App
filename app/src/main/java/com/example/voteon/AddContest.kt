package com.example.voteon

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_add_contest.*
import kotlinx.android.synthetic.main.activity_add_group.*
import kotlinx.android.synthetic.main.set_contest_group.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class AddContest : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private lateinit var img: ImageView
    private lateinit var database: DatabaseReference
    private var CODE_IMG_GALLERY: Int = 1
    private var SAMPLE_CROPPED_IMG_NAME: String = "SampleCropImg"
    var pic_update : Int = 0
    var selectedProfileUri : Uri? = null
    private lateinit var auth: FirebaseAuth
    var group: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contest)

        if (this.intent.extras != null && this.intent.extras!!.containsKey("group")) {
            group = intent.extras?.get("group").toString()
            edt_group.setText(group)
            //edt_group.isFocusableInTouchMode = false
        }else{
            //edt_group.isFocusableInTouchMode = true
        }

        edt_group.setOnClickListener {
            if(group == null){
                addGroupDialog()
            }
        }

        toolbar = findViewById(R.id.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener{
            finish()
        }

        init()

        img.setOnClickListener(View.OnClickListener(){
            startActivityForResult(Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), CODE_IMG_GALLERY)
        })

        edt_duration.setOnClickListener {
            contestDurationDialog()
        }
    }

    private fun init() {
        this.img = findViewById(R.id.contest_image)
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
                loading_add_contest.visibility = View.VISIBLE
                var admin = auth.currentUser?.uid.toString()
                var name = edt_name.text.toString()
                var group = edt_group.text.toString()
                var duration = edt_duration.text.toString()
                var description = edt_contest_decription.text.toString()
                val date = GregorianCalendar().timeInMillis.toString()

                if(admin != "" && name != "" && group != "" && duration != "" && description != ""){
                    if (selectedProfileUri != null){
                        val ref = FirebaseStorage.getInstance().getReference("/ContestPhoto/$name")

                        ref.putFile(selectedProfileUri!!).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener {
                                var uri = it.toString()

                                if(uri != "") {
                                    //Toast.makeText(this, "Profile photo uploaded!", Toast.LENGTH_SHORT).show()
                                    writeNewContest(admin, name, group, duration ,description, date, uri)
                                }else
                                {
                                    loading_1.visibility = View.GONE
                                    Toast.makeText(this, "Error uploading", Toast.LENGTH_SHORT).show()
                                    val desertRef = FirebaseStorage.getInstance().getReference("ContestPhoto/$name.jpg")

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

                    }else{
                        writeNewContest(admin, name, group, duration ,description, date, "")
                    }
                }else Toast.makeText(this, "Fill all forms!", Toast.LENGTH_SHORT).show()

                return true
            }
            R.id.btn_discard ->{
                val output = Intent()
                output.putExtra("fragment", "0")
                output.putExtra("status", "0")
                setResult(Activity.RESULT_OK, output)
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

    private fun contestDurationDialog(){
        val dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog .setCancelable(false)
        dialog .setContentView(R.layout.set_contest_duration)
        dialog .show()

        val cancel = dialog .findViewById(R.id.cancel) as Button
        val select = dialog .findViewById(R.id.select) as Button
        var hour = dialog.findViewById(R.id.hour) as EditText
        var min = dialog.findViewById(R.id.mins) as EditText
        hour.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "24"))
        min.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "60"))
        cancel.setOnClickListener {
            dialog .dismiss()
        }
        select.setOnClickListener {
            var shours = hour.text.toString()
            var smins = min.text.toString()
            if (shours == "")shours = "00"
            if (smins == "")smins = "00"
            var hours = shours.toInt()
            var mins = smins.toInt()

            var total : Int = hours + mins
            if(total < 1){
                Toast.makeText(this, "Duration can't be 00:00!", Toast.LENGTH_SHORT).show()
            }else{
                if (hours == 0 && mins < 5){
                    Toast.makeText(this, "Duration must be above 00:05!", Toast.LENGTH_SHORT).show()
                }else{
                    if (hours == 24 && mins > 0){
                        Toast.makeText(this, "Duration can't be above 24:00!", Toast.LENGTH_SHORT).show()
                    }else{
                        if(hours.toString().length == 1) hours = "0$hours".toInt()
                        if(mins.toString().length == 1) mins = "0$mins".toInt()

                        var done = "$hours:$mins"
                        edt_duration.setText(done)
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun addGroupDialog(){
        val dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog .setCancelable(false)
        dialog .setContentView(R.layout.set_contest_group)
        dialog .show()
        dialog.groups_recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        userJoinedGroups(dialog)
    }

    fun userJoinedGroups(dialog: Dialog){
        auth = FirebaseAuth.getInstance()
        var uid = auth.currentUser?.uid
        var groups : ArrayList<String> = ArrayList()
        var ref = FirebaseDatabase.getInstance().reference.child("group_members").orderByChild(uid.toString())
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("sorted", "2")

                p0.children.forEach {
                    var groupName = it.key.toString()
                    //Log.d("joined", it.value.toString())
                    val data = it.value.toString().split(",").toTypedArray()
                    for(user_id in data){
                        val items = user_id.split("=").toTypedArray()
                        val id = items[0]
                        var rid = id?.replace("{", "")
                        var fid = rid?.replace(" ", "")
                        if(fid == uid)groups.add(groupName)
                        Log.d("joined", user_id)
                        Log.d("joined", rid)
                        Log.d("joined", groups.toString())
                    }
                }

                val adapter = GroupAdapter<GroupieViewHolder>()
                for (str in groups!!) {
                    //distance += "${map.get(str)}"
                    Log.d("groups", str)
                    val res = FirebaseDatabase.getInstance().getReference("groups").child(str)
                    res.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Log.d("sorted", "error")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val details = p0.getValue(Groups::class.java)
                            if(details != null) {
                                dialog.loading_dialog.visibility = View.GONE
                                adapter.add(GroupsDisplay(details))
                                dialog.groups_recycler.adapter = adapter
                            }
                        }
                    })
                }

                adapter.setOnItemClickListener { item, view ->
                    val file_detail = item as GroupsDisplay
                    //Toast.makeText(this@AddContest, file_detail.details.name, Toast.LENGTH_SHORT).show()
                    edt_group.setText(file_detail.details.name)
                    dialog.dismiss()
                }

                //Log.d("sorted", "List $distance")
            }


        })

    }

    private fun writeNewContest(admin: String, name: String, group: String, duration: String, description:String, date: String, uri: String) {
        val user = Contests(admin, date, description, duration, "", "", group, name, "Pending", uri)
        database = FirebaseDatabase.getInstance().reference

        database.child("contest").child(name).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Write successful.", Toast.LENGTH_SHORT).show()
            val output = Intent()
            output.putExtra("fragment", "0")
            output.putExtra("status", "1")
            setResult(Activity.RESULT_OK, output)
            finish()
        }.addOnFailureListener {
            loading_1.visibility = View.GONE
            Toast.makeText(this, "Write not successful.", Toast.LENGTH_SHORT).show()
        }
        //loading_1.visibility = View.GONE
    }



}
