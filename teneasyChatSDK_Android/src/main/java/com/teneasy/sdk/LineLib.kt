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
    fun useTheLine(line: String)
    fun lineError(error: String)
}
class LineLib constructor(lines: Array<String>, linstener: LineDelegate) {
    private val lineList = lines
    private val TAG = "LineLib"
    private val listener: LineDelegate? = linstener
    //  private val
    fun getLine() {
        val anInt: Int = Random().nextInt(10000)
        Log.i(TAG, lineList.toString())
        var found = false
        for (hostUrl in lineList) {
            if (found){
                break
            }
            val url = "$hostUrl?v=$anInt"
            val client: OkHttpClient = OkHttpClient.Builder().build()
            val request: Request = Request.Builder().url(url).build()
            val call: okhttp3.Call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    print(call.request().url.host + " failed")
                }
                override fun onResponse(call: okhttp3.Call, response: Response) {
                    if (response.isSuccessful) {
                        val str: String = response.message
//                        if (str.contains("getJSONP._JSONP")) {
//                            return
//                        }
                        if (str.length > 1) {
                            //call.request().url.host
                            print(call.request().url.host + " 成功")
                            //listener?.useTheLine(call.request().url.host)
                            listener?.useTheLine("csapi.xdev.stream")
                            found = true
                        }
                    }
                    // break ;
                }
            })
        }
    }
}