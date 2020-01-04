package com.example.mychimney.datacore

data class VPNProfile(val map: MutableMap<String, Any?>) {
    var id_: Long by map
    var name: String by map
    var server: String by map
    var remoteport: Int by map
    var password: String by map
    var remoteDNS: String by map

    constructor() : this(HashMap())


    constructor(id:Long,name: String,server:String, remoteport:Int, password:String,remoteDNS:String) : this(HashMap()) {
        this.id_ = id
        this.name = name
        this.server = server
        this.remoteport = remoteport
        this.password = password
        this.remoteDNS = remoteDNS
    }
}