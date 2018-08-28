package com.emrehmrc.finalproject


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.emrehmrc.finalproject.class_model.ChatMessages
import com.emrehmrc.finalproject.class_model.ChatRooms
import com.emrehmrc.finalproject.class_model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.chatroomrecyclerview.*
import java.text.SimpleDateFormat
import java.util.*


class AddChatRoomFragment : DialogFragment() {


    lateinit var edtChatName: EditText
    lateinit var seekBarLevel: SeekBar
    lateinit var tvLevel: TextView
    lateinit var btnAddChatRoom: Button
    var mContext: FragmentActivity? = null
    var seekLevel = 1
    var userLevel = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_add_chat_room, container, false)

        mContext = activity
        edtChatName = view.findViewById(R.id.edtChatName)
        seekBarLevel = view.findViewById(R.id.seekBarLevel)
        tvLevel = view.findViewById(R.id.tvLevel)
        btnAddChatRoom = view.findViewById(R.id.btnAddChatRoom)

        getUserLevel()
        btnAddChatRoom.setOnClickListener {
            if (seekLevel < userLevel) {
                if (edtChatName.text.isNullOrEmpty()) {
                    //Toast.makeText(mContext, "Sohbet Oda Adı Boş Bırakılamaz", Toast .LENGTH_SHORT).show()
                    var toast = CustomToast(mContext!!, "Oda Adı Boş Bırakılamaz!")
                    toast.show()


                } else {
                    addChatRoom()
                }

            } else {

                var toast = CustomToast(mContext!!, "Admin Değilsiniz! Oda Ekleyemezsiniz!")
                toast.show()

            }
        }

        seekBarLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekLevel == 0) {
                    seekLevel = 1
                }
                seekLevel = progress
                tvLevel.setText(seekLevel.toString())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })


        return view
    }

    private fun addChatRoom() {

        var ref = FirebaseDatabase.getInstance().reference
        var roomID = ref.child("chat_rooms").push().key//uuid oluşturma

        var newRoom = ChatRooms()
        newRoom.creater_id = FirebaseAuth.getInstance().currentUser?.uid
        newRoom.level = seekLevel.toString()
        newRoom.roomName = edtChatName.text.toString()
        newRoom.room_id = roomID
        newRoom.room_img=""


        ref.child("chat_rooms").child(roomID).setValue(newRoom)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        dismiss()
                        //Toast.makeText(mContext, "Sohbet Odası Oluşturuldu", Toast.LENGTH_SHORT).show()
                        var toast = CustomToast(mContext!!, "Sohbet Odası Başarıyla Oluşturuldu")
                        toast.show()
                        var firstMessage = ChatMessages()
                        var messageID = ref.child("chat_rooms").push().key//uuid oluşturma
                        firstMessage.message = newRoom.roomName+" Odasına Hoşgeldiniz :)"
                        firstMessage.date = getDate()
                        ref.child("chat_rooms")
                                .child(roomID)
                                .child("messages")
                                .child(messageID)
                                .setValue(firstMessage)


                    } else {

                    //    Toast.makeText(mContext, "HATA OLUŞTU! " + task.exception?.message, Toast.LENGTH_SHORT).show()
                        var toast = CustomToast(mContext!!, "HATA OLUŞTU! " + task.exception?.message)
                        toast.show()
                    }
                }

        (activity as ChatRoomsActivity).getAllChatRooms()

    }

    private fun getUserLevel() {
        var ref = FirebaseDatabase.getInstance().reference
        var query = ref.child("users").orderByKey().equalTo(FirebaseAuth.getInstance()
                .currentUser?.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                for (single in p0!!.children) {

                    var cameuser = single.getValue(Users::class.java)
                    userLevel = cameuser?.level!!.toInt()


                }

            }

        })

    }

    private fun getDate(): String {

        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr"))
        return sdf.format(Date())


    }


}
