package com.emrehmrc.finalproject.class_model

class Users {

    var name: String? = null
    var phone: String? = null
    var profil_img: String? = null
    var level: String? = null
    var user_id: String? = null
    var token: String? = null

    constructor() {}

    constructor(name: String, phone: String, profil_img: String, level: String, user_id: String) {

        this.name = name
        this.phone = phone
        this.profil_img = profil_img
        this.level = level
        this.user_id = user_id
    }
}
