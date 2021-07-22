package com.example.voteon

import com.google.firebase.database.IgnoreExtraProperties
class User_detail(
    var email: String? = "",
    var name: String? = "",
    var password : String? = "",
    var uid: String? = "",
    var uri: String? = ""
)

class Groups(
    var admin: String? = "",
    var code: String? = "",
    var date: String? = "",
    var description: String? = "",
    var name: String? = "",
    var tag: String? = "",
    var uri: String? = ""
)

class Members(
    var members: String? = ""
)

class Contests(
    var admin: String? = "",
    var created: String? = "",
    var description: String? = "",
    var duration: String? = "",
    var end: String? = "",
    var endDate: String? = "",
    var group: String? = "",
    var name: String? = "",
    var status: String? = "",
    var uri: String? = ""
)

class Options(
    var name: String? = "",
    var position: String? = "",
    var uri: String? = ""
)
