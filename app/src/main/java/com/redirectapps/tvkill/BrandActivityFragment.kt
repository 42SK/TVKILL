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
