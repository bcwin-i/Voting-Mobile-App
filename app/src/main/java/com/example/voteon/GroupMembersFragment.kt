package com.example.voteon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class GroupMembersFragment : Fragment() {
    var group: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.group_members_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(arguments?.getString("group") != null){
            group = arguments?.getString("group")
            Toast.makeText(context, group, Toast.LENGTH_SHORT).show()
        }
        //locations_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }
}