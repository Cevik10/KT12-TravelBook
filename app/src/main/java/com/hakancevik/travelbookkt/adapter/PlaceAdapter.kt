package com.hakancevik.travelbookkt.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hakancevik.travelbookkt.databinding.RecyclerRowBinding
import com.hakancevik.travelbookkt.model.Place
import com.hakancevik.travelbookkt.view.MapsActivity

class PlaceAdapter(var placeList: ArrayList<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {
    class PlaceHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {

        holder.binding.recyclerViewTextView.text = placeList.get(position).name

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MapsActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id", placeList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return placeList.size
    }
}

