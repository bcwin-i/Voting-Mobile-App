package com.example.voteon

class ContestDataClass {
    var admin: String = ""
    var created: String = ""
    var description: String = ""
    var duration: String = ""
    var end: String = ""
    var endDate: String = ""
    var group: String = ""
    var name: String = ""
    var status: String = ""
    var uri: String = ""

    constructor(admin: String, created: String, description: String, duration: String, end: String, endDate: String, group: String, name: String, status: String, uri: String){
        this.admin = admin
        this.created = created
        this.description = description
        this.duration = duration
        this.end = end
        this.endDate = endDate
        this.group = group
        this.name = name
        this.status = status
        this.uri
    }

    constructor()

}