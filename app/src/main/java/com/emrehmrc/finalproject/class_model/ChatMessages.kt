package com.emrehmrc.finalproject.class_model

class ChatMessages {

    var message: String? = null
    var user_id: String? = null
    var date: String? = null
    var profil_img: String? = null
    var user_name: String? = null


    constructor(message: String, user_id: String, date: String, profil_img: String, user_name: String) {
        this.message = message
        this.user_id = user_id
        this.date = date
        this.profil_img = profil_img
        this.user_name = user_name
    }

    constructor() {}
}
