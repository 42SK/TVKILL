package com.redirectapps.tvkill

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
