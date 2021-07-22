package com.example.voteon

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_add_group.*
import kotlinx.android.synthetic.main.activity_contest_view.*
import kotlinx.android.synthetic.main.add_options_dialog.loading_add_option
import kotlinx.android.synthetic.main.complete_option_vote_dialog.*
import kotlinx.android.synthetic.main.options_view_adapter.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private var totalChecked = 0
private val listen : MutableLiveData<String> =  MutableLiveData<String>()
private val choice : ArrayList<String> = ArrayList()
private val choiceUri : ArrayList<String> = ArrayList()
private var switch : Int = 0
var uid = FirebaseAuth.getInstance().currentUser!!.uid
private var voted: Int = 0
private var contestStatus: String? = null
private var contestName: String? = null


class ContestView : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var addImage: ImageView? = null
    private var addDialog: Dialog? = null
    private var CODE_IMG_GALLERY: Int = 1
    private var SAMPLE_CROPPED_IMG_NAME: String = "SampleCropImg"
    var pic_update : Int = 0
    var selectedProfileUri : Uri? = null
    var optionToatal : Int? = null
    var contestAdmin: String? = null
    var durationr: String? = null

    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contest_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener{
            finish()
        }

        contest_options_recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        if (this.intent.extras != null && this.intent.extras!!.containsKey("name")) {
            val bundle = intent.extras
            var name = bundle?.get("name").toString()
            contestName = name
            totalOptions()
            var status = bundle?.get("status").toString()
            var description = bundle?.get("description").toString()
            var uri = bundle?.get("uri").toString()
            var admin = bundle?.get("admin").toString()
            var endDate = bundle?.get("endDate").toString()
            contestAdmin = admin
            var duration = bundle?.get("duration").toString()
            durationr = duration
            var date = bundle?.get("created").toString()
            if(status == "off")status = "Pending"
            contestStatus = status
            txt_contest_name.text = name
            txt_contest_description.text = description

            //Setting posting date
            //Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = date.toLong()

            val mYear: Int = calendar.get(Calendar.YEAR)
            var mMonth: Int = calendar.get(Calendar.MONTH)
            mMonth++
            val mDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
            val mHour: Int = calendar.get(Calendar.HOUR_OF_DAY)
            val mMins: Int = calendar.get(Calendar.MINUTE)

            txt_contest_date.text = "Created : $mDay-$mMonth-$mYear $mHour:$mMins"




            if(FirebaseAuth.getInstance().currentUser!!.uid == admin){
                if(status != "Active" || status != "Complete"){
                    add_contest_option.visibility = View.VISIBLE
                }
            }
            val sdf = SimpleDateFormat("yyyy:MM:dd - HH:mm")
            val currentDateandTime = sdf.format(Date())
            val edate: Calendar = Calendar.getInstance()
            var endi: Date? = null
            if(endDate != ""){
                edate.timeInMillis = endDate.toLong()
                endi = edate.time
            }

            if(status == "Active"){
                // || status != "Complete"
                add_contest_option.visibility = View.GONE

                val items = durationr!!.split(":")
                val hour = items[0].toInt()
                val mins = items[1].toInt()

                val date2 = sdf.parse(currentDateandTime)
                val calendar2 = Calendar.getInstance()
                calendar2.time = date2

                val date = sdf.parse(currentDateandTime)
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.HOUR, hour)
                calendar.add(Calendar.MINUTE, mins)


                contest_timer.text = "    Ending : ${sdf.format(endi)}"
                if(date < date2) {
                    Log.d("appDate", "Date1 is after Date2")
                }else{
                    Log.d("appDate", "Date1 is before Date2")
                }

                //val userDob = SimpleDateFormat("yyyy-MM-dd").parse(sdf.format(calendar.time))
                val today = Date()
                val diff = endi!!.time - today.time
                val numOfDays = (diff / (1000 * 60 * 60 * 24)).toInt()
                val hours = (diff / (1000 * 60 * 60)).toInt()
                var minutes = (diff / (1000 * 60)).toInt()
                val seconds = (diff / 1000).toInt()
                var lef = ""

                lef = "   ${minutes} minutes left"

                contest_timer.text = lef

                val h = Handler()
                h.postDelayed(object : Runnable {
                    private val time: Long = 0
                    override fun run() {
                        minutes -= 1
                        if(minutes <= 0){
                            var ref = FirebaseDatabase.getInstance().getReference("contest")
                            ref.child(contestName!!).child("status").setValue("Ended")
                                .addOnSuccessListener {
                                    finish()
                                    startActivity(intent)
                                }
                        }
                    }
                }, 60000)

                //Log.d("appDate", "Time left: $hours : $minutes")

            }else if(status == "Ended"){
                //Ending addidition algorithm
                add_contest_option.visibility = View.GONE
                contest_timer.text = "   Ended : ${sdf.format(endi!!)}"
            }

            if(uri != ""){
                Picasso.get()
                    .load(uri)
                    .into(contest_profile)
            }
            voted()
        }

        add_contest_option.setOnClickListener {
            showAddOption()
        }

        listen.observe(this, androidx.lifecycle.Observer {
            //Toast.makeText(this, choice.toString(), Toast.LENGTH_SHORT).show()
            if(choice.size == 1 && switch == 1){
                showCompleteVoteDialog(choice[0], choiceUri[0])
            }
            if(choice.size > 1){
                Toast.makeText(this, "Multiple option select is not allowed.", Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        if(uid == contestAdmin){
            val inflater = menuInflater
            inflater.inflate(R.menu.contest_view_menu, menu)
            val start = menu.getItem(0)
            val delete = menu.getItem(2)
            if(contestStatus == "Active" || contestStatus == "Ended"){
                start.isVisible = false
                delete.isVisible = false
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_start_contest -> {
                if(optionToatal!! < 2){
                    Toast.makeText(this, "A contest needs two options.", Toast.LENGTH_SHORT).show()
                }
                else{
                    val items = durationr!!.split(":")
                    val hour = items[0].toInt()
                    val mins = items[1].toInt()

                    val sdf = SimpleDateFormat("yyyy:MM:dd - HH:mm")
                    val currentDateandTime = sdf.format(Date())

                    val date2 = sdf.parse(currentDateandTime)
                    val calendar2 = Calendar.getInstance()
                    calendar2.time = date2

                    var dateFirst: Date = calendar2.time

                    val date = sdf.parse(currentDateandTime)
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.add(Calendar.HOUR, hour)
                    calendar.add(Calendar.MINUTE, mins)

                    var dateSecond : Date = calendar.time
                    var update : String = calendar.time.time.toString()
                    var datef: Date = calendar.time

                    contest_timer.text = "    Ending : ${sdf.format(datef)}"
                    if(date < date2) {
                        Log.d("appDate", "Date1 is after Date2")
                    }else{
                        Log.d("appDate", "Date1 is before Date2")
                    }

                    //val userDob = SimpleDateFormat("yyyy-MM-dd").parse(sdf.format(calendar.time))
                    val today = Date()
                    val diff = dateSecond.time - dateFirst.time
                    val numOfDays = (diff / (1000 * 60 * 60 * 24)).toInt()
                    val hours = (diff / (1000 * 60 * 60)).toInt()
                    val minutes = (diff / (1000 * 60)).toInt()
                    val seconds = (diff / 1000).toInt()
                    var ref = FirebaseDatabase.getInstance().getReference("contest")
                    ref.child(contestName!!).child("endDate").setValue(dateSecond.time.toString())
                    ref.child(contestName!!).child("status").setValue("Active")
                        .addOnSuccessListener {
                            finish()
                            startActivity(intent)
                        }

                    Log.d("appDate", "Time left: $hours : $minutes")
                }
                return true
            }
            R.id.btn_delete_contest ->{
                Toast.makeText(applicationContext, "click on share", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.btn_contest_details ->{

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun showAddOption(){
        val dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog .setCancelable(false)
        dialog .setContentView(R.layout.add_options_dialog)
        val cancel = dialog .findViewById(R.id.cancel) as Button
        val select = dialog .findViewById(R.id.select) as Button
        val image = dialog.findViewById(R.id.option_image) as ImageView
        var name = dialog.findViewById(R.id.edt_name) as EditText
        addImage = image
        addDialog = dialog
        dialog.show()

        image.setOnClickListener {
            startActivityForResult(Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), CODE_IMG_GALLERY)
        }
        cancel.setOnClickListener {
            dialog .dismiss()
        }
        select.setOnClickListener {
            if(name.text.toString() != ""){
                if (contestName == null)Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show()
                else{
                    if(optionToatal!! > 10) Toast.makeText(this, "Total number of options exceeded", Toast.LENGTH_SHORT).show()
                    else{
                        var position = optionToatal!! + 1
                        var pos = position.toString()
                        dialog.loading_add_option.visibility = View.VISIBLE
                        if(selectedProfileUri != null){
                            val ref = FirebaseStorage.getInstance().getReference("/OptionPhoto/$name")

                            ref.putFile(selectedProfileUri!!).addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener {
                                    var uri = it.toString()

                                    if(uri != "") {
                                        //Toast.makeText(this, "Profile photo uploaded!", Toast.LENGTH_SHORT).show()
                                        writeNewContestOtion(name.text.toString(),uri, pos)
                                    }else
                                    {
                                        loading_1.visibility = View.GONE
                                        Toast.makeText(this, "Error uploading", Toast.LENGTH_SHORT).show()
                                        val desertRef = FirebaseStorage.getInstance().getReference("OptionPhoto/$name.jpg")

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
                            writeNewContestOtion(name.text.toString(),"", pos)
                        }
                    }

                }
            }else{
                Toast.makeText(this, "Option name can't be empty!", Toast.LENGTH_SHORT).show()
            }
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
                //addImage!!.setClipToOutline(true)
                addImage!!.setImageURI(imageResourceCrop)
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

        uCrop.withMaxResultSize(450,450)

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

    private fun totalOptions(){
        var ref = FirebaseDatabase.getInstance().getReference("options").child(contestName!!)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val size = dataSnapshot.childrenCount.toInt()
                optionToatal = size
                Toast.makeText(this@ContestView, optionToatal.toString(), Toast.LENGTH_SHORT).show()

            }
            override fun onCancelled(databaseError: DatabaseError) {
            }

        }
        ref.addValueEventListener(postListener)
    }

    private fun writeNewContestOtion(name: String, uri: String, position: String){
        val user = Options(name, position, uri)
        database = FirebaseDatabase.getInstance().reference

        database.child("options").child(contestName!!).child(name).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Write successful.", Toast.LENGTH_SHORT).show()
            //finish()
            addDialog!!.dismiss()
            viewContestOptions()
        }.addOnFailureListener {
            loading_1.visibility = View.GONE
            Toast.makeText(this, "Write not successful.", Toast.LENGTH_SHORT).show()
        }
        //loading_1.visibility = View.GONE
    }

    private fun voted(){
        val rev = FirebaseDatabase.getInstance().getReference("choice").child(contestName!!)
        rev.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(pv: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(pv: DataSnapshot) {
                var votes = 0
                var total = 0
                var checked = ""
                pv.children.forEach {
                    total += 1
                    if(it.key == uid)voted = 1
                    if (total == pv.childrenCount.toInt()){
                        viewContestOptions()
                    }
                }

            }

        })
    }

    private fun viewContestOptions(){
        var groups : ArrayList<String> = ArrayList()
        var ref = FirebaseDatabase.getInstance().reference.child("options").child(contestName!!)
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("sorted", "2")

                p0.children.forEach {
                    var groupName = it.key.toString()
                    groups.add(groupName)
                }

                val adapter = GroupAdapter<GroupieViewHolder>()
                for (str in groups!!) {
                    //distance += "${map.get(str)}"
                    //Log.d("groups", str)
                    val res = FirebaseDatabase.getInstance().getReference("options").child(contestName!!).child(str).orderByChild("position")
                    res.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Log.d("sorted", "error")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.childrenCount.toInt() != 0){
                                txt_options_total.setTextColor(Color.parseColor("#293556"))
                                txt_options_total.text = "contest options total ${groups.size}"
                            }
                            val details = p0.getValue(OptionsDataClass::class.java)
                            //Log.d("keynames",details!!.name)
                            if(details != null) {
                                val rev = FirebaseDatabase.getInstance().getReference("choice").child(contestName!!)
                                rev.addListenerForSingleValueEvent(object: ValueEventListener {
                                    override fun onCancelled(pv: DatabaseError) {
                                        Log.d("sorted", "error")
                                    }

                                    override fun onDataChange(pv: DataSnapshot) {
                                        if(pv.childrenCount == 0.toLong()){
                                            adapter.add(OptionsDisplay(details, 0, 0, "0"))
                                            contest_options_recycler.adapter = adapter
                                        }
                                        else{
                                            var votes = 0
                                            var total = 0
                                            var checked = ""
                                            pv.children.forEach {
                                                total += 1
                                                if(details.name == it.value.toString())votes += 1
                                                //Log.d("totalChildren", "${it.key.toString()} : ${FirebaseAuth.getInstance().currentUser!!.uid}")
                                                if(it.key.toString() == uid)checked = it.value.toString()
                                                if (total == pv.childrenCount.toInt()){
                                                    adapter.add(OptionsDisplay(details, votes, pv.childrenCount.toInt(), checked))
                                                    contest_options_recycler.adapter = adapter
                                                }
                                            }

                                        }
                                    }
                                })
                            }
                        }
                    })
                }

                adapter.setOnItemClickListener { item, view ->
                    val file_detail = item as OptionsDisplay
                }

                //Log.d("sorted", "List $distance")
            }


        })


    }

    private fun showCompleteVoteDialog(it:String, uri: String){
        if(contestStatus != "Pending" && contestStatus != "ended"){
            val dialog = Dialog(this)
            dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
            //dialog .setCancelable(false)
            dialog .setContentView(R.layout.complete_option_vote_dialog)
            dialog.c_txt_option_name.text = it
            Picasso.get()
                .load(uri)
                .into(dialog.c_option_image as ImageView)
            dialog.show()

            val cancel = dialog .findViewById(R.id.cancel) as Button
            val select = dialog .findViewById(R.id.select) as Button
            select.setOnClickListener {
                submitVote()
            }
            cancel.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    private fun submitVote(){
        database = FirebaseDatabase.getInstance().reference

        database.child("choice").child(contestName!!).child(uid).setValue(choice[0]).addOnSuccessListener {
            Toast.makeText(this, "Write successful.", Toast.LENGTH_SHORT).show()
            viewContestOptions()
        }.addOnFailureListener {
            loading_1.visibility = View.GONE
            Toast.makeText(this, "Write not successful.", Toast.LENGTH_SHORT).show()
        }
    }
}


class OptionsDisplay(val details: OptionsDataClass, val votes: Int, val total: Int, val checked: String): Item<GroupieViewHolder>(){
    var percentage: Float = 0f
    override fun getLayout(): Int {
        return R.layout.options_view_adapter
    }
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if(votes != 0 && total != 0){
            percentage = ((votes*100)/total).toFloat()
            //var newP: Long = (votes/total).toLong()
            //newP *= 100

            val params: LinearLayout.LayoutParams = viewHolder.itemView.percentage_bar.layoutParams as LinearLayout.LayoutParams
            params.weight = percentage
            viewHolder.itemView.percentage_bar.setLayoutParams(params)
            Log.d("Option_click", "$votes/$total=$percentage"+" "+details.name)
        }
        Log.d("totalChildren", checked+" "+details.name)
        if(checked == details.name){
            viewHolder.itemView.option_check.isChecked = true
            viewHolder.itemView.option_check.isClickable = false
            voted = 1
        }
        if(voted == 1 || contestStatus == "Pending" || contestStatus == "ended")viewHolder.itemView.option_check.isClickable = false

        //Log.d("totalChildren", checked)
        var profile = viewHolder.itemView.option_image_view as ImageView

        if (details.uri != ""){
            Picasso.get()
                .load(details.uri)
                .into(profile, object : Callback {
                    override fun onSuccess() {
                        //profile.clipToOutline = true
                        viewHolder.itemView.txt_option_name.text = details.name
                        viewHolder.itemView.option_progress.text = "$votes votes, ${percentage.toInt()}%"
                    }
                    override fun onError(e: Exception?) {
                        //Log.d(TAG, "error")
                    }
                })
        }else{
            viewHolder.itemView.txt_option_name.text = details.name
            viewHolder.itemView.option_progress.text = "$votes votes, ${percentage.toInt()}%"
        }

        profile.setOnClickListener {
           Log.d("Option_click", details.name)
        }

        val h = Handler()
        h.postDelayed(object : Runnable {
            private val time: Long = 0
            override fun run() {
                update(viewHolder)
            }
        }, 30000)

        viewHolder.itemView.option_check.setOnCheckedChangeListener { buttonView, isChecked ->
            val r = Random()
            //listen.value = details.name
            val i1: Int = r.nextInt(45 - 28) + 28
            if(viewHolder.itemView.option_check.isChecked) {
                totalChecked += 1
                choice.add(details.name)
                choiceUri.add(details.uri!!)
                switch = 1
                listen.value = i1.toString()
            }
            if(!viewHolder.itemView.option_check.isChecked && totalChecked != 0){
                totalChecked -= 1
                choice.remove(details.name)
                choiceUri.remove(details.uri!!)
                switch = 0
                listen.value = i1.toString()
            }
            Log.d("Option_click", totalChecked.toString())

        }
    }

    private fun update(viewHolder: GroupieViewHolder){
        val rev = FirebaseDatabase.getInstance().getReference("choice").child(contestName!!).orderByValue().equalTo(details.name!!)
        rev.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(pv: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(pv: DataSnapshot) {
                if(pv.childrenCount.toInt() != votes){
                    val params: LinearLayout.LayoutParams = viewHolder.itemView.percentage_bar.layoutParams as LinearLayout.LayoutParams
                    params.weight = percentage
                    viewHolder.itemView.percentage_bar.setLayoutParams(params)

                    viewHolder.itemView.option_progress.text = "$votes votes, ${percentage.toInt()}%"
                }
            }
        })
    }

}