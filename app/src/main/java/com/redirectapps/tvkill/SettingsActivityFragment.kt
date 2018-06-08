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

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redirectapps.tvkill.databinding.FragmentSettingsBinding

class SettingsActivityFragment : Fragment(), SettingsFragmentHandlers {
    lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.handlers = this

        val settings = Settings.with(context!!)

        settings.additionalPatterns.observe(this, Observer {
            binding.additionalPatternsEnabled = it
        })

        settings.showMute.observe(this, Observer {
            binding.muteButtonEnabled = it!!
        })

        settings.delayBetweenPatterns.observe(this, Observer {
            binding.delayBetweenPatterns = it!!.toInt()
        })

        settings.showDetails.observe(this, Observer {
            binding.showDetailsEnabled = it!!
        })
    }

    override fun setEnableMuteButton(enableMuteButton: Boolean) {
        Settings.with(context!!).setShowMute(enableMuteButton)
    }

    override fun setEnableAdditionalPatterns(enabledAdditionalPatterns: Boolean) {
        Settings.with(context!!).setShowAdditionalPatterns(enabledAdditionalPatterns)
    }

    override fun setDelayBetweenPatterns(delayInMillis: Long) {
        Settings.with(context!!).setDelayBetweenPatterns(delayInMillis)
    }

    override fun setEnableShowDetails(showDetails: Boolean) {
        Settings.with(context!!).setShowDetails(showDetails)
    }
}

interface SettingsFragmentHandlers {
    fun setEnableMuteButton(enableMuteButton: Boolean)
    fun setEnableAdditionalPatterns(enabledAdditionalPatterns: Boolean)
    fun setDelayBetweenPatterns(delayInMillis: Long)
    fun setEnableShowDetails(showDetails: Boolean)
}