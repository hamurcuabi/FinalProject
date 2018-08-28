package com.emrehmrc.finalproject

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.emrehmrc.finalproject.adaptors.ChatRoomsRecyclerAdaptor
import com.emrehmrc.finalproject.class_model.ChatMessages
import com.emrehmrc.finalproject.class_model.ChatRooms
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat_rooms.*
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import com.emrehmrc.finalproject.class_model.Users
import com.squareup.picasso.Picasso


class ChatRoomsActivity : AppCompatActivity() {

    lateinit var allChatRooms:ArrayList<ChatRooms>
     var userLevel=""
    var uniqMessages: HashSet<String>? = null
    lateinit var mesagesState: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_chat_rooms)

        if(!isAdmin()){
            fabAddChatRoom.visibility=View.INVISIBLE
        }
        else fabAddChatRoom.visibility=View.VISIBLE
        fabAddChatRoom.setOnClickListener {


            var dialodAddChat = AddChatRoomFragment()
            dialodAddChat.show(supportFragmentManager, "ChatRoomEkle")

        }

        imgHome.setOnClickListener {
            var intent = Intent(this@ChatRoomsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        imgTurnback.setOnClickListener {
            var intent = Intent(this@ChatRoomsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        getAllChatRooms()



    }


    override fun onResume() {
        super.onResume()
        getAllChatRooms()

    }
    private fun isAdmin():Boolean {


        var referans = FirebaseDatabase.getInstance().reference
        var query = referans.child("users")
                .child(FirebaseAuth.getInstance().currentUser?.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                 userLevel = p0?.child("level")?.getValue().toString()


            }

        })


        if(userLevel.equals("0")) return false
        else return  true

    }


    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        var intent = Intent(this@ChatRoomsActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }


     fun getAllChatRooms() {

         uniqMessages = HashSet<String>()
        allChatRooms=ArrayList<ChatRooms>()
        var ref = FirebaseDatabase.getInstance().reference
        var query = ref.child("chat_rooms").addListenerForSingleValueEvent(object
            : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {

                for (rooms in p0!!.children) {

                    var currentroom = ChatRooms()
                    var mymap = (rooms.getValue() as HashMap<String, Object>)

                    currentroom.room_id = mymap.get("room_id").toString()
                    currentroom.creater_id = mymap.get("creater_id").toString()
                    currentroom.level = mymap.get("level").toString()
                    currentroom.roomName = mymap.get("roomName").toString()
                    currentroom.room_img = mymap.get("room_img").toString()
                    if (!uniqMessages!!.contains(currentroom.room_id!!)) {

                        uniqMessages?.add(currentroom.room_id!!)
                        var allMessagesRead = ArrayList<ChatMessages>()
                        for (messages in rooms.child("messages").children) {

                            var currentMessage = ChatMessages()
                            currentMessage.date = messages.getValue(ChatMessages::class.java)?.date
                            currentMessage.message = messages.getValue(ChatMessages::class.java)?.message
                            currentMessage.profil_img = messages.getValue(ChatMessages::class.java)?.profil_img
                            currentMessage.user_id = messages.getValue(ChatMessages::class.java)?.user_id
                            currentMessage.user_name = messages.getValue(ChatMessages::class.java)?.user_name

                            allMessagesRead.add(currentMessage)
                        }

                        currentroom.messages = allMessagesRead
                        allChatRooms.add(currentroom)


                    }

                }
                listAllRooms()





            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })
    }


    private fun listAllRooms() {

        var adapter= ChatRoomsRecyclerAdaptor(this@ChatRoomsActivity, allChatRooms)
        rvChatRoom.adapter=adapter
        rvChatRoom.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        progLoadRooms.visibility= View.GONE
    }
    public fun deleteChatRoom(room_id:String){



        var ref=FirebaseDatabase.getInstance().reference
        ref.child("chat_rooms")
                .child(room_id)
                .removeValue()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        getAllChatRooms()
                    }
                    else {

                    //    Toast.makeText(this, "Hata Oluştu! "+task.exception.toString(), Toast    .LENGTH_SHORT).show()
                        var toast= CustomToast(this,"Hata Oluştu! "+task.exception.toString())
                        toast.show()
                    }
                }


    }

}
