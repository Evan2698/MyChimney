package com.example.mychimney

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mychimney.datacore.VPNOpDao
import com.example.mychimney.datacore.VPNProfile
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit_vpn.*
import kotlinx.android.synthetic.main.content_edit_vpn.*
import java.util.regex.Pattern

class EditVPNActivity : AppCompatActivity() {

    var isNew : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_vpn)
        //setSupportActionBar(toolSaveBar)
        isNew = this.intent!!.getBooleanExtra("new", true)

        var vpn : VPNProfile? = null

        if (isNew) {
            remotedns.setText("1.1.1.1")
            this.passwd.setText("Evan\$%#@!#\$@!123")
        }
        else {
            var position = this.intent.getIntExtra("data", -1)
            var dao = VPNOpDao()
            var list = dao.query()
            if (position != -1 && list.count() > position) {
                vpn = list[position]

                this.vpnname.setText(vpn.name)
                this.servername.setText(vpn.server)
                this.remoteport.setText(vpn.remoteport.toString())
                this.passwd.setText(vpn.password)
                this.remotedns.setText(vpn.remoteDNS)
            }
        }

        saveVPN.setOnClickListener { view ->

            if (!checkBlankValue()){
                Snackbar.make(view, "please fill every item", Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.app_name), null).show()
                return@setOnClickListener
            }

            if (!checkPortValue()){
                Snackbar.make(view, "the port value is 1-65535.", Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.app_name), null).show()
                return@setOnClickListener
            }

            if (!checkIPAddress()) {
                Snackbar.make(view, "please check ip address or URL", Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.app_name), null).show()
                return@setOnClickListener
            }

            saveToDB(vpn)

            NotifyCenter.instance.nofityUI()

            Toast.makeText(this.applicationContext, "save success!", Toast.LENGTH_LONG).show()

            finish()

        }
    }


    fun saveToDB(v: VPNProfile?) {

        var vpn = VPNProfile()
        vpn.name = this.vpnname.text.toString().trim()
        vpn.server = this.servername.text.toString().trim()
        vpn.remoteport = this.remoteport.text.toString().toInt()
        vpn.remoteDNS = this.remotedns.text.toString().trim()
        vpn.password = this.passwd.text.toString().trim()


        var dao = VPNOpDao()
        if (this.isNew) {
            dao.addVPN(vpn)
        }
        else {
            vpn.id_ = v!!.id_
            dao.updateVPN(vpn)
        }
    }

    fun checkIPAddress() :Boolean {

        var patten =  Pattern.compile(
            "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}\$")

        var name = this.servername.text.toString()
        var dns = this.remotedns.text.toString()

        if ((name.startsWith("http") ||
                    patten.matcher(name).matches()) && (dns.startsWith("http")
                    || patten.matcher(dns).matches() )){
            return true
        }

        return false
    }

    fun checkPortValue() : Boolean {
        var port = this.remoteport.text.toString().toInt()
        if (port < 1) {
            return false
        }

        return true
    }

    fun checkBlankValue() : Boolean  {
        var vpn = this.servername.text
        var port = this.remoteport.text
        var pass = this.passwd.text
        var dns = this.remotedns.text

        if (vpn.isBlank()|| this.vpnname.text.isBlank()|| port.isBlank()|| pass.isBlank() || dns.isBlank()){
            return false
        }

        return true
    }

}
