package com.example.agronom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agronom.R
import com.example.agronom.data.Harvest

class HarvestAdapter (private var harvestList : ArrayList<Harvest>) : RecyclerView.Adapter<HarvestAdapter.MyViewHolder>() {
    private lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(item: Harvest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.harvest_item, parent, false)
        return MyViewHolder(itemView)
    }

    fun setFilteredList(harvestList: ArrayList<Harvest>){
        this.harvestList = harvestList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return  harvestList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = harvestList[position]
        holder.itemView.setOnClickListener {
            listener.onItemClick(currentItem)
        }
        holder.culture.text = currentItem.culture?.get("cultureName")
        Glide.with(holder.itemView.context).load(currentItem.culture?.get("imagePath")).into(holder.imageView)
        holder.field.text = currentItem.field?.get("name")
        holder.count.text = currentItem.count.toString() + " т."
        holder.date.text = currentItem.date
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val culture : TextView = itemView.findViewById(R.id.tvCulture)
        val field : TextView = itemView.findViewById(R.id.tvField)
        val count : TextView = itemView.findViewById(R.id.tvCount)
        val date : TextView = itemView.findViewById(R.id.tvDate)
        val imageView : ImageView = itemView.findViewById(R.id.imageView)
    }

}