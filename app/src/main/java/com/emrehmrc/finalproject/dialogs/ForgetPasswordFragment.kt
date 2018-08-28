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
import com.google.firebase.auth.FirebaseAuth


class ForgetPasswordFragment : DialogFragment() {

    lateinit var edtMail: EditText
    var mContext: FragmentActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_forget_password, container, false)
        mContext = activity
        var btnSend = view.findViewById<Button>(R.id.btnSend)
        var btnClose = view.findViewById<Button>(R.id.btnClose)
        edtMail=view.findViewById(R.id.edtMailFragment)

        btnClose.setOnClickListener {

            dialog.dismiss()
        }
        btnSend.setOnClickListener {
            if (!isEmpty()) {
                sendForgetPassword(edtMail.text.toString())

            } else {
                //Toast.makeText(mContext, "Mail Alanını Doldurunuz!", Toast.LENGTH_SHORT).show()
                var toast= CustomToast(mContext!!,"Mail Alanını Doldurunuz!")
                toast.show()
            }
        }


        return view
    }

    private fun sendForgetPassword(mail: String) {

        FirebaseAuth.getInstance().sendPasswordResetEmail(edtMail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                      //  Toast.makeText(mContext, "Şifre Sıfırlama Maili Gönderildi", Toast.LENGTH_SHORT).show()
                        var toast= CustomToast(mContext!!,"Şifre Sıfırlama Maili Gönderildi!")
                        toast.show()

                    } else {
                       // Toast.makeText(mContext, "HATA OLUŞTU! " + task.exception?.message, Toast.LENGTH_SHORT).show()
                        var toast= CustomToast(mContext!!,"HATA OLUŞTU! " + task.exception?.message)
                        toast.show()
                    }
                    dialog.dismiss()
                }

    }

    private fun isEmpty(): Boolean {

        return (edtMail.text.isEmpty())

    }
}
