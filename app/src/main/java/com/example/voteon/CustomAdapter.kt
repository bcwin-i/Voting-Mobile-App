package com.example.voteon

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class CustomAdapter(context: Context, image: ArrayList<String>, arrayList: ArrayList<HashMap<String, String>>) : BaseAdapter() {

    //Passing Values to Local Variables
    var image = image
    var arrayList = arrayList
    var context = context

    //Store image and arraylist in Temp Array List we Required it later
    var tempArrayList = ArrayList(image)
    var tempNameVersionList = ArrayList(arrayList)


    //Auto Generated Method
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var myview = convertView
        val holder: ViewHolder


        if (convertView == null) {

            //If Over View is Null than we Inflater view using Layout Inflater
            val mInflater = (context as Activity).layoutInflater

            //Inflating our list_row.
            myview = mInflater!!.inflate(R.layout.group_view_adapter, parent, false)

            //Create Object of ViewHolder Class and set our View to it
            holder = ViewHolder()

            //Find view By Id For all our Widget taken in list_row.

            /*Here !! are use for non-null asserted two prevent From null.
             you can also use Only Safe (?.)

            */
            holder.mImageView = myview!!.findViewById<ImageView>(R.id.group_profile) as ImageView
            holder.mHeader = myview!!.findViewById<TextView>(R.id.txt_group_name) as TextView
            holder.mSubHeader = myview!!.findViewById<TextView>(R.id.txt_group_tag) as TextView

            //Set A Tag to Identify our view.
            myview.tag = holder
        } else {
            //If Our View in not Null than Just get View using Tag and pass to holder Object.
            holder = myview!!.tag as ViewHolder
        }

        //Getting HasaMap At Particular Position
        val map = arrayList[position]

        //Setting image
        Picasso.get()
            .load(image[position])
            .into(holder.mImageView!!)

        //Setting name to TextView it's Key from HashMap At Position
        holder.mHeader!!.text = map["name"]

        //Setting version to TextView it's Key from HashMap At Position
        holder.mSubHeader!!.text = "${map["type"]}"


        return myview

    }

    //Auto Generated Method
    override fun getItem(p0: Int): Any {

        //Return the Current Position of ArrayList.
        return arrayList.get(p0)

    }

    //Auto Generated Method
    override fun getItemId(p0: Int): Long {
        return 0
    }

    //Auto Generated Method

    override fun getCount(): Int {

        //Return Size Of ArrayList
        return arrayList.size
    }


    //Create A class To hold over View like we taken in list_row.xml
    class ViewHolder {
        var mImageView: ImageView? = null
        var mHeader: TextView? = null
        var mSubHeader: TextView? = null
    }


    fun filter(text: String?) {
        //Our Search text
        val text = text!!.toLowerCase(Locale.getDefault())
        Log.d("Searched", text)

        //Here We Clear Both ArrayList because We update according to Search query.
        arrayList.clear()
        image.clear()


        if (text.length == 0) {
            Log.d("Searched", "0")
            /*If Search query is Empty than we add all temp data into our main ArrayList

            We store Value in temp in Starting of Program.

            */
            //arrayList.clear()

        } else {
            Log.d("Searched", tempNameVersionList.toString())
            for (i in 0..tempNameVersionList.size - 1) {
                Log.d("Searched", "1")
                /*
                If our Search query is not empty than we Check Our search keyword in Temp ArrayList.
                if our Search Keyword in Temp ArrayList than we add to our Main ArrayList
                */

                if (tempNameVersionList.get(i).get("name")!!.toLowerCase(Locale.getDefault()).contains(text)) {
                    Log.d("Searched", tempNameVersionList.get(i).get("name"))
                    image.add(tempArrayList.get(i))
                    arrayList.add(tempNameVersionList.get(i))
                }

            }
        }
        //This is to notify that data change in Adapter and Reflect the changes.
        notifyDataSetChanged()
    }


}