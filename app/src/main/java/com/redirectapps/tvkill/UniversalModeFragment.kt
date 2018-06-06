/**
 * Copyright (C) 2015 Sebastian Kappes
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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redirectapps.tvkill.databinding.UniversalBinding

class UniversalModeFragment : Fragment(), UniversalModeHandlers {
    companion object {
        private const val STATUS_FOREVER_MODE = "forever_mode"
    }

    lateinit var binding: UniversalBinding
    var foreverModeEnabled = MutableLiveData<Boolean>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = UniversalBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            foreverModeEnabled.value = savedInstanceState.getBoolean(STATUS_FOREVER_MODE)
        } else {
            foreverModeEnabled.value = false
        }

        binding.handlers = this
        Settings.with(context!!).showMute.observe(this, Observer { binding.showMute = it })
        foreverModeEnabled.observe(this, Observer { binding.foreverModeEnabled = it })

        TransmitService.status.observe(this, Observer {
            if (it == null) {
                binding.sendingSomething = false
                binding.powerOffStatus = UniversalModeButtonStatus.Idle
                binding.muteStatus = UniversalModeButtonStatus.Idle
                binding.progress = null
            } else {
                binding.sendingSomething = true
                binding.progress = it.progress

                if (it.request.brandName == null) {
                    if (it.request.action == TransmitServiceAction.Off) {
                        binding.powerOffStatus = UniversalModeButtonStatus.SendingThis
                        binding.muteStatus = UniversalModeButtonStatus.SendingOther
                    } else if (it.request.action == TransmitServiceAction.Mute) {
                        binding.powerOffStatus = UniversalModeButtonStatus.SendingOther
                        binding.muteStatus = UniversalModeButtonStatus.SendingThis
                    } else {
                        throw IllegalStateException()
                    }

                    binding.foreverModeEnabled = it.request.forever
                } else {
                    binding.powerOffStatus = UniversalModeButtonStatus.SendingOther
                    binding.muteStatus = UniversalModeButtonStatus.SendingOther
                }
            }
        })
    }

    override fun doPowerOff() {
        TransmitService.executeRequest(
                TransmitServiceSendRequest(
                        TransmitServiceAction.Off,
                        foreverModeEnabled.value!!,
                        null
                ),
                context!!
        )
    }

    override fun doMute() {
        TransmitService.executeRequest(
                TransmitServiceSendRequest(
                        TransmitServiceAction.Mute,
                        foreverModeEnabled.value!!,
                        null
                ),
                context!!
        )
    }

    override fun cancel() {
        TransmitService.executeRequest(
                TransmitServiceCancelRequest,
                context!!
        )
    }

    override fun setForeverModeEnabled(enabled: Boolean) {
        foreverModeEnabled.value = enabled
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(STATUS_FOREVER_MODE, foreverModeEnabled.value!!)
    }
}

interface UniversalModeHandlers {
    fun doPowerOff()
    fun doMute()
    fun cancel()
    fun setForeverModeEnabled(enabled: Boolean)
}

enum class UniversalModeButtonStatus {
    Idle, SendingThis, SendingOther
}
