package com.example.voteon

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.android.synthetic.main.group_fragment.*
import kotlinx.android.synthetic.main.group_view_adapter.view.*

class GroupsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.group_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //groups_recycler.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        groups_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        userJoinedGroups()
    }

    fun userJoinedGroups(){
        querying.visibility = View.VISIBLE
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
                if(p0.childrenCount < 1){
                    querying.visibility = View.GONE
                    off_join.visibility = View.VISIBLE
                }else{
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
                        //Log.d("groups", str)
                        val res = FirebaseDatabase.getInstance().getReference("groups").child(str)
                        res.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                Log.d("sorted", "error")
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                querying.visibility = View.GONE
                                val details = p0.getValue(Groups::class.java)
                                if(details != null) {
                                    adapter.add(GroupsDisplay(details))
                                    groups_recycler.adapter = adapter
                                }
                            }
                        })
                    }

                    adapter.setOnItemClickListener { item, view ->
                        val file_detail = item as GroupsDisplay
                        val intent = Intent(view.context, GroupView::class.java)
                        intent.putExtra("name", file_detail.details.name)
                        intent.putExtra("tag", file_detail.details.tag)
                        intent.putExtra("description", file_detail.details.description)
                        var profile = file_detail.details.uri
                        if(profile == "") profile = ""
                        intent.putExtra("uri", profile)
                        intent.putExtra("admin", file_detail.details.admin)
                        intent.putExtra("date", file_detail.details.date)

                        startActivity(intent)
                    }
                }


                //Log.d("sorted", "List $distance")
            }


        })

    }
}

class GroupsDisplay(val details: Groups): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.group_view_adapter
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        var profile = viewHolder.itemView.group_profile

        if (details.uri != ""){
            Picasso.get()
                .load(details.uri)
                .into(profile, object : Callback {
                    override fun onSuccess() {
                        viewHolder.itemView.txt_group_name.text = details.name
                        viewHolder.itemView.txt_group_tag.text = details.tag
                    }
                    override fun onError(e: Exception?) {
                        //Log.d(TAG, "error")
                    }
                })
        }else{
            viewHolder.itemView.txt_group_name.text = details.name
            viewHolder.itemView.txt_group_tag.text = details.tag
        }
    }

}