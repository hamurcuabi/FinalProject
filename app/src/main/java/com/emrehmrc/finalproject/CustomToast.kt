package com.emrehmrc.finalproject

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import java.util.zip.Inflater

class CustomToast {

    var context:Context?=null
    var message:String?=null



    constructor(context: Context,message:String){

        this.context=context
        this.message=message
    }
    public fun show(){

        var inflater = LayoutInflater.from(context)
        val customToastroot = inflater.inflate(R.layout.custom_toast, null)
        val customtoast = Toast(context)
        var messageText = customToastroot.findViewById<TextView>(R.id
                .tvMessage)
        messageText.text=message
        customtoast.view = customToastroot
        customtoast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        customtoast.duration = Toast.LENGTH_SHORT
        customtoast.show()
    }

}