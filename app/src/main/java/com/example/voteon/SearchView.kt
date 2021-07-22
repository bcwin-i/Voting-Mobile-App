package com.example.voteon

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_add_contest.*
import kotlinx.android.synthetic.main.activity_add_group.*
import kotlinx.android.synthetic.main.activity_search_view.*
import kotlinx.android.synthetic.main.group_access_dialog.*
import kotlinx.android.synthetic.main.group_fragment.*
import java.util.HashMap

class SearchView : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var images: ArrayList<String> = ArrayList()
    private var name: ArrayList<String> = ArrayList()
    private var tag: ArrayList<String> = ArrayList()
    val info = ArrayList<HashMap<String, String>>()
    var hashMap: HashMap<String, String> = HashMap<String, String>()
    private var status: Int = 0
    var customAdapter : CustomAdapter? = null
    private lateinit var auth: FirebaseAuth
    var groups : ArrayList<String> = ArrayList()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener{
            finish()
        }
        val bundle = intent.extras
        var type = bundle?.get("type").toString()
        if(type == "groups"){
            querylist()
        }else{
            querylistMembers()
        }

        userJoinedGroups()
        search_list_row.visibility = View.GONE
        search_list_row.setOnItemClickListener { adapterView, view, position, l ->
            //Provide the data on Click position in our listview
            val hashMap: HashMap<String, String> = customAdapter!!.getItem(position) as HashMap<String, String>
            //Toast.makeText(this, "Name : " + hashMap.get("name") + "\nVersion : " + hashMap.get("type"), Toast.LENGTH_LONG).show()
            var total = 0
            Log.d("access",groups.toString())
            if(groups.size != 0) {
                var joined = 0
                loop@ for (str in groups!!) {
                    Log.d("access", str + " : " + hashMap.get("name"))
                    total += 1
                    if (hashMap.get("name") == str) {
                        val intent = Intent(this, GroupView::class.java)
                        intent.putExtra("name", str)
                        intent.putExtra("search", "1")
                        startActivity(intent)
                        break@loop
                    }
                    if (groups.size == total) {
                        groupAccess(hashMap.get("name")!!)
                    }
                }
            }else groupAccess(hashMap.get("name")!!)
        }

        if (this.intent.extras != null && this.intent.extras!!.containsKey("type")) {
            val bundle = intent.extras
            var type = bundle?.get("type").toString()

            searchItem.hint = "search $type"

            Toast.makeText(this, type, Toast.LENGTH_SHORT).show()

            searchItem.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if(status != 0) {
                        search_list_row.visibility = View.VISIBLE
                        search_status.text = "search results.."
                        customAdapter!!.filter(s.toString())
                    }
                    //calculatePasswordStrength(s.toString())
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }

    }

    private fun querylist(){
        val res = FirebaseDatabase.getInstance().getReference("groups")
        res.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                var total = 0
                p0.children.forEach {
                    val details = it.getValue(Groups::class.java)
                    if (details != null) {
                        images.add(details.uri.toString())
                        name.add(details.name.toString())
                        tag.add(details.tag.toString())

                        hashMap = HashMap<String, String>()
                        hashMap["name"] = details.name.toString()
                        hashMap["type"] = details.tag.toString()

                        info.add(hashMap)
                        total += 1
                        if (total == p0.childrenCount.toInt()){
                            search_status.text = "search available.."
                            customAdapter = CustomAdapter(this@SearchView, images, info)
                            search_list_row.adapter = customAdapter
                            status = 1
                            //Toast.makeText(this@SearchView, name.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun querylistMembers(){
        val res = FirebaseDatabase.getInstance().getReference("users")
        res.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                var total = 0
                p0.children.forEach {
                    val details = it.getValue(User_detail::class.java)
                    if (details != null) {
                        images.add(details.uri.toString())
                        name.add(details.name.toString())
                        //tag.add(details.tag.toString())

                        hashMap = HashMap<String, String>()
                        hashMap["name"] = details.name.toString()
                        hashMap["type"] = ""

                        info.add(hashMap)
                        total += 1
                        if (total == p0.childrenCount.toInt()){
                            search_status.text = "search available.."
                            customAdapter = CustomAdapter(this@SearchView, images, info)
                            search_list_row.adapter = customAdapter
                            status = 1
                            //Toast.makeText(this@SearchView, name.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    fun userJoinedGroups(){
        //querying.visibility = View.VISIBLE
        auth = FirebaseAuth.getInstance()
        var uid = auth.currentUser?.uid
        var ref = FirebaseDatabase.getInstance().reference.child("group_members").orderByChild(uid.toString())
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("sorted", "2")

                groups = ArrayList()
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
            }


        })

    }

    private fun groupAccess(name: String){
        val dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog .setCancelable(false)
        dialog .setContentView(R.layout.group_access_dialog)
        val cancel = dialog .findViewById(R.id.cancel) as Button
        val select = dialog .findViewById(R.id.select) as Button
        var pin = dialog.findViewById(R.id.pin) as EditText
        dialog.access_dialog_groupname.text = name
        cancel.setOnClickListener {
            dialog .dismiss()
        }
        select.setOnClickListener {
            dialog.error_msg.visibility = View.GONE
            if(pin.text.toString() == ""){
                Toast.makeText(this, "Pin code must be four letters!", Toast.LENGTH_SHORT).show()
            }
            else{
                dialog.loading_dialog.visibility = View.VISIBLE
                var total = 0
                Log.d("access",groups.toString())
                if(groups.size != 0){
                    for (str in groups!!) {
                        Log.d("access",str+" : "+name)
                        total += 1
                        var joined = 0
                        if(name == str) joined == 1
                        if(groups.size == total){
                            if(joined != 0){
                                val intent = Intent(this, GroupView::class.java)
                                intent.putExtra("name", name)
                                intent.putExtra("search", "1")
                                startActivity(intent)
                            }
                            else{
                                val res = FirebaseDatabase.getInstance().getReference("groups").child(name)
                                res.addListenerForSingleValueEvent(object: ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        Log.d("sorted", "error")
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val details = p0.getValue(Groups::class.java)
                                        if(details != null) {
                                            if(details.code != pin.text.toString()){
                                                dialog.loading_dialog.visibility = View.GONE
                                                dialog.error_msg.visibility = View.VISIBLE
                                                dialog.error_msg.text = "wrong pin"
                                            }else{
                                                writeGroupMembers(name, uid)
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    }
                }else{
                    dialog.loading_dialog.visibility = View.VISIBLE
                    val res = FirebaseDatabase.getInstance().getReference("groups").child(name)
                    res.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Log.d("sorted", "error")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val details = p0.getValue(Groups::class.java)
                            if (details != null) {
                                if (details.code != pin.text.toString()) {
                                    dialog.loading_dialog.visibility = View.GONE
                                    dialog.error_msg.visibility = View.VISIBLE
                                    dialog.error_msg.text = "wrong pin"
                                } else {
                                    writeGroupMembers(name, uid)
                                }
                            }
                        }
                    })
                }

            }
        }
        dialog .show()
    }

    private fun writeGroupMembers(name: String, admin: String){
        var user = Members(admin)
        database = FirebaseDatabase.getInstance().reference

        database.child("group_members").child(name).child(admin).setValue(user).addOnSuccessListener {
            val intent = Intent(this@SearchView, GroupView::class.java)
            intent.putExtra("name", name)
            intent.putExtra("search", "1")
            startActivity(intent)
        }.addOnFailureListener {
            Toast.makeText(this, "Write not successful.", Toast.LENGTH_SHORT).show()
        }
        //loading_1.visibility = View.GONE
    }
}
