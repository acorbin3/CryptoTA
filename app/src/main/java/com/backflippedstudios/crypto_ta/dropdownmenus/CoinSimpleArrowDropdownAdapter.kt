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
        var view: View = convertView ?: this.mInflator.inflate(R.layout.spinner_dropdown_main_view_with_arrow,parent,false)
        //Remove the detailed name of the first coin
        //println(data.list[position])
        val dashIndex = data.list[position].indexOf("-")
        val slashIndex = data.list[position].indexOf("/")
        if(dashIndex > 0 && slashIndex > 0 && dashIndex < slashIndex) {
            view.findViewById<TextView>(R.id.tvHeader).text = data.list[position].removeRange(dashIndex, slashIndex)
        }else{
            view.findViewById<TextView>(R.id.tvHeader).text = data.list[position]
        }
        return view
    }
}