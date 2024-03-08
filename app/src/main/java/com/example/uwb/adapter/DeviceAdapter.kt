package com.example.uwb.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uwb.databinding.ItemListBinding
import com.example.uwb.model.DeviceModel

class DeviceAdapter : ListAdapter<DeviceModel, DeviceAdapter.DeviceViewHolder>(DeviceDiffUtilCallback()) {

    // ViewHolder class to hold references to the views for each item
    class DeviceViewHolder(private val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item:DeviceModel){
            binding.tvName.text = item.deviceName
            binding.tvAddress.text = item.deviceAddress
        }
    }

    // Create new view holder (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DeviceViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil callback for efficient list updates
    class DeviceDiffUtilCallback : DiffUtil.ItemCallback<DeviceModel>() {
        override fun areItemsTheSame(oldItem: DeviceModel, newItem: DeviceModel): Boolean {
            return oldItem.deviceAddress == newItem.deviceAddress
        }

        override fun areContentsTheSame(oldItem: DeviceModel, newItem: DeviceModel): Boolean {
            return oldItem == newItem
        }
    }
}