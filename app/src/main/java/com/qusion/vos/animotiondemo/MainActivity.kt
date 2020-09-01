package com.qusion.vos.animotiondemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qusion.vos.animotiondemo.R
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}