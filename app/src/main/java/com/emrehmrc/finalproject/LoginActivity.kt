package com.emrehmrc.finalproject

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Toast
import com.emrehmrc.finalproject.dialogs.ForgetPasswordFragment
import com.emrehmrc.finalproject.dialogs.SendMailAgainFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    var view: View? = null
    //Giriş yapılmış mı kontrol için AuthListener ekliyorum

    lateinit var mAuthState: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_login)

        initMAuthState()
        tvSendMailAgain.setOnClickListener {
            var sendMailDialog = SendMailAgainFragment()
            sendMailDialog.show(supportFragmentManager, "MailYenidenOnay")
        }
        tvRegister.setOnClickListener {

            var intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
        tvForgetPassword.setOnClickListener {

            var forgetPassDialog = ForgetPasswordFragment()
            forgetPassDialog.show(supportFragmentManager, "ŞifremiUnuttum")


        }

        cbShowPass.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

                if (!isChecked) {
                    edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                } else edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }

        })

        btnLogin.setOnClickListener {


            if (!isEmpty()) {
                progresStart()
                FirebaseAuth.getInstance().signInWithEmailAndPassword(edtEmail.text.toString(),
                        edtPassword.text.toString())
                        .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                            override fun onComplete(p0: Task<AuthResult>) {

                                if (p0.isSuccessful) {

                                    if (!p0.result.user.isEmailVerified) {

                                        FirebaseAuth.getInstance().signOut()
/*
                                        Toast.makeText(this@LoginActivity, "MAİLİNİZİ ONAYLAYIN!"
                                                + p0
                                                .exception?.message, Toast
                                                .LENGTH_LONG).show()
                                                */
                                        var toast=CustomToast(this@LoginActivity,"Mail Adresinizi Onaylayınız")
                                        toast.show()

                                    } else {

                                        //FCM
                                        var currentToken: String? = FirebaseInstanceId.getInstance().token
                                        saveToken(currentToken)

                                        var intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }


                                } else {
                                    /*
                                    Toast.makeText(this@LoginActivity, "HATALI GİRİŞ! " + p0
                                            .exception?.message, Toast
                                            .LENGTH_LONG).show()

                                    val context = applicationContext
                                    val inflater = layoutInflater

                                    val customToastroot = inflater.inflate(R.layout.custom_toast, null)

                                    val customtoast = Toast(context)

                                    var messageText = customToastroot.findViewById<TextView>(R.id
                                            .tvMessage)
                                     messageText.setText("HATALI GİRİŞ")

                                    customtoast.view = customToastroot
                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
                                    customtoast.duration = Toast.LENGTH_SHORT
                                    customtoast.show()
 */
                                    var toast=CustomToast(this@LoginActivity,"Hatalı Giriş")
                                    toast.show()

                                }
                                progresStop()

                            }

                        })

            } else {
                /*
                Toast.makeText(this@LoginActivity, "BOŞ ALANLARI DOLDURUNUZ!", Toast
                        .LENGTH_LONG).show()
                        */
                var toast=CustomToast(this@LoginActivity,"Boş Alanları Doldurunuz")
                toast.show()
            }

        }

    }

    private fun saveToken(currentToken: String?) {

        var ref = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser?.uid)
                .child("token")
                .setValue(currentToken)


    }

    private fun initMAuthState() {

        mAuthState = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var user = p0.currentUser
                if (user != null) {//Kullanıcı Girişi Var mı ?


                    if (user.isEmailVerified) {//Giren Kullanıcı Onaylı mı?

                        //Onaylı Sisteme Gir
                        var intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)

                    } else {

                        var toast=CustomToast(this@LoginActivity,"Mail Adresinizi Onaylayınız")
                        toast.show()


                    }
                } else {

                }

            }


        }

    }

    private fun isEmpty(): Boolean {

        return (edtEmail.text.isEmpty() || edtPassword.text.isEmpty())

    }

    private fun progresStart() {

        pbLogin.visibility = View.VISIBLE
    }

    private fun progresStop() {

        pbLogin.visibility = View.INVISIBLE
    }

    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthState)
        super.onStart()

    }

    override fun onStop() {
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthState)
        super.onStop()

    }


}
