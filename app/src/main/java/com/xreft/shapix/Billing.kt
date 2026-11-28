package com.xreft.shapix

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ir.myket.billingclient.IabHelper

fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

object Billing {
    private const val SKU = "remove_ads"
    private const val PREFS = "shapix_prefs"
    private const val KEY = "ads_removed"

    var adsRemoved by mutableStateOf(false)
        private set

    private var helper: IabHelper? = null
    private var ready = false
    private val main = Handler(Looper.getMainLooper())

    fun init(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        adsRemoved = prefs.getBoolean(KEY, false)
        if (BuildConfig.IAB_PUBLIC_KEY.isBlank()) return
        try {
            helper = IabHelper(activity, BuildConfig.IAB_PUBLIC_KEY)
            helper?.startSetup { result ->
                if (!result.isSuccess) return@startSetup
                ready = true
                restore(activity)
            }
        } catch (e: Throwable) {
        }
    }

    private fun restore(activity: Activity) {
        try {
            helper?.queryInventoryAsync(true, listOf(SKU)) { result, inventory ->
                if (result.isFailure || inventory == null) return@queryInventoryAsync
                val owned = inventory.getPurchase(SKU) != null
                main.post { setRemoved(activity, owned) }
            }
        } catch (e: Throwable) {
        }
    }

    private fun setRemoved(activity: Activity, value: Boolean) {
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY, value).apply()
        adsRemoved = value
        if (!value) Ads.init(activity)
    }

    fun buy(activity: Activity, onResult: (String) -> Unit) {
        val h = helper
        if (h == null || !ready) {
            onResult("msgBillingUnavailable")
            return
        }
        try {
            h.launchPurchaseFlow(activity, SKU, { result, purchase ->
                main.post {
                    if (result.isFailure || purchase == null) {
                        onResult("msgPurchaseFail")
                        restore(activity)
                    } else if (purchase.sku == SKU) {
                        setRemoved(activity, true)
                        onResult("msgPurchaseOk")
                    }
                }
            }, "shapix-remove-ads")
        } catch (e: Throwable) {
            onResult("msgPurchaseFail")
        }
    }

    fun dispose() {
        try { helper?.dispose() } catch (e: Throwable) { }
        helper = null
        ready = false
    }
}
