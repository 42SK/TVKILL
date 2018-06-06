/**
 * Copyright (C) 2015 Sebastian Kappes
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
import android.app.AlertDialog
import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.hardware.ConsumerIrManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Spinner
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    // this allows the service to see that the activity is shown
    private val dummyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {

        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            if (doesSupportIr()) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, UniversalModeFragment())
                        .commit()
            } else {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, UnsupportedFragment())
                        .commit()
            }
        }
    }

    private fun doesSupportIr(): Boolean {
        val irService = getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager

        return irService.hasIrEmitter()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        if (doesSupportIr()) {
            val brandsItem = menu.findItem(R.id.brands)

            TransmitService.status.observe(this, Observer {
                brandsItem.isEnabled = it == null
            })
        } else {
            menu.removeItem(R.id.brands)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            startActivity(Intent(
                    this,
                    Preferences::class.java
            ))

            return true
        } else if (item.itemId == R.id.brands) {
            startActivity(Intent(
                    this,
                    BrandActivity::class.java
            ))
        }

        return super.onOptionsItemSelected(item)
    }
}
