package com.emrehmrc.finalproject.dialogs


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.emrehmrc.finalproject.CustomToast
import com.emrehmrc.finalproject.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_send_mail_again.*


class SendMailAgainFragment : DialogFragment() {


    lateinit var edtMail: EditText
    lateinit var edtPass: EditText
     var mContext: FragmentActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_send_mail_again, container, false)

        mContext = activity

        var btnClose = view.findViewById<Button>(R.id.btnCloseFragment)
        var btnSend = view.findViewById<Button>(R.id.btnSendFragment)
        edtMail = view.findViewById(R.id.edtMailFragment)
        edtPass = view.findViewById(R.id.edtPasswordFragment)

        btnClose.setOnClickListener {

            dialog.dismiss()
        }
        btnSend.setOnClickListener {
            if (!isEmpty()) {
                tryLogin(edtMail.text.toString(), edtPass.text.toString())
                dialog.dismiss()
            }
            else {
              //  Toast.makeText(mContext, "Boş Alanları Doldurunuz!", Toast.LENGTH_SHORT).show()
                var toast= CustomToast(mContext!!,"Boş Alanları Doldurunuz!")
                toast.show()
            }
        }

        return view
    }

    private fun tryLogin(mail: String, pass: String) {

        var credential = EmailAuthProvider.getCredential(mail, pass)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                sendMailAgain()
            } else {

               // Toast.makeText(mContext, "Email ya da Şifre Hatalı!", Toast.LENGTH_SHORT).show()
                var toast= CustomToast(mContext!!,"Email ya da Şifre Hatalı!")
                toast.show()

            }

        }


    }

    private fun sendMailAgain() {
        var user = FirebaseAuth.getInstance().currentUser

        if (user != null) {

            user.sendEmailVerification().addOnCompleteListener(object : OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {

                    if (p0.isSuccessful) {
                        //Toast.makeText(mContext, "Mailinizi Kontrol Ediniz!",    Toast    .LENGTH_LONG).show()
                        var toast= CustomToast(mContext!!,"Mailinizi Kontrol Ediniz!")
                        toast.show()
                    } else {
                     //   Toast.makeText(mContext, "HATA OLUŞTU: " + p0.exception?.message, Toast.LENGTH_LONG).show()
                        var toast= CustomToast(mContext!!,"HATA OLUŞTU: " + p0.exception?.message)
                        toast.show()
                    }

                }


            })

        }
    }
    private fun isEmpty(): Boolean {

        return (edtMailFragment.text.isEmpty() || edtPasswordFragment.text.isEmpty())

    }

}
