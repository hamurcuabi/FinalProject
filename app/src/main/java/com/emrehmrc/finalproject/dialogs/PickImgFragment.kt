package com.emrehmrc.finalproject.dialogs


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.emrehmrc.finalproject.R


class PickImgFragment : DialogFragment() {

    interface onImgListener {

        fun galeryPick(path: Uri?)
        fun cameraShout(img: Bitmap)

    }

    lateinit var mProfilImg: onImgListener
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_pick_img, container, false)

        var pickGaleri = view.findViewById<TextView>(R.id.tvGaleri)
        var pickCamera = view.findViewById<TextView>(R.id.tvCamera)
        pickGaleri.setOnClickListener {

            var intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)


        }
        pickCamera.setOnClickListener {

            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 2)
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {

            var fromGaleri=data.data
            mProfilImg.galeryPick(fromGaleri)
           dialog.dismiss()


        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {


            var fromCamera:Bitmap
            fromCamera=data.extras.get("data") as Bitmap
            mProfilImg.cameraShout(fromCamera)
            dialog.dismiss()
        }
    }

    override fun onAttach(context: Context?) {

        mProfilImg=activity as onImgListener
        super.onAttach(context)
    }

}
