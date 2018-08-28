package com.emrehmrc.finalproject.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIDService:FirebaseInstanceIdService() {

    override fun onTokenRefresh() {

        var currentToken: String?=FirebaseInstanceId.getInstance().token

        saveToken(currentToken)

    }

    private fun saveToken(currentToken: String?) {

        var ref=FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser?.uid)
                .child("token")
                .setValue(currentToken)


    }
}