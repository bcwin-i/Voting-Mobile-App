package com.example.voteon

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.activity_welcome.*


class Welcome : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var addtype: Int = 0
    private var startingTab: Int = 0
    private var updater: Int = 0
    var viewAdapter: ViewPager? = null

    private val tabIcons = intArrayOf(
        R.drawable.ic_good,
        R.drawable.ic_group,
        R.drawable.ic_member
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        toolbar = findViewById(R.id.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setSupportActionBar(toolbar)

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        setupViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                //viewPager.adapter!!.notifyDataSetChanged()
                if(updater != 0){
                    //Toast.makeText(this@Welcome, "updated with: "+updater.toString(), Toast.LENGTH_SHORT).show()
                    updater = 0
                    viewPager.adapter!!.notifyDataSetChanged()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        tabLayout = findViewById(R.id.tablayout) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
        setupTabIcons()
        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                addtype = position
                if(addtype != 0)search.visibility = View.VISIBLE
                else search.visibility = View.GONE
                //Toast.makeText(this@Welcome, addtype.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        add.setOnClickListener {
            if(addtype == 0){
                val intent = Intent(this, AddContest::class.java)
                startActivityForResult(intent, 0)
            }
            else if(addtype == 1){
                val intent = Intent(this, AddGroup::class.java)
                startActivity(intent)
            }
            else if(addtype == 2){

            }
        }
        search.setOnClickListener {
            if(addtype == 1){
                val intent = Intent(this, SearchView::class.java)
                intent.putExtra("type", "groups")
                startActivity(intent)
            }
            else if(addtype == 2){
                val intent = Intent(this, SearchView::class.java)
                intent.putExtra("type", "members")
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.welcome_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_profile -> {
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
                return true
            }
            R.id.btn_settings ->{
                Toast.makeText(applicationContext, "click on share", Toast.LENGTH_LONG).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            val status = data.getStringExtra("status")
            val frag = data.getStringExtra("fragment")
                if(status != "0"){
                    //fragment
                    startingTab = frag.toInt()
                    if(viewAdapter != null){
                        updater = 1
                        viewAdapter!!.setCurrentItem(startingTab, true)
                        //Toast.makeText(this, startingTab.toString(),Toast.LENGTH_SHORT).show()
                    }
                    //setupViewPager(viewUpdate!!)
                }
        }
    }


    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ContestFragment(), "Contest")
        adapter.addFragment(GroupsFragment(), "Groups")
        //adapter.addFragment(MemberFragment(), "Listed")
        viewPager.adapter = adapter
        viewAdapter = viewPager
    }

    private fun setupTabIcons() {
        tabLayout!!.getTabAt(0)!!.setIcon(tabIcons[0])
        tabLayout!!.getTabAt(1)!!.setIcon(tabIcons[1])
        //tabLayout!!.getTabAt(2)!!.setIcon(tabIcons[2])
    }

}
