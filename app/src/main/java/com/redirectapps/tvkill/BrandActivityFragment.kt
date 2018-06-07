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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redirectapps.tvkill.databinding.FragmentBrandBinding

class BrandActivityFragment : Fragment(), BrandAdapterHandlers, BrandActivityFragmentHandlers {
    companion object {
        const val FOREVER_MODE_ENABLED = "forever_mode_enabled"

        fun newInstance(foreverModeEnabled: Boolean): BrandActivityFragment {
            val result = BrandActivityFragment()

            result.arguments = Bundle()
            result.arguments!!.putBoolean(FOREVER_MODE_ENABLED, foreverModeEnabled)

            return result
        }
    }

    lateinit var binding: FragmentBrandBinding;
    var foreverModeEnabled = MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = this.arguments

        if (savedInstanceState != null) {
            foreverModeEnabled.value = savedInstanceState.getBoolean(FOREVER_MODE_ENABLED)
        } else if (arguments != null) {
            foreverModeEnabled.value = arguments.getBoolean(FOREVER_MODE_ENABLED, false)
        } else {
            foreverModeEnabled.value = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(FOREVER_MODE_ENABLED, foreverModeEnabled.value!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBrandBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BrandAdapter()

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(context)
        binding.handlers = this

        Settings.with(context!!).showMute.observe(this, Observer {
            if (it != null) {
                adapter.setShowMuteButton(it)
            }
        })

        TransmitService.status.observe(this, Observer {
            adapter.setTransmitStatus(it)

            binding.canModifyForeverEnabled = it == null
        })

        adapter.setHandlers(this)

        foreverModeEnabled.observe(this, Observer { binding.foreverModeEnabled = it })
    }

    override fun doMute(designation: String) {
        TransmitService.executeRequest(
                TransmitServiceSendRequest(
                        TransmitServiceAction.Mute,
                        foreverModeEnabled.value!!,
                        designation
                ),
                context!!
        )
    }

    override fun doPowerOff(designation: String) {
        TransmitService.executeRequest(
                TransmitServiceSendRequest(
                        TransmitServiceAction.Off,
                        foreverModeEnabled.value!!,
                        designation
                ),
                context!!
        )
    }

    override fun cancelTransmit() {
        TransmitService.executeRequest(
                TransmitServiceCancelRequest,
                context!!
        )
    }

    override fun setForeverModeEnabled(enabled: Boolean) {
        foreverModeEnabled.value = enabled
    }
}

interface BrandActivityFragmentHandlers {
    fun setForeverModeEnabled(enabled: Boolean)
}