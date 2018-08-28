package com.emrehmrc.finalproject.adaptors

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emrehmrc.finalproject.ChatRoomInActivity
import com.emrehmrc.finalproject.ChatRoomsActivity
import com.emrehmrc.finalproject.R
import com.emrehmrc.finalproject.class_model.ChatRooms
import com.emrehmrc.finalproject.class_model.Users
import com.emrehmrc.finalproject.dialogs.UpdateChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chatroomrecyclerview.view.*


class ChatRoomsRecyclerAdaptor(mActivity: AppCompatActivity, allChatRooms: ArrayList<ChatRooms>) :
        RecyclerView
        .Adapter<ChatRoomsRecyclerAdaptor.ChatRoomsHolder>() {

    var mActivity = mActivity
    override fun onBindViewHolder(holder: ChatRoomsHolder, position: Int) {
        var currentRoom = allRooms.get(position)
        holder?.setData(currentRoom, position)
    }

    var allRooms = allChatRooms
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomsHolder {
        var inflater = LayoutInflater.from(parent?.context)
        var oneRoom = inflater.inflate(R.layout.chatroomrecyclerview, parent, false)
        return ChatRoomsHolder(oneRoom)
    }

    override fun getItemCount(): Int {
        return allRooms.size
    }


    inner class ChatRoomsHolder(itemview: View?) : RecyclerView.ViewHolder(itemview) {

        var oneRoom = itemview as ConstraintLayout
        var currentRoomCreator = oneRoom.tvCreatorName
        var currentRoomimg = oneRoom.imgCreator
        var currentRoomDeleteimg = oneRoom.imgChatRoomDelete
        var currentRoomTotalMessages = oneRoom.tvNumberofMessages
        var currenRoomName = oneRoom.tvChatRoomName
        var progRoomImg = oneRoom.progRoomImg

        fun setData(currentRoom: ChatRooms, position: Int) {

            currenRoomName.text = currentRoom.roomName?.toUpperCase()
            currentRoomTotalMessages.text = (currentRoom.messages)?.size.toString()
            try {
                Picasso.get().load(currentRoom.room_img).into(currentRoomimg)
            } catch (ex: Exception) {
            }


            if (currentRoom.creater_id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {

                currentRoomDeleteimg.setImageResource(R.drawable.dustbin)
            } else  {
                currentRoomDeleteimg.setImageResource(R.drawable.join)

            }
            currentRoomDeleteimg.setOnClickListener {
                if (currentRoom.creater_id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {

                    var dialog = AlertDialog.Builder(itemView.context)
                    dialog.setTitle("Sohbet Odası Sil ?")
                    dialog.setMessage("Silmek İsteğinize Emin misiniz?")
                    dialog.setCancelable(true)
                    dialog.setPositiveButton("EVET SİL", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {

                            (mActivity as ChatRoomsActivity).deleteChatRoom(currentRoom.room_id.toString())
                        }

                    })
                    dialog.setNegativeButton("HAYIR SİLME", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {

                        }
                    })
                    dialog.show()

                } else {
                    joinUserToRoom(currentRoom)
                    var intent = Intent(mActivity, ChatRoomInActivity::class.java)
                    intent.putExtra("room_id", currentRoom.room_id)
                    mActivity.startActivity(intent)
                }
            }

            oneRoom.setOnClickListener {
                joinUserToRoom(currentRoom)
                var intent = Intent(mActivity, ChatRoomInActivity::class.java)
                intent.putExtra("room_id", currentRoom.room_id)
                mActivity.startActivity(intent)


            }


            var ref = FirebaseDatabase.getInstance().reference
            var query = ref.child("users")
                    .orderByKey()
                    .equalTo(currentRoom.creater_id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(p0: DataSnapshot?) {

                            for (users in p0!!.children) {
                                var img = users.getValue(Users::class.java)?.profil_img.toString()
                                //   Picasso.get().load(img).into(currentRoomimg)
                                progRoomImg.visibility = View.GONE

                                currentRoomCreator.text = users.getValue(Users::class.java)?.name?.toUpperCase()

                            }
                        }


                    })

            currentRoomimg.setOnClickListener {

                if (currentRoom.creater_id.equals(FirebaseAuth.getInstance().currentUser?.uid)){

                    val bundle = Bundle()
                    bundle.putString("roomid", currentRoom.room_id)
                    val fragobj = UpdateChatRoom()
                    fragobj.setArguments(bundle)
                    fragobj.show(mActivity.supportFragmentManager, "odagüncelleme")


                }


                /*
                var updatechatroom=UpdateChatRoom()
                updatechatroom.show(mActivity.supportFragmentManager,"odagüncelleme")
                */
            }

        }

        private fun joinUserToRoom(currentRoom: ChatRooms) {

            var ref = FirebaseDatabase.getInstance().reference
                    .child("chat_rooms")
                    .child(currentRoom.room_id)
                    .child("users_in_room")
                    .child(FirebaseAuth.getInstance().currentUser?.uid)
                    .child("read_messages")
                    .setValue((currentRoom.messages)?.size?.toString())

        }

    }
}