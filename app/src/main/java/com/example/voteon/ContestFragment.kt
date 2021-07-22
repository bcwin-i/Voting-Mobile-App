package com.example.voteon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.contest_fragmet.*
import kotlinx.android.synthetic.main.contest_view_adapter.view.*

class ContestFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.contest_fragmet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contest_recycler.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        userGroupsContest()
    }

    fun userGroupsContest(){
        auth = FirebaseAuth.getInstance()
        var contest: ArrayList<String> = ArrayList()
        var uid = auth.currentUser?.uid
        var groups : ArrayList<String> = ArrayList()
        var ref = FirebaseDatabase.getInstance().reference.child("group_members").orderByChild(uid.toString())
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("sorted", "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("sorted", "2")
                if(p0.childrenCount < 1){
                    querying_contest!!.visibility = View.GONE
                    off_join.visibility = View.VISIBLE
                }else{
                    p0.children.forEach {
                        var groupName = it.key.toString()
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
                    displayGrid(groups)
                }

            }
        })

    }

    fun displayGrid(groups: ArrayList<String>){
        querying_contest!!.visibility = View.VISIBLE
        val adapter = GroupAdapter<GroupieViewHolder>()
        for (str in groups) {
            //distance += "${map.get(str)}"
            Log.d("groupsc", str)
            var ref = FirebaseDatabase.getInstance().reference.child("contest").orderByChild("group").equalTo(str)
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("sorted", "error")
                }

                override fun onDataChange(it: DataSnapshot) {
                    //Log.d("keyname",it.value.toString())
                    if(it.childrenCount < 1){
                        querying_contest!!.visibility = View.GONE
                        off_join.visibility = View.VISIBLE
                    }
                    else{
                        it.children.forEach {
                            querying_contest!!.visibility = View.GONE
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
        }

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

class ContestDisplayClass(val details: ContestDataClass, private val poster: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.contest_view_adapter
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        var profile = viewHolder.itemView.contest_image
        var status = details.status
        if(details.status == "off")status = "Pending"

        if (details.uri != ""){
            Picasso.get()
                .load(details.uri)
                .into(profile, object : Callback {
                    override fun onSuccess() {
                        viewHolder.itemView.txt_contest_name.text = details.name
                        viewHolder.itemView.txt_status.text = status
                    }
                    override fun onError(e: Exception?) {
                        //Log.d(TAG, "error")
                    }
                })
        }else{
            viewHolder.itemView.txt_contest_name.text = details.name
            viewHolder.itemView.txt_status.text = status
        }

        var admin = viewHolder.itemView.admin_profile as ImageView

        if(poster != ""){
            Picasso.get().load(poster).into(admin)
        }
    }

}