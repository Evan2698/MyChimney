package com.example.mychimney

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable

class MyServiceStatusReceiver() : BroadcastReceiver(), Parcelable {
    constructor(parcel: Parcel) : this() {


    }

    override fun onReceive(context: Context?, intent: Intent?) {

       var status = intent!!.getStringExtra("status")
        if (status != null){
            NotifyCenter.instance.updateConnectStatus(status)
        }

        var mds = intent.getStringExtra("flow")
        if (mds != null) {
            var p0 = intent.getLongExtra("up", 0)
            var p1 = intent.getLongExtra("up", 0)
            NotifyCenter.instance.updateFlow(p0, p1)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyServiceStatusReceiver> {
        override fun createFromParcel(parcel: Parcel): MyServiceStatusReceiver {
            return MyServiceStatusReceiver(parcel)
        }

        override fun newArray(size: Int): Array<MyServiceStatusReceiver?> {
            return arrayOfNulls(size)
        }
    }
}