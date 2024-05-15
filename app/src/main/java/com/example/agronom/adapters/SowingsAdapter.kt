package com.example.agronom.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agronom.R
import com.example.agronom.data.Sowings

class SowingsAdapter (private var sowingsList : ArrayList<Sowings>) : RecyclerView.Adapter<SowingsAdapter.MyViewHolder>() {
    private lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(item: Sowings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sowing_item, parent, false)
        return MyViewHolder(itemView)
    }

    fun setFilteredList(sowingsList: ArrayList<Sowings>){
        this.sowingsList = sowingsList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return  sowingsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = sowingsList[position]
        holder.itemView.setOnClickListener {
            listener.onItemClick(currentItem)
        }
        holder.culture.text = currentItem.culture?.get("cultureName")
        holder.imageView.minimumWidth = holder.imageView.height
        Glide.with(holder.itemView.context).load(currentItem.culture?.get("imagePath")).into(holder.imageView)
        holder.field.text = currentItem.field?.get("name")
        holder.date.text = currentItem.date
        if(currentItem.status!!){
            holder.status.text = "Засеян"
            holder.sowingLayout.setBackgroundResource(android.R.color.transparent)
        }
        else{
            holder.status.text = "Завершён"
            holder.sowingLayout.setBackgroundResource(R.color.light_gray)
            holder.status.setTextColor(Color.parseColor("#a2a2a2"))
        }
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val culture : TextView = itemView.findViewById(R.id.tvCulture)
        val field : TextView = itemView.findViewById(R.id.tvField)
        val date : TextView = itemView.findViewById(R.id.tvDate)
        val status : TextView = itemView.findViewById(R.id.tvStatus)
        val imageView : ImageView = itemView.findViewById(R.id.imageView)
        val sowingLayout : GridLayout = itemView.findViewById(R.id.sowingLayout)
    }

}