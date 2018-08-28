package com.emrehmrc.finalproject

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.emrehmrc.finalproject.class_model.Users
import com.emrehmrc.finalproject.dialogs.PickImgFragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profil_setting.*
import java.io.ByteArrayOutputStream

class ProfilSettingActivity : AppCompatActivity(), PickImgFragment.onImgListener {

    val COMPRESSİNG_QUALTY = 3
    var fromGaleri: Uri? = null
    var fromCamera: Bitmap? = null
    override fun galeryPick(path: Uri?) {

        fromGaleri = path

        Picasso.get().load(fromGaleri).resize(96, 96).into(imgProfil)
    }

    override fun cameraShout(img: Bitmap) {

        fromCamera = img
        imgProfil.setImageBitmap(fromCamera)
    }

    var permissions: Boolean = false
    var user = FirebaseAuth.getInstance().currentUser!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil_setting)

        readFromFireDatabase()
        btnResetPass.setOnClickListener {
            sendResetPassword(user.email.toString())
        }
        btnSave.setOnClickListener {

            if (edtUserName.text.isNotEmpty()) {

                saveUserInfos()

            } else {
                // Toast.makeText(this@ProfilSettingActivity, "KULLANICI ADINIZI YAZINIZ! ", Toast  .LENGTH_SHORT)  .show()
                var toast = CustomToast(this, "Kullanıcı Adınızı Yazınız!")
                toast.show()
            }
            if (fromGaleri != null) {


                imgCompres(fromGaleri!!)


            }
            if (fromCamera != null) {

                imgCompres(fromCamera!!)
            }
        }


        btnNewSavePass.setOnClickListener {
            saveNewPass()
        }

        tvShowLayout.setOnClickListener {

            if (layoutPassUpdate.visibility == View.INVISIBLE) {
                layoutPassUpdate.visibility = View.VISIBLE
            } else {

                layoutPassUpdate.visibility = View.INVISIBLE
            }
        }

        imgProfil.setOnClickListener {

            if (permissions) {
                var pickImg = PickImgFragment()
                pickImg.show(supportFragmentManager, "fotoSeç")
            } else {
                getPermissions()
            }

        }
        imgHomeProfil.setOnClickListener {

            var intent = Intent(this@ProfilSettingActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        imgBack.setOnClickListener {
            var intent = Intent(this@ProfilSettingActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        initMAuthState()

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

        if (ContextCompat.checkSelfPermission(this, perms[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, perms[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, perms[2]) == PackageManager.PERMISSION_GRANTED) {

            permissions = true

        } else {
            ActivityCompat.requestPermissions(this, perms, 3)

        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == 3) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                try {
                    super.onPostResume()
                    var pickImg = PickImgFragment()
                    pickImg.show(supportFragmentManager, "fotoSeç")
                } catch (ex: Exception) {

                    Toast.makeText(this@ProfilSettingActivity, ex.toString(), Toast.LENGTH_SHORT).show()
                }

            } else {

                //  Toast.makeText(this@ProfilSettingActivity, "Tüm İzinleri Vermelisiniz", Toast  .LENGTH_SHORT).show()
                var toast = CustomToast(this, "Tüm İzinleri Vermelisiniz!")
                toast.show()

            }

        }
    }

    private fun saveNewPass(){
        if (edtUserPass.text.toString().isNotEmpty()) {

            var credential = EmailAuthProvider.getCredential(user.email.toString(), edtUserPass
                    .text.toString())
            user.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            if (edtNewPass1.text.toString().isNotEmpty() && edtNewPass2.text
                                            .toString().isNotEmpty()) {
                                if (edtNewPass1.text.toString
                                        ().equals(edtNewPass2.text.toString())) {
                                    user.updatePassword(edtNewPass2.text.toString())
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    // Toast.makeText(this@ProfilSettingActivity,   "Şifreniz Başarıyla Güncellendi", Toast.LENGTH_SHORT.show()
                                                    var toast = CustomToast(this, "Şifreniz Başarıyla Güncellendi")
                                                    toast.show()
                                                    FirebaseAuth.getInstance().signOut()
                                                    backToLogin()

                                                } else {
                                                    // Toast.makeText(this@ProfilSettingActivity,  "HATA OLUŞTU! " + task.exception?.message, Toast  .LENGTH_SHORT).show()
                                                    var toast = CustomToast(this, "HATA OLUŞTU! " + task.exception?.message)
                                                    toast.show()

                                                }
                                            }
                                } else {
                                    //     Toast.makeText(this@ProfilSettingActivity, "Şifreler " +      "Uyuşmuyor!", Toast.LENGTH_SHORT).show()
                                    var toast = CustomToast(this, "Şifreler Uyuşmuyor!")
                                    toast.show()
                                }


                            } else {
                                //   Toast.makeText(this@ProfilSettingActivity, "Boş Alanları " +"Doldurunuz!", Toast.LENGTH_SHORT).show()
                                var toast = CustomToast(this, "Boş Alanları " +
                                        "Doldurunuz!")
                                toast.show()


                            }


                        } else {
                            //         Toast.makeText(this@ProfilSettingActivity, "Geçerli Şifreniz Hatalı! " + task.exception?.message, Toast.LENGTH_SHORT).show()
                            var toast = CustomToast(this, "Geçerli Şifreniz Hatalı!")
                            toast.show()
                        }
                    }
        } else {

            //  Toast.makeText(this@ProfilSettingActivity, "Geçerli Şifrenizi Yazmanız " +"Gerekiyor!", Toast.LENGTH_SHORT).show()
            var toast = CustomToast(this, "Geçerli Şifrenizi Yazmanız Gerekiyor!")
            toast.show()


        }
    }

    /*Mail Güncelleme İçin Kullanıldı
    private fun saveNewMail() {
        if (edtUserPass.text.toString().isNotEmpty()) {


            var credential = EmailAuthProvider.getCredential(user.email.toString(), edtUserPass
                    .text.toString())
            user.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            FirebaseAuth.getInstance().fetchProvidersForEmail(edtNewMail.text.toString())
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            if (task.getResult().providers?.size == 1) {
                                                Toast.makeText(this@ProfilSettingActivity,
                                                        "Bu Mail Kullanımda!!", Toast
                                                        .LENGTH_SHORT)
                                                        .show()

                                            } else {
                                                user.updateEmail(edtNewMail.text.toString())
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Toast.makeText(this@ProfilSettingActivity,
                                                                        "Mailiniz Başarıyla Güncellendi", Toast
                                                                        .LENGTH_SHORT)
                                                                        .show()
                                                                FirebaseAuth.getInstance().signOut()
                                                                backToLogin()

                                                            } else {
                                                                Toast.makeText(this@ProfilSettingActivity,
                                                                        "HATA OLUŞTU! " + task.exception?.message, Toast
                                                                        .LENGTH_SHORT)
                                                                        .show()

                                                            }
                                                        }


                                            }


                                        } else {
                                            Toast.makeText(this@ProfilSettingActivity, "MAİL GÜNCELLENEMEDİ! " + task.exception?.message, Toast
                                                    .LENGTH_SHORT)
                                                    .show()

                                        }

                                    }


                        } else {
                            Toast.makeText(this@ProfilSettingActivity, "Geçerli Şifrenizi " +
                                    "Hatalı! " + task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
        } else {

            Toast.makeText(this@ProfilSettingActivity, "Geçerli Şifrenizi Yazmanız " +
                    "Gerekiyor!", Toast.LENGTH_SHORT).show()

        }
    }
     */
    private fun saveUserInfos() {

        if (!edtUserName.text.toString().equals(user.email
                        .toString())) {


            var updateProfil = UserProfileChangeRequest.Builder()
                    .setDisplayName(edtUserName.text.toString()).build()
            user.updateProfile(updateProfil)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            //Database e yaz
                            FirebaseDatabase.getInstance().reference
                                    .child("users")
                                    .child(FirebaseAuth.getInstance().currentUser?.uid)
                                    .child("name")
                                    .setValue(edtUserName.text.toString())
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            //   Toast.makeText(this@ProfilSettingActivity,    "BAŞARIYLA GÜNCELLENDİ",  Toast.LENGTH_SHORT)  .show()
                                            var toast = CustomToast(this, "Başarıyla Güncellendi")
                                            toast.show()
                                        } else {
                                            //  Toast.makeText(this@ProfilSettingActivity, "HATA " +      "OLUŞTU! " + task  .exception?.message,  Toast        .LENGTH_SHORT)     .show()
                                            var toast = CustomToast(this, "HATA OLUŞTU! " + task
                                                    .exception?.message)
                                            toast.show()
                                        }
                                    }

                        } else {
                            // Toast.makeText(this@ProfilSettingActivity, "HATA OLUŞTU! " + task   .exception?.message,  Toast          .LENGTH_SHORT)  .show()
                            var toast = CustomToast(this, "HATA OLUŞTU! " + task
                                    .exception?.message)
                            toast.show()

                        }
                    }
        }


    }

    private fun sendResetPassword(mail: String) {

        FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        //   Toast.makeText(this@ProfilSettingActivity, "Şifre Sıfırlama Maili " + "Gönderildi", Toast   .LENGTH_SHORT).show()
                        var toast = CustomToast(this, "Şifre Sıfırlama Maili Gönderildi")
                        toast.show()
                    } else {
                        // Toast.makeText(this@ProfilSettingActivity, "HATA OLUŞTU! " + task.exception?.message, Toast    .LENGTH_SHORT)    .show()
                        var toast = CustomToast(this, "HATA OLUŞTU! " + task
                                .exception?.message)
                        toast.show()
                    }

                }

    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun backToLogin() {

        var intent = Intent(this@ProfilSettingActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun readFromFireDatabase() {

        pbUpdateImg.visibility = View.VISIBLE
        var referans = FirebaseDatabase.getInstance().reference

        var query = referans.child("users")
                .orderByKey()//orderbyChild olabilir yine
                .equalTo(user.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                for (singleSnapshot in p0!!.children) {
                    var cameUser = singleSnapshot.getValue(Users::class.java)
                    edtUserName.setText(cameUser?.name)
                    edtPhone.setText(cameUser?.phone)

                    if (cameUser?.profil_img != "") {
                        Picasso.get().load(cameUser?.profil_img
                                .toString()).into(imgProfil)
                    }

                    pbUpdateImg.visibility = View.INVISIBLE

                }

            }

        })

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

                mBitmap = MediaStore.Images.Media.getBitmap(this@ProfilSettingActivity.contentResolver, params[0])

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

            uploadImgToFiredatabase(result)

        }

        override fun onPreExecute() {
            super.onPreExecute()
        }
    }

    private fun uploadImgToFiredatabase(result: ByteArray?) {

        pbUpdateImg.visibility = View.VISIBLE
        var databeseRef = FirebaseStorage.getInstance().getReference()
        var placeUpload = databeseRef.child("images/users/" + FirebaseAuth.getInstance().currentUser?.uid + "/profil_img")
                .putBytes(result!!)
                .addOnSuccessListener { taskSnapshot ->

                    var fireBaseUrl = taskSnapshot.downloadUrl
                    FirebaseDatabase.getInstance().reference
                            .child("users")
                            .child(FirebaseAuth.getInstance().currentUser?.uid)
                            .child("profil_img")
                            .setValue(fireBaseUrl.toString())


                    Picasso.get().load(fireBaseUrl?.toString()).into(imgProfil)
                    pbUpdateImg.visibility = View.INVISIBLE


                }
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(p0: java.lang.Exception) {

                        pbUpdateImg.visibility = View.INVISIBLE
                        //   Toast.makeText(this@ProfilSettingActivity, "Upload Hatası! " + p0  .message,  Toast.LENGTH_SHORT).show()
                        var toast = CustomToast(this@ProfilSettingActivity, "Upload Hatası! " + p0.message)
                        toast.show()
                    }

                })


    }

    private fun initMAuthState() {

        FirebaseAuth.getInstance().addAuthStateListener { task ->
            if (task.currentUser == null) backToLogin()
        }


    }


}
