package com.example.voteon

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.contest_fragmet.*
import kotlinx.android.synthetic.main.group_contest_fragment.*
import kotlinx.android.synthetic.main.group_contest_fragment.contest_recycler
import kotlinx.android.synthetic.main.group_contest_fragment.off_join
import kotlinx.android.synthetic.main.group_contest_fragment.querying_contest

class GroupContestFragment : Fragment() {
    var group: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.group_contest_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(arguments?.getString("group") != null){
            group = arguments?.getString("group")
            displayGrid(group!!)
        }
        btn_add_contest.setOnClickListener {
            val intent = Intent(context, AddContest::class.java)
            intent.putExtra("group", group)
            startActivity(intent)
        }
        //locations_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        contest_recycler.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
    }


    fun displayGrid(groups: String){
        querying_contest.visibility = View.VISIBLE
        val adapter = GroupAdapter<GroupieViewHolder>()

            var ref = FirebaseDatabase.getInstance().reference.child("contest").orderByChild("group").equalTo(groups)
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("sorted", "error")
                }

                override fun onDataChange(it: DataSnapshot) {
                    //Log.d("keyname",it.value.toString())
                    if(it.childrenCount < 1){
                        querying_contest.visibility = View.GONE
                        off_join.visibility = View.VISIBLE
                    }
                    else{
                        it.children.forEach {
                            querying_contest.visibility = View.GONE
                            val details = it.getValue(ContestDataClass::class.java)
                            //Log.d("keynames",details!!.name)
                            if(details != null) {
                                //var poster = posterPhoto(details.admin)
                                var poster = ""
                                var ref = FirebaseDatabase.getInstance().getReference("users").child(details.admin)

                                val postListener = object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        // Get Post object and use the values to update the UI
                                        val post = dataSnapshot.getValue(User_detail::class.java)
                                        // [START_EXCLUDE]
                                        post?.let {
                                            poster = post.uri.toString()
                                            adapter.add(ContestDisplayClass(details, poster))
                                            contest_recycler.adapter = adapter
                                        }
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {
                                    }

                                }
                                ref.addValueEventListener(postListener)

                            }
                        }
                    }
                }
            })


        adapter.setOnItemClickListener { item, view ->
            val file_detail = item as ContestDisplayClass
            val intent = Intent(view.context, ContestView::class.java)
            intent.putExtra("name", file_detail.details.name)
            intent.putExtra("duration", file_detail.details.duration)
            intent.putExtra("end", file_detail.details.end)
            intent.putExtra("group", file_detail.details.group)
            intent.putExtra("endDate", file_detail.details.endDate)
            intent.putExtra("status", file_detail.details.status)
            intent.putExtra("description", file_detail.details.description)
            var profile = file_detail.details.uri
            if(profile == "") profile = ""
            intent.putExtra("uri", profile)
            intent.putExtra("admin", file_detail.details.admin)
            intent.putExtra("created", file_detail.details.created)

            startActivity(intent)
        }
    }


}