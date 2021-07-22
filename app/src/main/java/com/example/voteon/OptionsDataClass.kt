package com.example.voteon

class OptionsDataClass {
    var name: String = ""
    var position: String = ""
    var uri: String? = ""

    constructor(name: String, position: String, uri: String)
    {
        this.name = name
        this.position = position
        this.uri = uri
    }

    constructor()

}