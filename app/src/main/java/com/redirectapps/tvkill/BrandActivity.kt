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

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_brand.*

class BrandActivity : AppCompatActivity() {
    companion object {
        private const val FOREVER_MODE_ENABLED = "forever_mode_enabled"

        fun startForResult(activity: Activity, foreverModeEnabled: Boolean, requestCode: Int) {
            activity.startActivityForResult(
                    Intent(activity, BrandActivity::class.java)
                            .putExtra(FOREVER_MODE_ENABLED, foreverModeEnabled),
                    requestCode
            )
        }
    }

    // this allows the service to see that the activity is shown
    private val dummyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {

        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand)
        setSupportActionBar(toolbar)

        lateinit var fragment: BrandActivityFragment

        if (savedInstanceState == null) {
            fragment = BrandActivityFragment.newInstance(intent.getBooleanExtra(FOREVER_MODE_ENABLED, false))

            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
        } else {
            fragment = supportFragmentManager.findFragmentById(R.id.container) as BrandActivityFragment
        }

        fragment.foreverModeEnabled.observe(this, Observer {
            setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(FOREVER_MODE_ENABLED, it!!)
            )
        })
    }

    override fun onResume() {
        super.onResume()

        bindService(
                Intent(this, TransmitService::class.java),
                dummyServiceConnection,
                Context.BIND_AUTO_CREATE
        )
    }

    override fun onPause() {
        super.onPause()

        unbindService(dummyServiceConnection)
    }
}
