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
import android.content.Intent
import android.os.Bundle
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

        TransmitService.subscribeIfRunning.observe(this, Observer {  })
    }
}
