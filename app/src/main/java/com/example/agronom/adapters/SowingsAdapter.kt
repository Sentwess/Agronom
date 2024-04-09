package com.example.agronom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agronom.R
import com.example.agronom.data.Sowings

class SowingsAdapter (private var sowingsList : ArrayList<Sowings>) : RecyclerView.Adapter<SowingsAdapter.MyViewHolder>() {
    private lateinit var mListener:OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sowing_item, parent, false)
        return MyViewHolder(itemView, mListener)
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
        holder.culture.text = currentItem.culture?.get("cultureName") + "(${currentItem.culture?.get("varienty")})"
        Glide.with(holder.itemView.context).load(currentItem.culture?.get("imagePath")).into(holder.imageView)
        holder.field.text = currentItem.field?.get("name")
        holder.count.text = currentItem.count.toString()
        holder.date.text = currentItem.date.toString()
        if(currentItem.status!!){
            holder.status.text = "Засеяно"
        }
        else{
            holder.status.text = "Свободно"
        }
    }

    class MyViewHolder(itemView : View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){
        val culture : TextView = itemView.findViewById(R.id.tvCulture)
        val field : TextView = itemView.findViewById(R.id.tvField)
        val count : TextView = itemView.findViewById(R.id.tvCount)
        val date : TextView = itemView.findViewById(R.id.tvDate)
        val status : TextView = itemView.findViewById(R.id.tvStatus)
        val imageView : ImageView = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }

}