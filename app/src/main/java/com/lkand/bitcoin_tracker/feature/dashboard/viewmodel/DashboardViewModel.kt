package com.lkand.bitcoin_tracker.feature.dashboard.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.CountDownTimer
import android.util.Log
import com.lkand.bitcoin_tracker.feature.dashboard.model.DashboardResponseModel
import com.lkand.bitcoin_tracker.util.NetworkUtil
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class DashboardViewModel: ViewModel() {

    private val responseModel = MutableLiveData<DashboardResponseModel>()
    private val responseBuyModel = MutableLiveData<DashboardResponseModel>()
    private val responseSellModel = MutableLiveData<DashboardResponseModel>()
    private val socketIsClosed = MutableLiveData<Boolean>()
    private val socketCountDown = MutableLiveData<Int>()

    fun transform(): MutableLiveData<Int> {
        val socket = NetworkUtil.create("wss://ws-feed.pro.coinbase.com", bitcoinRateListener())

        val timer = object: CountDownTimer(30000,100) {
            override fun onTick(millisUntilFinished: Long) {
                this@DashboardViewModel.socketCountDown.value = (millisUntilFinished / 100).toInt()
            }
            override fun onFinish() {
                socket.close(1000, "10s up")
                this@DashboardViewModel.socketIsClosed.value = true
            }
        }
        timer.start()

        return this.socketCountDown
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

                val responseModel = DashboardResponseModel(text)
                this@DashboardViewModel.responseModel.postValue(responseModel)
                Log.d("DebugUtil, ", text)

                if (responseModel.side == "sell") {
                    this@DashboardViewModel.responseSellModel.postValue(responseModel)
                }
                else if (responseModel.side == "buy") {
                    this@DashboardViewModel.responseBuyModel.postValue(responseModel)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)

                webSocket.close(1000, null)
                Log.d("DebugUtil", "Closed")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)

                t.printStackTrace()
            }

        }
    }

    fun getResponseBuyModel(): DashboardResponseModel {
        return if(this.responseBuyModel.value != null) this.responseBuyModel.value!! else DashboardResponseModel("""
            {
                type: "Loading ...",
                price: 0,
                side: "Loading ..."
            }
        """.trimIndent())
    }

    fun getResponseSellModel(): DashboardResponseModel {
        return if(this.responseSellModel.value != null) this.responseSellModel.value!! else DashboardResponseModel("""
            {
                type: "Loading ...",
                price: 0,
                side: "Loading ..."
            }
        """.trimIndent())
    }

    fun getSocketCountDown(): String {
        if (this.socketCountDown.value == null) {
            return "00:00"
        }

        val countDown = this.socketCountDown.value!!

        val seconds = (countDown.div(10)).toString()
        val milliseconds = (countDown.rem(10)).toString() + "0"

        return "$seconds:$milliseconds"
    }
}