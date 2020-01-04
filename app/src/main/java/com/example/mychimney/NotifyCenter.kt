package com.example.mychimney

class NotifyCenter : UIUpdateInterface, UpdateConnectStatus {

    var updater = ArrayList<UIUpdateInterface>()
    var satusUpdater = ArrayList<UpdateConnectStatus>()


    override fun register(v: UIUpdateInterface) {
        updater.add(v)

    }

    override fun unregister(v: UIUpdateInterface) {
        updater.remove(v)

    }

    fun registerSatusListener(v: UpdateConnectStatus) {
        satusUpdater.add(v)

    }

    fun unRegisterSatusListener(v: UpdateConnectStatus) {
        satusUpdater.remove(v)

    }

    override fun nofityUI() {
        for (v in updater) {
            v.nofityUI()
        }
    }

    companion object {

        val instance by lazy { NotifyCenter() }
    }

    override fun updateConnectStatus(status: String) {
        for (v in satusUpdater) {
            v.updateConnectStatus(status)
        }
    }

    override fun updateFlow(p0: Long, p1: Long) {
        for (v in satusUpdater) {
            v.updateFlow(p0, p1)
        }
    }

}