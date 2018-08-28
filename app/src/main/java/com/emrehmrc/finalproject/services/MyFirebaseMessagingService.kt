package com.emrehmrc.finalproject.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.emrehmrc.finalproject.ChatRoomInActivity
import com.emrehmrc.finalproject.MainActivity
import com.emrehmrc.finalproject.R
import com.emrehmrc.finalproject.class_model.ChatRooms
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    var waitingMessages = 0
    override fun onMessageReceived(p0: RemoteMessage?) {


        if (!ChatRoomInActivity.isActivityActive){

            var title = p0?.notification?.title
            var body = p0?.notification?.body
            var data = p0?.data

            var ntitle = p0?.data?.get("title")
            var ncontent = p0?.data?.get("content")
            var ntype = p0?.data?.get("type")
            var nroomId = p0?.data?.get("chatRoomId")

            Log.e("FCM", ntitle + ncontent + ntype + nroomId)

            var ref = FirebaseDatabase.getInstance().reference
                    .child("chat_rooms")
                    .orderByKey()
                    .equalTo(nroomId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {

                            Log.e("FCM",p0?.details)
                        }

                        override fun onDataChange(p0: DataSnapshot?) {

                          //  var room = p0?.children?.iterator()?.next()
                            for (room in p0!!.children) {

                                var currentroom = ChatRooms()
                                var mymap = (room?.getValue() as java.util.HashMap<String, Object>)

                                currentroom.room_id = mymap.get("room_id").toString()
                                currentroom.creater_id = mymap.get("creater_id").toString()
                                currentroom.level = mymap.get("level").toString()
                                currentroom.roomName = mymap.get("roomName").toString()


                                var readMessage = room.child("users_in_room")
                                        .child(FirebaseAuth.getInstance().currentUser?.uid)
                                        .child("read_messages")
                                        .getValue().toString().toInt()

                                var totalMessage = room.child("messages").childrenCount.toInt()
                                waitingMessages = totalMessage - readMessage

                                sendNotification(ntitle, ncontent, currentroom)
                            }
                        }

                    })

        }


    }

    @SuppressLint("ResourceAsColor")
    private fun sendNotification(ntitle: String?, ncontent: String?, room: ChatRooms) {


        var notId = createNotificationId(room.room_id!!)
        Log.e("FCM",""+notId)

        var pending= Intent(this,MainActivity::class.java)
        pending.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        pending.putExtra("chatRoomId",room?.room_id)

        var pendingNotification=PendingIntent.getActivity(this,1,pending,PendingIntent.FLAG_UPDATE_CURRENT)



        var builder = NotificationCompat.Builder(this, room.roomName!!)
                    .setSmallIcon(R.drawable.bell)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.event))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentTitle(room.roomName + " odasından " + ntitle)
                    .setContentText("İÇERİK")
                    .setColor(R.color.colorAccent)
                    .setAutoCancel(true)
                    .setSubText("" + waitingMessages + " yeni mesaj")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(ncontent))
                    .setNumber(waitingMessages)
                    .setOnlyAlertOnce(true)
                .setContentIntent(pendingNotification)



        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(notId!!, builder.build())


    }

    private fun createNotificationId(key: String): Int? {

        var id = 0

        for (i in 4..8) {
            id = id + key[i].toInt()
        }


        return id
    }
}