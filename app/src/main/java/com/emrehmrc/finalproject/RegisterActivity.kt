package com.emrehmrc.finalproject

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.emrehmrc.finalproject.class_model.Users
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_register)

        edtPass.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (edtPassAgain.text.toString().equals(edtPass.text.toString())) {
                    edtPass.setTextColor(resources.getColor(R.color.white_greyish))
                    edtPassAgain.setTextColor(resources.getColor(R.color.white_greyish))
                } else {
                    edtPass.setTextColor(resources.getColor(R.color.warning))
                    edtPassAgain.setTextColor(resources.getColor(R.color.warning))
                }
            }

        })
        edtPassAgain.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (edtPassAgain.text.toString().equals(edtPass.text.toString())) {
                    edtPass.setTextColor(resources.getColor(R.color.white_greyish))
                    edtPassAgain.setTextColor(resources.getColor(R.color.white_greyish))
                } else {
                    edtPass.setTextColor(resources.getColor(R.color.warning))
                    edtPassAgain.setTextColor(resources.getColor(R.color.warning))
                }
            }

        })

        btnRegister.setOnClickListener {

            if (!isEmpty()) {

                if (isPasswordsEqual()) {
                    if (edtPass.text.toString().length <= 5) {
                        var toast = CustomToast(this@RegisterActivity, "En Az 6 Karakter Giriniz")
                        toast.show()
                    } else {
                        Register(edtMail.text.toString(), edtPass.text.toString())
                    }


                } else {

                    // Toast.makeText(this, "ŞİFRELER UYUŞMUYOR!", Toast.LENGTH_LONG).show()
                    var toast = CustomToast(this@RegisterActivity, "Şifreler Uyuşmuyor")
                    toast.show()

                }

            } else {

                //  Toast.makeText(this, "BOŞ ALANLARI DOLDURUNUZ!", Toast.LENGTH_LONG).show()
                var toast = CustomToast(this@RegisterActivity, "Boş Alanları Doldurunuz")
                toast.show()
            }

        }

    }

    private fun Register(mail: String, pass: String) {

        progresStart()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                    override fun onComplete(p0: Task<AuthResult>) {

                        if (p0.isSuccessful) {

                            //Mail Gönderme işlemi
                            sendMailToVerif()
                            //Kullanıcıyı veritabanına kaydetme
                            var currentuser = FirebaseAuth.getInstance().currentUser
                            var user = Users()
                            user.name = currentuser?.email.toString().substring(0, currentuser
                                    ?.email.toString().indexOf("@"))
                            user.level = "0"
                            user.phone = "012345678"
                            user.profil_img = ""
                            user.user_id = currentuser?.uid

                            FirebaseDatabase.getInstance().reference
                                    .child("users")
                                    .child(currentuser?.uid)
                                    .setValue(user)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            var toast = CustomToast(this@RegisterActivity, "Üye " +
                                                    "Kaydı Yapıldı")
                                            toast.show()
                                            FirebaseAuth.getInstance().signOut()
                                            backToLogin()

                                        } else {

                                            var toast = CustomToast(this@RegisterActivity, "Hata " +
                                                    "Oluştu")
                                            toast.show()
                                        }
                                    }


                        } else {

                            var toast = CustomToast(this@RegisterActivity, "Hata Oluştu")
                            toast.show()
                        }
                        progresStop()
                    }


                })


    }


    private fun isEmpty(): Boolean {

        return (edtMail.text.isEmpty() || edtPass.text.isEmpty() || edtPassAgain.text
                .isEmpty())

    }

    private fun isPasswordsEqual(): Boolean {

        return (edtPass.text.toString().equals(edtPassAgain.text.toString()))
    }

    private fun sendMailToVerif() {

        var user = FirebaseAuth.getInstance().currentUser

        if (user != null) {

            user.sendEmailVerification().addOnCompleteListener(object : OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {

                    if (p0.isSuccessful) {
                        var toast = CustomToast(this@RegisterActivity, "Mailinizi Kontrol Ediniz")
                        toast.show()
                    } else {

                        var toast = CustomToast(this@RegisterActivity, "Hata Oluştu")
                        toast.show()
                    }

                }


            })

        }

    }

    private fun progresStart() {

        pbRegister.visibility = View.VISIBLE
    }

    private fun progresStop() {

        pbRegister.visibility = View.INVISIBLE
    }

    private fun backToLogin() {

        var intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

}
