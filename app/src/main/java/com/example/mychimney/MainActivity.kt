package com.example.mychimney

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mychimney.datacore.FormatUtils
import com.example.mychimney.datacore.VPNOpDao
import com.example.mychimney.datacore.VPNProfile
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity(), UIUpdateInterface , UpdateConnectStatus{

    var servicestate : String = "-1"
    val Tag :String = "MainActivity"
    var launcher : MyServiceStatusReceiver = MyServiceStatusReceiver()

    override fun register(v: UIUpdateInterface) {
        NotifyCenter.instance.register(v)

    }

    override fun unregister(v: UIUpdateInterface) {
        NotifyCenter.instance.unregister(v)

    }

    fun registerActionEventListener(){
        var filter =  IntentFilter()
        filter.addAction("com.chineseelements.CHIMNEY")
        registerReceiver(this.launcher!!, filter)
    }

    fun unRegisterActionEventListener(){
        unregisterReceiver(this.launcher!!)
    }


    override fun updateConnectStatus(status: String) {

        this.servicestate = status

        when(status) {
            "1"-> {
                launchvpn.background = this.getDrawable(R.drawable.button_circle_shape_run)
                Log.i(Tag, "status: 1 " + status)
            }
            "2"-> {
                launchvpn.background = this.getDrawable(R.drawable.button_circle_shape_error)
                Log.i(Tag, "status: 2 " + status)
            }
            "3"-> {
                launchvpn.background = this.getDrawable(R.drawable.button_circle_shape_process)
                Log.i(Tag, "status: 3 " + status)
            }
            else -> {
                launchvpn.background = this.getDrawable(R.drawable.button_circle_shape)
                Log.i(Tag, "status others:  " + status)
            }
        }
    }

    override fun updateFlow(p0: Long, p1: Long) {
        var ada = vpnList.adapter as VpnAdapter
        ada.updateFlow(p0, p1)
    }

    override fun nofityUI() {
        var a = this.vpnList.adapter as VpnAdapter
        a.update()
        a.notifyDataSetChanged()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        registerActionEventListener()
        NotifyCenter.instance.register(this)
        NotifyCenter.instance.registerSatusListener(this)


        launchvpn.setOnClickListener { view ->
            if (vpnList.count < 1) {
                Snackbar.make(view, "Please set VPN  profile first.", Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.app_name), null).show()
            } else {
                if (CustomApp.instance.selectedItemId > -1
                    && CustomApp.instance.selectedItemId < vpnList.count) {
                    var vpn = vpnList.adapter.getItem(CustomApp.instance.selectedItemId ) as VPNProfile
                    if (vpn != null){

                        Log.i(Tag, "launch app !!!")
                        this.genserviceConfigFile(vpn)
                        if ("1" == this.servicestate) {
                            Log.i(Tag, "stop vpn service!!!")
                            this.updateConnectStatus("3")
                            this.stopService()

                        }
                        else {
                            this.updateConnectStatus("3")
                            this.startService()
                            Log.i(Tag, "start vpn service !!!")
                        }
                    }

                } else {
                    Snackbar.make(view, "Please select VPN first.", Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.app_name), null).show()
                }
            }
        }

        addvpnbtn.setOnClickListener  {
            val mIntent = Intent(this@MainActivity, EditVPNActivity::class.java)
            mIntent.putExtra("new", true)
            startActivity(mIntent)
        }
        vpnList.adapter = VpnAdapter(this.baseContext)
        NotifyCenter.instance.register(this)
        vpnList.setOnItemClickListener { _, _, _, id ->

            if (-1 != id.toInt()) {
                CustomApp.instance.selectedItemId = id.toInt()
            }
        }
        this.updateCurrentStatus()
    }

    fun startService() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, 0)
        } else {
            onActivityResult(0, Activity.RESULT_OK, null)
        }
    }

    fun stopService() {
        var intent = Intent()
        intent.action = "com.chineseelements.LAUNCH_CMD"
        intent.putExtra("CMD", "stop")
        sendBroadcast(intent)
    }

    fun updateCurrentStatus() {
        var intent = Intent()
        intent.action = "com.chineseelements.LAUNCH_CMD"
        intent.putExtra("CMD", "update")
        sendBroadcast(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var intent = Intent(this, MyChimneyService::class.java)
            startService(intent)

        } else {
            Snackbar.make(this.vpnList, "permission denied!!", Snackbar.LENGTH_LONG).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unRegisterActionEventListener()
        NotifyCenter.instance.unregister(this)
    }


    fun genserviceConfigFile(vpn: VPNProfile) {
        var file = File(this.applicationContext.filesDir, "vpn.json")
        file.delete()

        var json = JSONObject()
        json.put("server", vpn.server)
        json.put("server_port", vpn.remoteport)
        json.put("local_port", 1080)
        json.put("local_address", "127.0.0.1")
        json.put("password", vpn.password.trim())
        json.put("dns", vpn.remoteDNS)


        file.writeText(json.toString())
    }


    inner class VpnAdapter(var ctx: Context) : BaseAdapter() {

        private var Inflator: LayoutInflater? = null
        private var viewList: HashMap<Int, ViewHolder>
        private var listData: List<VPNProfile>
        private var up: Long = -1
        private var down: Long = -1

        init {
            this.Inflator = LayoutInflater.from(ctx)
            this.viewList = HashMap<Int, ViewHolder>()
            var dao = VPNOpDao()
            this.listData = dao.query()
        }

        fun updateFlow(up:Long, down:Long){
            this.up = up
            this.down = down
            this.notifyDataSetChanged()
        }

        fun update() {
            var dao = VPNOpDao()
            this.listData = dao.query()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder = ViewHolder()
            if (convertView == null) {
                var view = this.Inflator!!.inflate(R.layout.vpnlist_item, parent, false)
                viewHolder.dataFlow = view.findViewById(R.id.dataflow)
                viewHolder.all = view
                viewHolder.vpnName = view.findViewById(R.id.vpnName)
                viewHolder.deletebtn = view.findViewById(R.id.vpndeletevpn)
                viewHolder.editbtn = view.findViewById(R.id.vpneditbtn)
                viewHolder.position = position
                viewHolder.editbtn!!.tag = viewHolder
                viewHolder.deletebtn!!.tag = viewHolder
                view.tag = viewHolder
                viewHolder.adapter = this
                this.viewList.put(position, viewHolder)

                viewHolder.editbtn!!.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        var holder = v!!.tag as ViewHolder
                        if (holder.position >= 0 && holder.position < holder.adapter!!.count) {
                            val mIntent = Intent(holder.all!!.context, EditVPNActivity::class.java)
                            mIntent.putExtra("new", false)
                            mIntent.putExtra("data", holder.position)
                            holder.all!!.context.startActivity(mIntent)
                        }
                    }
                })

                viewHolder.deletebtn!!.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        var holder = v!!.tag as ViewHolder
                        if (holder.position >= 0 && holder.position < holder.adapter!!.count) {
                            var vpn = holder.adapter!!.getItem(holder.position) as VPNProfile
                            var dao = VPNOpDao()
                            dao.deleteOne(vpn.id_.toInt())
                            holder.adapter!!.update()
                            holder.adapter!!.notifyDataSetChanged()
                            Toast.makeText(
                                parent!!.context,
                                "[" + vpn.name + "]" + " was removed successfully.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })

            } else {
                viewHolder = convertView.tag as ViewHolder

            }

            viewHolder.vpnName!!.text = this.listData[position].name
            viewHolder.dataFlow!!.text = " ↑ 12 Kb/s ↓ 13 KB/s "

            if (CustomApp.instance.selectedItemId == position) {
                var out = "↑ " + FormatUtils().Format(this.up) +
                        "  ↓ " + FormatUtils().Format(this.down)

                viewHolder.dataFlow!!.text = out
            }


            return viewHolder.all!!
        }

        override fun getItem(position: Int): Any {
            return this.listData[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return this.listData.size
        }
    }


    inner class ViewHolder {
        var vpnName: TextView? = null
        var dataFlow: TextView?
        var deletebtn: ImageButton?
        var editbtn: ImageButton?
        var all: View?
        var position: Int
        var adapter: VpnAdapter?

        constructor() {
            this.vpnName = null
            this.dataFlow = null
            this.deletebtn = null
            this.editbtn = null
            this.all = null
            this.position = -1
            this.adapter = null
        }

    }
}
