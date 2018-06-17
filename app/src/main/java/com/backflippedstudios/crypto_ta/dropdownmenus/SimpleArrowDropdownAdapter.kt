package com.backflippedstudios.crypto_ta.dropdownmenus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.backflippedstudios.crypto_ta.R

/**
 * Created by C0rbin on 12/2/2017.
 */
class SimpleArrowDropdownAdapter(context: Context?, resource: Int, objects: ArrayList<String>) : ArrayAdapter<String>(context, resource, objects) {
    private val mInflator: LayoutInflater = LayoutInflater.from(context)

    var list: ArrayList<String> = ArrayList()
    
    init {
        list = objects
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View
        if (convertView == null) {
            view = this.mInflator.inflate(R.layout.spinner_dropdown_main_view_with_arrow,parent,false)
        }else{
            view = convertView
        }
        if(list.size > position) {
            view.findViewById<TextView>(R.id.tvHeader).text = list[position]
        }else{
            view.findViewById<TextView>(R.id.tvHeader).text = "WTF"
        }
        return view
    }
}