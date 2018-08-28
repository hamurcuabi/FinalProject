package com.emrehmrc.finalproject.dialogs

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.emrehmrc.finalproject.ChatRoomsActivity
import com.emrehmrc.finalproject.CustomToast
import com.emrehmrc.finalproject.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class UpdateChatRoom : DialogFragment(), PickImgFragment.onImgListener {
    var fromGaleri: Uri? = null
    var fromCamera: Bitmap? = null
    val COMPRESSİNG_QUALTY = 3
    override fun galeryPick(path: Uri?) {
        fromGaleri = path
        Picasso.get().load(fromGaleri).resize(96, 96).into(imgRoomUpdate)
    }

    override fun cameraShout(img: Bitmap) {
        fromCamera = img
        imgRoomUpdate.setImageBitmap(fromCamera)
    }


    lateinit var edtRoomName: EditText
    lateinit var imgRoomUpdate: ImageView
    lateinit var tvRoomName: TextView
    lateinit var btnUpdateClose: Button
    lateinit var btnUpdateUptade: Button
    lateinit var btnCameraOpen: Button
    lateinit var progBar: ProgressBar
    lateinit var roomId: String
    var permissions: Boolean = false

    var mContext: FragmentActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.updatechatroom, container, false)
        var path = ""
        roomId = arguments!!.getString("roomid")
        mContext = activity
        edtRoomName = view.findViewById(R.id.edtRoomName)
        imgRoomUpdate = view.findViewById(R.id.imgRoomUpdate)
        tvRoomName = view.findViewById(R.id.tvRoomName)
        btnUpdateClose = view.findViewById(R.id.btnUpdateClose)
        btnUpdateUptade = view.findViewById(R.id.btnUpdateUptade)
        btnCameraOpen = view.findViewById(R.id.btnCameraOpen)
        progBar = view.findViewById(R.id.progUpdateChatroom)
        btnCameraOpen.setOnClickListener {


            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 2)
        }
        getRoomName()
        getPath()
        btnUpdateUptade.setOnClickListener {

            updateRoomName()


        }
        btnUpdateClose.setOnClickListener {
            dismiss()
            (activity as ChatRoomsActivity).getAllChatRooms()
        }
        imgRoomUpdate.setOnClickListener {


            var intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)


        }

        return view
    }

    private fun getPath() {
        var ref = FirebaseDatabase.getInstance().reference
        var query = ref.child("chat_rooms").child(roomId).child("room_img")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                if ((p0?.value.toString().equals(""))) {
                  //  imgRoomUpdate.setImageResource(R.drawable.defaultuser)

                } else {

                    try {
                        Picasso.get().load(p0?.value.toString()).resize(96,96).into(imgRoomUpdate)
                    } catch (ex: Exception) {

                    }

                }


            }

        })

    }


    private fun updateImg(result: ByteArray?) {


        var databeseRef = FirebaseStorage.getInstance().getReference()
        var placeUpload = databeseRef.child("images/rooms/" + roomId + "/room_img")
                .putBytes(result!!)
                .addOnSuccessListener { taskSnapshot ->

                    var fireBaseUrl = taskSnapshot.downloadUrl
                    FirebaseDatabase.getInstance().reference
                            .child("chat_rooms")
                            .child(roomId)
                            .child("room_img")
                            .setValue(fireBaseUrl.toString())

                    var toast = CustomToast(mContext!!, "Fotoğraf Yüklendi!!")
                    toast.show()


                }
                .addOnFailureListener { task ->
                    var toast = CustomToast(mContext!!, "HATA OLUŞTU!")
                    toast.show()
                }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {

            var toast = CustomToast(mContext!!, "Yükleme Başladı!")
            toast.show()
            var fromGaleri = data.data
            imgCompres(fromGaleri)


        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            var toast = CustomToast(mContext!!, "Yükleme Başladı!")
            toast.show()

            var fromCamera: Bitmap
            fromCamera = data.extras.get("data") as Bitmap
            imgCompres(fromCamera)

        }
    }

    private fun getRoomName() {
        var ref = FirebaseDatabase.getInstance().reference
        var query = ref.child("chat_rooms").child(roomId).child("roomName")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {


                tvRoomName.setText(p0?.value.toString())

            }

        })
    }

    private fun updateRoomName() {
        if (!edtRoomName.text.toString().equals("")) {

            var ref = FirebaseDatabase.getInstance().reference
            ref.child("chat_rooms").child(roomId).child("roomName").setValue(edtRoomName.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var toast = CustomToast(mContext!!, "Başarıyla Güncellendi")
                            toast.show()
                            dismiss()
                            (activity as ChatRoomsActivity).getAllChatRooms()
                        } else {
                            var toast = CustomToast(mContext!!, "Güncelleme Hatası")
                            toast.show()
                            dismiss()
                        }
                    }
        }

    }

    private fun imgCompres(fromGaleri: Uri) {

        var compres = CompressingImg()
        compres.execute(fromGaleri)
    }

    private fun imgCompres(fromCamera: Bitmap) {
        var compres = CompressingImg(fromCamera)
        var uri: Uri? = null
        compres.execute(uri)
    }

    private fun getPermissions() {

        var perms = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest
                .permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)

        if (ContextCompat.checkSelfPermission(mContext!!, perms[0]) == PackageManager
                        .PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext!!, perms[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext!!, perms[2]) == PackageManager.PERMISSION_GRANTED) {

            permissions = true

        } else {
            ActivityCompat.requestPermissions(mContext!!, perms, 3)

        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == 3) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                try {

                    var pickImg = PickImgFragment()
                    pickImg.show(mContext!!.supportFragmentManager, "fotoSeç")
                } catch (ex: Exception) {

                    Toast.makeText(mContext, ex.toString(), Toast.LENGTH_SHORT).show()
                }

            } else {

                //  Toast.makeText(this@ProfilSettingActivity, "Tüm İzinleri Vermelisiniz", Toast  .LENGTH_SHORT).show()
                var toast = CustomToast(mContext!!, "Tüm İzinleri Vermelisiniz!")
                toast.show()

            }

        }
    }

    inner class CompressingImg : AsyncTask<Uri, Double, ByteArray> {

        var mBitmap: Bitmap? = null

        constructor() {}
        constructor(bm: Bitmap) {

            mBitmap = bm
        }

        override fun doInBackground(vararg params: Uri?): ByteArray? {


            //eğer galerindense
            if (mBitmap == null) {

                mBitmap = MediaStore.Images.Media.getBitmap(mContext!!.contentResolver, params[0])

            }

            var imgBytes: ByteArray? = null

            for (i in 1..COMPRESSİNG_QUALTY) {

                imgBytes = convertToBitmaptoByte(mBitmap, 100 / i)
                publishProgress(imgBytes?.size?.toDouble())


            }
            return imgBytes

        }

        override fun onProgressUpdate(vararg values: Double?) {
            super.onProgressUpdate(*values)
        }

        private fun convertToBitmaptoByte(mBitmap: Bitmap?, i: Int): ByteArray? {

            var stream = ByteArrayOutputStream()
            mBitmap?.compress(Bitmap.CompressFormat.JPEG, i, stream)
            return stream.toByteArray()

        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)


            updateImg(result)
            imgRoomUpdate.setImageBitmap(mBitmap)

        }

        override fun onPreExecute() {
            super.onPreExecute()
        }
    }
}