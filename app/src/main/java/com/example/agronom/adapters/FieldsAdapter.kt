package com.example.agronom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.data.Fields

class FieldsAdapter(private var fieldsList : ArrayList<Fields>) : RecyclerView.Adapter<FieldsAdapter.MyViewHolder>() {
    private lateinit var mListener:OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.field_item, parent, false)
        return MyViewHolder(itemView, mListener)
    }

    fun setFilteredList(fieldsList: ArrayList<Fields>){
        this.fieldsList = fieldsList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return  fieldsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = fieldsList[position]
        holder.name.text = currentItem.name
        holder.size.text = currentItem.size
        if(currentItem.status!!){
            holder.status.text = "Засеяно"
        }
        else{
            holder.status.text = "Свободно"
        }
    }

    class MyViewHolder(itemView : View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){
        val name : TextView = itemView.findViewById(R.id.tvName)
        val size : TextView = itemView.findViewById(R.id.tvSize)
        val status : TextView = itemView.findViewById(R.id.tvStatus)

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }

}