package com.emrehmrc.finalproject.adaptors

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emrehmrc.finalproject.CustomToast
import com.emrehmrc.finalproject.R
import com.emrehmrc.finalproject.class_model.ChatMessages
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.messagesrecyclerview.view.*


class MessagesRecyclerAdaptor(context: Context, allMessages: ArrayList<ChatMessages>) : RecyclerView
.Adapter<MessagesRecyclerAdaptor.MessagesViewHolder>() {

    var allMessages = allMessages
    var mContext = context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {

        var inflater = LayoutInflater.from(mContext)

        var view: View? = null
        if (viewType == 1) {
            view = inflater.inflate(R.layout.messagesrecyclerview, parent, false)
        } else if (viewType == 2) {

            view = inflater.inflate(R.layout.messagesrecyclerview2, parent, false)
        }
        return MessagesViewHolder(view)
    }

    override fun getItemCount(): Int {

        return allMessages.size
    }

    override fun getItemViewType(position: Int): Int {

        if (allMessages.get(position).user_id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {

            return 1
        } else {
            return 2
        }
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        var currentMessage = allMessages.get(position)
        holder?.setData(currentMessage, position)
    }


    inner class MessagesViewHolder(itemview: View?) : RecyclerView.ViewHolder(itemview) {

        var oneMessage = itemview as ConstraintLayout
        var userName = oneMessage.tvUserName
        var message = oneMessage.tvMessage
        var date = oneMessage.tvDate
        var userImg = oneMessage.imgAbc

        fun setData(currentMessage: ChatMessages, position: Int) {

            userName.text = currentMessage.user_name
            message.text = currentMessage.message
            date.text = currentMessage.date
         try {
             var img = currentMessage.user_name?.substring(0, 1)?.toLowerCase()
             var resourceID = mContext.getResources().getIdentifier(
                     img,
                     "drawable",
                     mContext.getPackageName()
             )
             if (resourceID != 0) Picasso.get().load(resourceID).resize(48, 48).into(userImg)
         }
         catch (e:Exception){}
            if (!currentMessage.profil_img.isNullOrEmpty()) {
                //  Picasso.get().load(currentMessage.profil_img).resize(40, 40).into(userImg)


            }
            oneMessage.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {

                    if (currentMessage.user_id == FirebaseAuth.getInstance().currentUser?.uid) {

                        deleteMessage(currentMessage.user_id + "")
                    }

                    return true
                }

            })


        }


    }

    private fun deleteMessage(s: String) {
        var toast = CustomToast(mContext, "Bu Mesaj Silindi!")
        toast.show()

/*
            var ref=FirebaseDatabase.getInstance().reference
            ref.child("chat_rooms")
                    .child(s)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            var toast= CustomToast(mContext,"Bu Mesaj Silindi!")
                            toast.show()
                        }
                        else {

                            //    Toast.makeText(this, "Hata Oluştu! "+task.exception.toString(), Toast    .LENGTH_SHORT).show()
                            var toast= CustomToast(this,"Hata Oluştu! "+task.exception.toString())
                            toast.show()
                        }
                    }
                    */

    }
}