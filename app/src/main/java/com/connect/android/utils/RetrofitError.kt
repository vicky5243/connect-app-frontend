package com.connect.android.utils

import com.connect.android.models.res.ErrorRes
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ResponseBody

class RetrofitError {
    companion object {
        private val moshiAdapter by lazy {
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                .adapter(ErrorRes::class.java)
        }

        fun convertErrorBody(errorBody: ResponseBody?): ErrorRes? {
            return try {
                errorBody?.source()?.let {
                    moshiAdapter.fromJson(it)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}