package com.teneasy.sdk

import android.provider.SyncStateContract.Constants
import android.telecom.Call
import android.util.Log
import com.teneasyChat.api.common.CMessage
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Random

interface LineDelegate {

    // 收到消息
    fun useTheLine(line: Object)
    fun lineError(error: String)
}
class LineLib {
    private val lineList = ArrayList<String>()
    val TAG = "LineLib"
    private fun getLine() {
        val anInt: Int = Random().nextInt(10000)

        Log.i(TAG, lineList.toString())
        for (hostUrl in lineList) {
            val url = "$hostUrl?v=$anInt"
            val client: OkHttpClient = OkHttpClient.Builder().build()
            val request: Request = Request.Builder().url(url).build()
            val call: okhttp3.Call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                   // TODO("Not yet implemented")
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                   // TODO("Not yet implemented")
                    if (response.isSuccessful) {
                        val str: String = response.message
                        if (!str.contains("getJSONP._JSONP")) {
                            return
                        }
                    }
                   // break ;
                }
            })
        }
    }
}