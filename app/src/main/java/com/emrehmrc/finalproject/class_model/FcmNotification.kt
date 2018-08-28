package com.emrehmrc.finalproject.class_model

import com.google.gson.annotations.SerializedName

class FcmNotification {


    @SerializedName("data")
    var data: Data? = null
    @SerializedName("to")
    var to: String? = null

    constructor(to: String, data:Data){

        this.to=to
        this.data=data
    }

    class Data {
        @SerializedName("type")
        var type: String?=null
        @SerializedName("content")
        var content: String?=null
        @SerializedName("title")
        var title: String?=null
        @SerializedName("chatRoomId")
        var chatRoomId: String?=null
        constructor(title: String, content: String, type: String,chatRoomId:String?) {

            this.title=title
            this.content=content
            this.type=type
            this.chatRoomId=chatRoomId

        }
    }
}
