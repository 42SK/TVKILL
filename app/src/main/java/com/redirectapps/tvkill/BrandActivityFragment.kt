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
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_brand.*

class BrandActivityFragment : Fragment(), BrandAdapterHandlers {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_brand, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BrandAdapter()

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(context)

        Settings.with(context!!).showMute.observe(this, Observer {
            if (it != null) {
                adapter.setShowMuteButton(it)
            }
        })

        TransmitService.status.observe(this, Observer {
            adapter.setTransmitStatus(it)
        })

        adapter.setHandlers(this)
    }

    override fun doMute(designation: String) {
        TransmitService.executeRequest(
                TransmitServiceSendRequest(
                        TransmitServiceAction.Mute,
                        false,
                        designation
                ),
                context!!
        )
    }

    override fun doPowerOff(designation: String) {
        TransmitService.executeRequest(
                TransmitServiceSendRequest(
                        TransmitServiceAction.Off,
                        false,
                        designation
                ),
                context!!
        )
    }
}
