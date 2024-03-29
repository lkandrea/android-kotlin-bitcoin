package com.lkand.bitcoin_tracker.feature.dashboard.model

import org.json.JSONObject

class DashboardResponseModel(json: String): JSONObject(json) {
    val side: String? = this.optString("side")
    val price: Double? = this.optDouble("price")
}