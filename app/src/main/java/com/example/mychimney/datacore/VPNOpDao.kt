package com.example.mychimney.datacore

import android.content.ContentValues
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select

class VPNOpDao {

    fun addVPN(vpn: VPNProfile): Int {

        var r: Int = -1
        var cv = ContentValues()
        cv.put(VPNTables.NAME, vpn.name)
        cv.put(VPNTables.SERVER, vpn.server)
        cv.put(VPNTables.REMOTEPORT, vpn.remoteport)
        cv.put(VPNTables.PASSWORD, vpn.password)
        cv.put(VPNTables.DNS, vpn.remoteDNS)

        DatabaseOpenHelper.instance.use {
            var v = insert(VPNTables.TABLE_NAME, null, cv)
            r = v.toInt()
        }

        return r
    }

    fun deleteVPN(vpns: List<VPNProfile>): Int {
        for (v in vpns) {
            deleteOne(v.id_.toInt())
        }

        return 0
    }

    fun updateVPN(vpn: VPNProfile) : Int {
        var r = 0

        var values = ContentValues()
        values.put(VPNTables.NAME, vpn.name)
        values.put(VPNTables.SERVER, vpn.server)
        values.put(VPNTables.REMOTEPORT, vpn.remoteport)
        values.put(VPNTables.PASSWORD, vpn.password)
        values.put(VPNTables.DNS, vpn.remoteDNS)

        DatabaseOpenHelper.instance.use {
            r = update(VPNTables.TABLE_NAME, values,
                "${VPNTables.ID} =" + vpn.id_, null)

        }

        return r
    }

    fun deleteOne( id : Int ): Int {

        var r : Int = -1
        DatabaseOpenHelper.instance.use {
            r = delete(VPNTables.TABLE_NAME,
                "${VPNTables.ID} = {id}",
                "id" to id)
        }

        return r
    }

    fun query(): List<VPNProfile> {

        var vpns :List<VPNProfile> = ArrayList<VPNProfile>()

        DatabaseOpenHelper.instance.use {
            vpns = select(VPNTables.TABLE_NAME).parseList(object : MapRowParser<VPNProfile> {
                override fun parseRow(columns: Map<String, Any?>): VPNProfile {

                    val id = columns.getValue(VPNTables.ID)
                    val name = columns.getValue(VPNTables.NAME)
                    val server = columns.getValue(VPNTables.SERVER)
                    val port = columns.getValue(VPNTables.REMOTEPORT)
                    val pass = columns.getValue(VPNTables.PASSWORD)
                    val dns = columns.getValue(VPNTables.DNS)

                    var v = VPNProfile()
                    v.name = name.toString()
                    v.id_ = id.toString().toLong()
                    v.server = server.toString()
                    v.remoteport = port.toString().toInt()
                    v.password = pass.toString()
                    v.remoteDNS = dns.toString()
                    return v
                }
            })
        }

        return vpns
    }
}