package com.lkand.bitcoin_tracker.feature.dashboard.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.CountDownTimer
import android.util.Log
import com.lkand.bitcoin_tracker.feature.dashboard.model.DashboardModel
import com.lkand.bitcoin_tracker.util.NetworkUtil
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class DashboardViewModel: ViewModel() {

    private val responseModel = MutableLiveData<DashboardModel>()
    private val viewStatus = MutableLiveData<Int>()
    private val loginCode = MutableLiveData<String>()

    fun transform() {
        val socket = NetworkUtil.create("wss://ws-feed.pro.coinbase.com", bitcoinRateListener())

        val timer = object: CountDownTimer(5000,1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("DebugUtil, tick", millisUntilFinished.toString())
            }
            override fun onFinish() {
                socket.close(1000, "5s up")
            }
        }
        timer.start()
    }

    private fun bitcoinRateListener(): WebSocketListener {
        return object: WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)

                webSocket.send("""
                    {
                        "type": "subscribe",
                        "product_ids": [
                            "BTC-USD"
                        ],
                        "channels": [
                            "matches"
                        ]
                    }
                """.trimIndent())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)

                Log.d("DebugUtil, string", text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)

                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)

                t.printStackTrace()
            }

        }
    }

    fun getLoginCode(): String {
        return if(this.loginCode.value != null) this.loginCode.value!! else "Loading.."
    }
}