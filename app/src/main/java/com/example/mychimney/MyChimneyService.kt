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
import tun4android.IDataFlow
import tun4android.ISocket
import tun4android.Tun4android
import java.io.File

class MyChimneyService : VpnService() {

    companion object {
        const val INIT: Int = 0
        const val RUNNING: Int = 1
        const val ERROR: Int = 2


        const val NETADDRESS = "10.0.0.2"
        const val MTU = 1500
    }


    var launcher : LaunchReceiver? = null
    val  Tag :String  = "MyChimneyService"
    var vpnState : Int  = 0
    var chsevice : Boolean = false
    var netcon : ParcelFileDescriptor? = null
    var netstack: Boolean = false
    var exit : Boolean = false


    override fun onCreate() {
        registerActionEventListener()
        super.onCreate()

        Tun4android.register(ISocket { p0 ->
            var r = this@MyChimneyService.protect(p0.toInt())
            Log.i(Tag , "call back protect socket ")
            r
        }, IDataFlow { p0, p1 -> sendTrafficData(p0, p1) })

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


            if (!this.chsevice) {
                chsevice = Tun4android.startChimney(server.trim(),
                    sport.toLong(),
                    "127.0.0.1", 1080.toLong(),
                    pass.trim(), CustomApp.instance.filesDir.absolutePath )
            }

            if (this.netcon == null) {

                var builder = Builder().addAddress(NETADDRESS, 0)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer(dns)
                    .setSession("Chimney")
                    .setMtu(MTU)
                Log.i(Tag, "net connection launched!" + (this.netcon != null))

                this.netcon = builder.establish()
            }

            if (!this.netstack && this.netcon != null) {
                Tun4android.startNetstackService(this.netcon!!.fd.toLong(), "127.0.0.1:1080", dns)
                Log.i(Tag, "netstack initialize successed")
                this.netstack = true
            }

            if (this.chsevice  && this.netstack
                && this.netcon != null) {
                this.vpnState = RUNNING
            }
            else {
                this.vpnState = ERROR
            }

            Log.i(Tag, "vpn state is : " + vpnState)


        }
        return vpnState
    }

    private  fun stopService(){
        synchronized(this){

            if (this.chsevice) {
                Tun4android.stopChimney()
                this.chsevice = false
            }

            if (this.netstack) {
                Tun4android.stopNetStackService()
                this.netstack = false
            }

            Thread.sleep(2000)

            if (this.netcon != null) {
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