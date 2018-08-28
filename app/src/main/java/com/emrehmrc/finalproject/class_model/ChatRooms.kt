package com.emrehmrc.finalproject.class_model

class ChatRooms {

    var roomName: String? = null
    var creater_id: String? = null
    var level: String? = null
    var room_id: String? = null
    var messages:List<ChatMessages>?=null
    var room_img:String?=null

    constructor(roomName: String, creater_id: String, level: String, room_id: String,
                messages:List<ChatMessages>,room_img:String) {

        this.roomName = roomName
        this.creater_id = creater_id
        this.level = level
        this.room_id = room_id
        this.messages=messages
        this.room_img=room_img
    }

    constructor() {

    }
}
