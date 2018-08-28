package com.emrehmrc.finalproject

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex

class MyApplication : Application() {

    //Bi hatayı çözmek için kullandım
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}