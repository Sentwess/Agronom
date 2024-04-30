package com.example.agronom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.agronom.R

class HarvestViewAdapter(private val context: Context, private var harvest: Map<String,String>) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return 1
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return 1
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return 1
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    fun updateInfo(cropInfo: Map<String, String>) {
        harvest = cropInfo
        notifyDataSetChanged()
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.expand_list_name, null)
        val textView = view.findViewById<TextView>(R.id.expandName)
        textView.text = "Информация о предшественнике"
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.harvest_view_item, null)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val tvCulture = view.findViewById<TextView>(R.id.tvCulture)
        val tvcultureVarienty = view.findViewById<TextView>(R.id.tvcultureVarienty)
        val tvField = view.findViewById<TextView>(R.id.tvField)
        val tvCount = view.findViewById<TextView>(R.id.tvCount)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        imageView.minimumWidth = imageView.height
        Glide.with(context).load(harvest["imagePath"]).into(imageView)
        tvCulture.text = harvest["culture"].toString()
        tvcultureVarienty.text = harvest["varienty"].toString()
        tvField.text = harvest["field"].toString()
        tvCount.text = harvest["count"].toString()
        tvDate.text = harvest["date"].toString()
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }
}