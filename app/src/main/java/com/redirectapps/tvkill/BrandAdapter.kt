/**
 * Copyright (C) 2018 Jonas Lochmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.redirectapps.tvkill

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redirectapps.tvkill.databinding.ActivityBrandItemBinding

class BrandAdapter: RecyclerView.Adapter<BrandAdapterViewHolder>() {
    companion object {
        private const val TYPE_BRAND = 1
        private const val TYPE_MUTE_DISABLED_INFO = 2
    }

    val data = BrandContainer.allBrands

    private var showMuteButton = false
    private var foreverModeEnabled = false
    private var transmitStatus: TransmitServiceStatus? = null
    private var handlers: BrandAdapterHandlers? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        if (showMuteDisabledInfo()) {
            return data.size + 1
        } else {
            return data.size
        }
    }

    private fun getItem(position: Int): Brand {
        if (showMuteDisabledInfo()) {
            return data[position - 1]
        } else {
            return data[position]
        }
    }

    override fun getItemId(position: Int): Long {
        if (showMuteDisabledInfo() && position == 0) {
            return 123; // hardcoded constant
        } else {
            return getItem(position).designation.hashCode().toLong()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (showMuteDisabledInfo() && position == 0) {
            return TYPE_MUTE_DISABLED_INFO
        } else {
            return TYPE_BRAND
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandAdapterViewHolder {
        if (viewType == TYPE_BRAND) {
            return BrandViewHolder(
                    ActivityBrandItemBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                    )
            )
        } else if (viewType == TYPE_MUTE_DISABLED_INFO) {
            return MuteDisabledViewHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.activity_brand_mute_disabled, parent, false)
            )
        } else {
            throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: BrandAdapterViewHolder, position: Int) {
        if (holder is BrandViewHolder) {
            val designation = getItem(position).designation

            holder.binding.brandName = designation
            holder.binding.showMuteButton = showMuteButton
            holder.binding.handlers = handlers
            holder.binding.enableMuteButton = !foreverModeEnabled

            if (transmitStatus?.request?.brandName == designation) {
                val action = transmitStatus!!.request.action

                if (action == TransmitServiceAction.Mute) {
                    holder.binding.status = BrandStatus.Mute
                } else if (action == TransmitServiceAction.Off) {
                    holder.binding.status = BrandStatus.PowerOff
                } else {
                    throw IllegalStateException()
                }

                holder.binding.sendingThisForever = transmitStatus!!.request.forever
            } else {
                if (transmitStatus == null) {
                    holder.binding.status = BrandStatus.Idle
                } else {
                    holder.binding.status = BrandStatus.SendingOther
                }

                holder.binding.sendingThisForever = false
            }

            holder.binding.executePendingBindings()
        } else if (holder is MuteDisabledViewHolder) {
            // nothing to do
        } else {
            throw IllegalStateException()
        }
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

    fun setForeverModeEnabled(foreverModeEnabled: Boolean) {
        this.foreverModeEnabled = foreverModeEnabled
        notifyDataSetChanged()
    }

    private fun showMuteDisabledInfo(): Boolean {
        return showMuteButton && foreverModeEnabled
    }
}

sealed class BrandAdapterViewHolder(view: View): RecyclerView.ViewHolder(view)
class BrandViewHolder(val binding: ActivityBrandItemBinding): BrandAdapterViewHolder(binding.root)
class MuteDisabledViewHolder(view: View): BrandAdapterViewHolder(view)

enum class BrandStatus {
    Idle, Mute, PowerOff, SendingOther
}

interface BrandAdapterHandlers {
    fun doPowerOff(designation: String)
    fun doMute(designation: String)
    fun cancelTransmit()
}