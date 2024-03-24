package com.example.agronom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.data.Cultures

class CultureAdapter(private var cultureList : ArrayList<Cultures>) : RecyclerView.Adapter<CultureAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.culture, parent, false)
        return MyViewHolder(itemView)
    }

    fun setFilteredList(cultureList: ArrayList<Cultures>){
        this.cultureList = cultureList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return  cultureList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = cultureList[position]
        holder.cultureName.text = currentItem.cultureName
        holder.varienty.text = currentItem.varienty
        holder.boardingMonth.text = currentItem.boardingMonth
        holder.growingSeason.text = currentItem.growingSeason
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val cultureName : TextView = itemView.findViewById(R.id.tvName)
        val varienty : TextView = itemView.findViewById(R.id.tvVarienty)
        val boardingMonth : TextView = itemView.findViewById(R.id.tvboardingMonth)
        val growingSeason : TextView = itemView.findViewById(R.id.tvgrowingSeason)
    }
}