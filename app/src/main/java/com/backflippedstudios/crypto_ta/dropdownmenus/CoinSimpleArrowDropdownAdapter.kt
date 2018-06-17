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
class CoinSimpleArrowDropdownAdapter(context: Context?, resource: Int, objects: ArrayList<String>) : ArrayAdapter<String>(context, resource, objects) {
    private val mInflator: LayoutInflater = LayoutInflater.from(context)

    object data{
        var list: ArrayList<String> = ArrayList()
    }
    init {
        data.list = objects
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View
        if (convertView == null) {
            view = this.mInflator.inflate(R.layout.spinner_dropdown_main_view_with_arrow,parent,false)
        }else{
            view = convertView
        }
        view.findViewById<TextView>(R.id.tvHeader).text = data.list[position].substringBefore("-")
        return view
    }
}