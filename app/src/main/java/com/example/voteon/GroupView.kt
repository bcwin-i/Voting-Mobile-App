package com.example.voteon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_group_view.*
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.group_fragment.*

class GroupView : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var addtype: Int = 0
    var groupname: String? = null

    private val tabIcons = intArrayOf(
        R.drawable.ic_good,
        R.drawable.ic_member,
        R.drawable.ic_list
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener{
            finish()
        }

        if (this.intent.extras != null && this.intent.extras!!.containsKey("admin")) {
            val bundle = intent.extras
            var name = bundle?.get("name").toString()
            var tag = bundle?.get("tag").toString()
            var description = bundle?.get("description").toString()
            var uri = bundle?.get("uri").toString()
            var admi = bundle?.get("admin").toString()
            var date = bundle?.get("date").toString()
            searchMembers(name)

            toolbar!!.title = name
            toolbar!!.subtitle = tag
            txt_group_description.text = description

            if(uri != ""){
                Picasso.get()
                    .load(uri)
                    .into(group_profile)
            }
            groupname = name
        }
        if (this.intent.extras != null && this.intent.extras!!.containsKey("search")) {
            val bundle = intent.extras
            var name = bundle?.get("name").toString()
            searchDetails(name)
        }

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        setupViewPager(viewPager)

        tabLayout = findViewById(R.id.tablayout) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
        setupTabIcons()

    }


    private fun setupViewPager(viewPager: ViewPager) {
        val bundle = Bundle()
        var fragment1 = GroupContestFragment()
        bundle.putString("group", groupname)
        fragment1.arguments = bundle

        var fragment2 = GroupMembersFragment()
        bundle.putString("group", groupname)
        fragment2.arguments = bundle

        var fragment3 = GroupTopicsFragment()
        bundle.putString("group", groupname)
        fragment3.arguments = bundle
        val adapter = GroupViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(fragment1, "")
        adapter.addFragment(fragment2, "")
        adapter.addFragment(fragment3, "")
        viewPager.adapter = adapter
    }

    private fun setupTabIcons() {
        tabLayout!!.getTabAt(0)!!.setIcon(tabIcons[0])
        tabLayout!!.getTabAt(1)!!.setIcon(tabIcons[1])
        tabLayout!!.getTabAt(2)!!.setIcon(tabIcons[2])
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.group_view_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_decide -> {

                return true
            }
            R.id.btn_manage -> {

                return true
            }
            R.id.btn_edit ->{

                return true
            }
            R.id.btn_details ->{

                return true
            }
            R.id.btn_delete ->{

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun searchDetails(name: String){
        val res = FirebaseDatabase.getInstance().getReference("groups").child(name)
        res.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val details = p0.getValue(Groups::class.java)
                txt_total_contest.text = p0.childrenCount.toString()
                if(details != null) {
                    toolbar!!.title = details.name
                    toolbar!!.subtitle = details.tag
                    txt_group_description.text = details.description

                    if(details.uri != ""){
                        Picasso.get()
                            .load(details.uri)
                            .into(group_profile)
                    }
                    groupname = name
                }
            }
        })
    }

    private fun contest(name: String){
        val res = FirebaseDatabase.getInstance().getReference("contest").orderByChild("group").equalTo(name)
        res.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val details = p0.getValue(Groups::class.java)
                txt_total_contest.text = p0.childrenCount.toString()
            }
        })
    }

    private fun searchMembers(name: String){
        val res = FirebaseDatabase.getInstance().getReference("group_members").child(name)
        res.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val details = p0.getValue(Groups::class.java)
                txt_total_members.text = p0.childrenCount.toString()
            }
        })
    }
}
