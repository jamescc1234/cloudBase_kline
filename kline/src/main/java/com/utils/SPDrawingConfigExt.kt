package com.utils

import com.binance.hydrogen.storage.shared.StorageManager

/**
 * @author James Chen
 * @date 24/4/2023
 */

const val CACHE_DEFAULT_CONFIG_KEY = "CACHE_DEFAULT_CONFIG_KEY"

fun StorageManager.getDrawingDefaultConfig(): String {
    return mStorageHelper.getString(CACHE_DEFAULT_CONFIG_KEY, "")
}

fun StorageManager.setDrawingDefaultConfig(config: String) {
    mStorageHelper.putString(CACHE_DEFAULT_CONFIG_KEY, config)
}

private const val CACHE_KEY = "KLINE_DRAWING_CACHE"

fun StorageManager.getDrawingCache(symbol: String, flagTime: String): String {
    return mStorageHelper.getString("$CACHE_KEY$symbol$flagTime", "")
}

fun StorageManager.setDrawingCache(symbol: String, flagTime: String, cache: String) {
    mStorageHelper.putString("$CACHE_KEY$symbol$flagTime", cache)
}