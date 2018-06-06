package com.redirectapps.tvkill

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.redirectapps.tvkill.databinding.ActivityBrandItemBinding

class BrandAdapter: RecyclerView.Adapter<BrandViewHolder>() {
    val data = BrandContainer.allBrands

    private var showMuteButton = false
    private var transmitStatus: TransmitServiceStatus? = null
    private var handlers: BrandAdapterHandlers? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getItem(position: Int): Brand {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).designation.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        return BrandViewHolder(
                ActivityBrandItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val designation = getItem(position).designation

        holder.binding.brandName = designation
        holder.binding.showMuteButton = showMuteButton
        holder.binding.handlers = handlers

        if (transmitStatus?.request?.brandName == designation) {
            val action = transmitStatus!!.request.action

            if (action == TransmitServiceAction.Mute) {
                holder.binding.status = BrandStatus.Mute
            } else if (action == TransmitServiceAction.Off) {
                holder.binding.status = BrandStatus.PowerOff
            } else {
                throw IllegalStateException()
            }
        } else {
            if (transmitStatus == null) {
                holder.binding.status = BrandStatus.Idle
            } else {
                holder.binding.status = BrandStatus.SendingOther
            }
        }

        holder.binding.executePendingBindings()
    }

    fun setShowMuteButton(showMuteButton: Boolean) {
        this.showMuteButton = showMuteButton
        notifyDataSetChanged()
    }

    fun setTransmitStatus(transmitStatus: TransmitServiceStatus?) {
        this.transmitStatus = transmitStatus
        notifyDataSetChanged()
    }

    fun setHandlers(handlers: BrandAdapterHandlers?) {
        this.handlers = handlers;
        notifyDataSetChanged()
    }
}

class BrandViewHolder(val binding: ActivityBrandItemBinding): RecyclerView.ViewHolder(binding.root)

enum class BrandStatus {
    Idle, Mute, PowerOff, SendingOther
}

interface BrandAdapterHandlers {
    fun doPowerOff(designation: String)
    fun doMute(designation: String)
}