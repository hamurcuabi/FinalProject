package com.emrehmrc.finalproject

import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.emrehmrc.finalproject.adaptors.MessagesRecyclerAdaptor
import com.emrehmrc.finalproject.class_model.ChatMessages
import com.emrehmrc.finalproject.class_model.ChatRooms
import com.emrehmrc.finalproject.class_model.FcmNotification
import com.emrehmrc.finalproject.class_model.Users
import com.emrehmrc.finalproject.interfaces.FcmInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat_room_in.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat_rooms.*


class ChatRoomInActivity : AppCompatActivity() {


    var allMessages: ArrayList<ChatMessages>? = null
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    var roomId: String? = null
    var uniqMessages: HashSet<String>? = null
    var mRecyclerAdapter: MessagesRecyclerAdaptor? = null
    var ref: DatabaseReference? = null
    var SERVER_KEY: String? = null
    val BASE_URL = "https://fcm.googleapis.com/fcm/"
    var room: ChatRooms? = null
    lateinit var roomName:String

    companion object {
        var isActivityActive: Boolean = false

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_chat_room_in)
        startAuthListener()
        getServerKey()
        getRoomId()
        edtMessage.setOnClickListener {
            try {
                rvMessages.smoothScrollToPosition(mRecyclerAdapter!!.itemCount - 1)
            } catch (e: Exception) {

            }

        }
        imgSend.setOnClickListener {

            if (!edtMessage.text.toString().equals("")) {

                var sendMessage = ChatMessages()
                sendMessage.message = edtMessage.text.toString()
                sendMessage.user_id = FirebaseAuth.getInstance().currentUser?.uid
                sendMessage.date = getDate()

                var reference = FirebaseDatabase.getInstance().reference
                        .child("chat_rooms")
                        .child(roomId)
                        .child("messages")
                var newMessage = reference.push().key
                reference.child(newMessage)
                        .setValue(sendMessage)


                //Retrofit
                var retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                var myInterface = retrofit.create(FcmInterface::class.java)
                var headers = HashMap<String, String>()
                headers.put("Content-Type", "application/json")
                headers.put("Authorization", "key=" + SERVER_KEY)


                /*
                var data = FcmNotification.Data("title", edtMessage.text.toString(),
                        "tip",roomId)
                var notification = FcmNotification("fjQsGD1G3AE:APA91bFo21qBQcp8eXtpDQU_3T9wVnShpvWFqKUyM7mT73iI3TTSRM_gNkBSxNBW_oAXswLURC69wtdsXLmAqaEalmdcrhv5pxYy_H4AARCCKXZZFIPPJSX2Qq6ZggVoBPSC20A8ZECW", data)

                var response = myInterface.sendNotification(headers, notification)
                response.enqueue(object : Callback<Response<FcmNotification>> {
                    override fun onFailure(call: Call<Response<FcmNotification>>?, t: Throwable?) {

                        Log.e("FCM",t?.message)
                    }

                    override fun onResponse(call: Call<Response<FcmNotification>>?, response: Response<Response<FcmNotification>>?) {
                       // Log.e("FCM","Başarılı "+response?.message()?.toString())
                    }


                })
                */


                var ref = FirebaseDatabase.getInstance().reference
                        .child("chat_rooms")
                        .child(roomId)
                        .child("users_in_room")
                        .orderByKey()
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {

                            }

                            override fun onDataChange(p0: DataSnapshot?) {

                                for (userId in p0?.children!!) {
                                    var id = userId?.key

                                    //
                                    if (!id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {

                                        var ref = FirebaseDatabase.getInstance().reference
                                                .child("users")
                                                .orderByKey()
                                                .equalTo(id)
                                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onCancelled(p0: DatabaseError?) {

                                                    }

                                                    override fun onDataChange(p0: DataSnapshot?) {

       var currentUser = p0?.children?.iterator()?.next()
       var token = currentUser?.getValue(Users::class
               .java)?.token


       var data = FcmNotification.Data("Bildirim",edtMessage.text.toString(), "KampüsEtkinlik", roomId)
       var to = token
       var notification = FcmNotification(to!!, data)

       var response = myInterface.sendNotification(headers, notification)
       response.enqueue(object : Callback<Response<FcmNotification>> {
           override fun onFailure(call: Call<Response<FcmNotification>>?, t: Throwable?) {

               // Log.e("FCM",t? .message)
           }

           override fun onResponse(call: Call<Response<FcmNotification>>?, response: Response<Response<FcmNotification>>?) {

                                                                // Log.e("FCM",response?.message())
                                                            }


                                                        })
                                                    }

                                                })


                                    }

                                }
                            }

                        })

                edtMessage.setText("")


            } else {
            }


        }
        fabCloseNotification.setOnClickListener {
            var dialog= AlertDialog.Builder(this)
            dialog.setTitle("Bildirimler Kapatılsın mı?")
            dialog.setMessage("Bildirimleri Kapatmak İstedğinize Emin misiniz?")
            dialog.setCancelable(true)
            dialog.setPositiveButton("EVET KAPAT",object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    var intent = Intent(this@ChatRoomInActivity, ChatRoomsActivity::class.java)
                    startActivity(intent)
                    finish()

                }

            })
            dialog.setNegativeButton("HAYIR KAPATMA",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }
            })
            dialog.show()

            outUserFromRoom()

        }
        fabBack.setOnClickListener{

            super.onBackPressed()
            finish()
        }
        getAllMessages()

    }

    private fun outUserFromRoom() {

        var ref = FirebaseDatabase.getInstance().reference
                .child("chat_rooms")
                .child(roomId)
                .child("users_in_room")
                .child(FirebaseAuth.getInstance().currentUser?.uid)
                .removeValue()


    }

    private fun getServerKey() {

        var ref = FirebaseDatabase.getInstance().reference
                .child("server")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {

                        var singelSnapShot = p0?.children?.iterator()?.next()
                        SERVER_KEY = singelSnapShot?.getValue().toString()


                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }


                })


    }

    private fun getRoomId() {
        var pending = intent
        if (intent.hasExtra("chatRoomId")) {
            roomId = intent.getStringExtra("chatRoomId")
        } else {
            roomId = intent.getStringExtra("room_id")
        }
        getRoomName()


        startMessageListener()


    }

    private fun getRoomName() {

        roomName=""
        var referans = FirebaseDatabase.getInstance().reference
        var query = referans.child("chat_rooms")
                .child(roomId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                var singelSnapShot = p0?.child("roomName")?.getValue()
                tvChatRoomNameIn.text=singelSnapShot.toString().toUpperCase()
            }

        })


    }

    private fun getDate(): String {

        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr"))
        return sdf.format(Date())


    }

    var mValueEventListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {

        }

        override fun onDataChange(p0: DataSnapshot?) {
            getAllMessages()
            if (isActivityActive) readMessagesUpdate(p0?.childrenCount?.toInt())

        }


    }

    private fun readMessagesUpdate(totalMessage: Int?) {

        var ref = FirebaseDatabase.getInstance().reference
                .child("chat_rooms")
                .child(roomId)
                .child("users_in_room")
                .child(FirebaseAuth.getInstance().currentUser?.uid)
                .child("read_messages")
                .setValue(totalMessage)
    }

    private fun startMessageListener() {


        ref = FirebaseDatabase.getInstance().getReference().child("chat_rooms")
                .child(roomId)
                .child("messages")
        ref?.addValueEventListener(mValueEventListener)

    }

    override fun onStart() {
        super.onStart()
        isActivityActive = true
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()

        isActivityActive = false
        if (mAuthListener != null) {

            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener!!)
        }
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser == null) {

            var intent = Intent(this@ChatRoomInActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun startAuthListener() {

        mAuthListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var user = p0.currentUser
                if (user == null) {

                    var intent = Intent(this@ChatRoomInActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()


                }
            }

        }
    }

    private fun getAllMessages() {

        if (allMessages == null) {

            allMessages = ArrayList<ChatMessages>()
            uniqMessages = HashSet<String>()
        }

        ref = FirebaseDatabase.getInstance().getReference()

        var query = ref?.child("chat_rooms")?.child(roomId)?.child("messages")!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

                Toast.makeText(this@ChatRoomInActivity, "Hata!" + p0.toString(), Toast
                        .LENGTH_SHORT).show()

            }

            override fun onDataChange(p0: DataSnapshot?) {

                for (message in p0!!.children) {

                    var currentMessage = ChatMessages()
                    var user_id = message.getValue(ChatMessages::class.java)!!.user_id

                    if (!uniqMessages!!.contains(message.key)) {

                        uniqMessages?.add(message.key)
                        if (user_id != null) {

                            currentMessage.message=message.getValue(ChatMessages::class.java)!!.message
                            currentMessage.user_id=message.getValue(ChatMessages::class.java)!!.user_id
                            currentMessage.date=message.getValue(ChatMessages::class.java)!!.date
                            currentMessage.user_name=message.getValue(ChatMessages::class.java)!!.user_name
                            var userInfos = ref!!.child("users")?.orderByKey()
                                    ?.equalTo(user_id)
                            userInfos?.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError?) {

                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0?.exists()!!) {
                                        var currentUser = p0!!.children?.iterator()?.next()
                                        currentMessage.user_name = currentUser?.getValue(Users::class.java)?.name
                                      //  currentMessage.profil_img = currentUser?.getValue (Users::class.java)?.profil_img
                                        mRecyclerAdapter?.notifyDataSetChanged()
                                    }
                                }

                            })

                            allMessages?.add(currentMessage)
                            mRecyclerAdapter?.notifyDataSetChanged()
                            rvMessages.scrollToPosition(mRecyclerAdapter!!.itemCount - 1)

                        } else {
                            currentMessage.user_id = ""
                            currentMessage.date = message.getValue(ChatMessages::class.java)?.date
                            currentMessage.message = message.getValue(ChatMessages::class.java)?.message
                            currentMessage.user_name = ""
                            currentMessage.profil_img = ""
                            allMessages?.add(currentMessage)
                            mRecyclerAdapter?.notifyDataSetChanged()

                        }


                    }


                }

            }
        })

        if (mRecyclerAdapter == null) {

            mRecyclerAdapter = MessagesRecyclerAdaptor(this, allMessages!!)
            rvMessages.adapter = mRecyclerAdapter
            rvMessages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rvMessages.scrollToPosition(mRecyclerAdapter?.itemCount!! - 1)

        }


    }


}

