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

class CultureViewAdapter(private val context: Context, private var crops: Map<String,String>) : BaseExpandableListAdapter() {

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

    fun updateCropInfo(cropInfo: Map<String, String>) {
        // Обновите данные культуры в вашем адаптере
        crops = cropInfo
        notifyDataSetChanged() // Сообщите адаптеру, что данные изменились
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.expand_list_name, null)
        val textView = view.findViewById<TextView>(R.id.expandName)
        textView.text = "Информация о культуре"
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val cropInfo = crops
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.culture_item, null)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvVarienty = view.findViewById<TextView>(R.id.tvVarienty)
        val tvboardingMonth = view.findViewById<TextView>(R.id.tvboardingMonth)
        val tvgrowingSeason = view.findViewById<TextView>(R.id.tvgrowingSeason)
        Glide.with(context).load(crops["imagePath"]).into(imageView)
        tvName.text = cropInfo["cultureName"].toString()
        tvVarienty.text = cropInfo["varienty"].toString()
        tvboardingMonth.text = cropInfo["boardingMonth"].toString()
        tvgrowingSeason.text = cropInfo["growingSeason"].toString()
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }
}