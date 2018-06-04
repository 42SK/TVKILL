package com.redirectapps.tvkill

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_unsupported.*

class UnsupportedFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_unsupported, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        learnMore.setOnClickListener {
            startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.blaster_dialog_more_blaster_url))
            ))
        }
    }
}
