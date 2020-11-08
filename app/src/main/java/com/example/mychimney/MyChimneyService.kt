package com.example.mychimney

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

import org.json.JSONObject
import tun4socks.BindSocket
import tun4socks.Tun4socks
import tun4socks.VPNParam
import java.io.File

class MyChimneyService : VpnService(), BindSocket {

    companion object {
        const val INIT: Int = 0
        const val RUNNING: Int = 1
        const val ERROR: Int = 2


        const val NETADDRESS = "192.168.12.12"
        const val MTU = 1500
    }


    var launcher : LaunchReceiver? = null
    val  Tag :String  = "MyChimneyService"
    var vpnState : Int  = 0
    var netcon : ParcelFileDescriptor? = null
    var exit : Boolean = false

    override fun protect(filedescriptor: Long): Long {
        var v = super.protect(filedescriptor.toInt())
        if (v){
            return 0
        }
        return -1
    }


    override fun onCreate() {
        registerActionEventListener()
        super.onCreate()
        Log.i(Tag , "func is test !!!!!!!!!!!!!!!!!!!!!!!!!!!!! ")
        this.updateserviceState()
    }

    fun updateserviceState(){

        Thread {

            while (!this.exit) {
                Thread.sleep(4000)
                val p0 = (1000..9000).random()
                var p1 = (1000..9000).random()
                if (this.vpnState == RUNNING) {
                    this.sendFlowstatus(p0.toLong(), p1.toLong())
                }
            }
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (RUNNING == this.vpnState && this.netcon != null) {
            this.sendServicestatus(this.vpnState)
            return Service.START_NOT_STICKY
        }

        this.startService()
        this.sendServicestatus(this.vpnState)

        return START_NOT_STICKY
    }

    private  fun sendServicestatus(status: Int){
        var intent = Intent()
        intent.action = "com.chineseelements.CHIMNEY"
        intent.putExtra("status", status.toString())
        sendBroadcast(intent)
    }

    private  fun sendFlowstatus(up:Long, down:Long){
        var intent = Intent()
        intent.action = "com.chineseelements.CHIMNEY"
        intent.putExtra("flow", "value")
        intent.putExtra("up",up)
        intent.putExtra("down", down)
        sendBroadcast(intent)
    }


    fun startService(): Int {

        synchronized(this) {

            if (RUNNING == vpnState){
                return this.vpnState
            }

            var vpn = this.prepareVPNData(null)

            var server = vpn.getString("server")
            var sport = vpn.getInt("server_port")
            var pass = vpn.getString("password")
            var dns = vpn.getString("dns")

            if (this.netcon == null) {

                var builder = Builder().addAddress(NETADDRESS, 0)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer(dns)
                    .setSession("Chimney")
                    .setMtu(MTU)
                Log.i(Tag, "net connection launched!" + (this.netcon != null))

                this.netcon = builder.establish()

                var vpnpara = VPNParam()
                vpnpara.fileDescriptor = this.netcon!!.fd.toLong()
                vpnpara.passWD = pass
                vpnpara.remoteServer = server
                vpnpara.port = sport.toLong()
                vpnpara.udpPort = sport.toLong()
                vpnpara.callBack = this
                var i = Tun4socks.startVPN(vpnpara)
                if (0L == i) {
                    this.vpnState = RUNNING
                }
                else {
                    this.vpnState = ERROR
               }
            }
            Log.i(Tag, "vpn state is : " + vpnState)
        }

        this.sendServicestatus(this.vpnState)

        return vpnState
    }

    private  fun stopService(){
        synchronized(this){
            if (this.netcon != null) {
                Tun4socks.stopVPN()
                this.netcon!!.close()
                this.netcon = null
            }
            this.vpnState = INIT
        }
        this.sendServicestatus(this.vpnState)
        this.exit = true
    }

    override fun onRevoke(){
        stopService()
        stopSelf()
    }


    override fun onDestroy() {
        this.stopService()

        unRegisterActionEventListener()
        super.onDestroy()
    }

    fun sendTrafficData(p0: Long, p1: Long)  {
        Log.i(Tag, (p0 + p1).toString())
    }


    private fun prepareVPNData(intent: Intent?): JSONObject {

        var file = File(this.filesDir, "vpn.json")
        var content = file.readText()
        var json = JSONObject(content)
        return json
    }


    fun registerActionEventListener(){
        var filter =  IntentFilter()
        filter.addAction("com.chineseelements.LAUNCH_CMD")
        this.launcher = LaunchReceiver()
        registerReceiver(this.launcher!!, filter)
    }

    fun unRegisterActionEventListener(){
        unregisterReceiver(this.launcher!!)
    }


    inner class LaunchReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            var value = intent!!.getStringExtra("CMD")
            if (value!!.contains("stop")) {
                this@MyChimneyService.stopService()
                this@MyChimneyService.stopSelf()
                this@MyChimneyService.sendServicestatus(INIT)
            }
            else {
                this@MyChimneyService.sendServicestatus(this@MyChimneyService.vpnState)
            }
        }
    }
}