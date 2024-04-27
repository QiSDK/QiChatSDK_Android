package com.example.teneasychatsdkdemo

import AppConfig
import android.util.Log
import com.google.gson.Gson
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.util.Base64
import com.teneasy.sdk.Line
import com.teneasyChat.api.common.CMessage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.random.Random

interface ReadTextDelegate {
    // 收到消息
    fun receivedMsg(msg: String)
}
class ReadTxtLib constructor(lines: Array<String>, lisener: ReadTextDelegate) {
    private val lineList = lines
    private val TAG = "LineLib"
    private var usedLine = false
    private var retryTimes = 0
    private var bodyStr = ""
    private var lisener: ReadTextDelegate? = lisener

    //  private val
    fun getLine() {

        retryTimes = 0
        usedLine = false

        Log.i(TAG, lineList.toString())
        var found = false
        var r = Random.nextInt(100000)
        var myIndex = 0
        for (hostUrl in lineList) {
            if (usedLine){
                break
            }
            val url = "$hostUrl?r=$r"
            val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS).build()
            val request: Request = Request.Builder().url(url).build()
            val call: okhttp3.Call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {

                }
                override fun onResponse(call: okhttp3.Call, response: Response) {
                    var f = false

                    if (response.isSuccessful) {
                        //未加密
                        //var body = response.body?.string()

                        //有加密
                        var contents  = response.body?.string()
                        var body = contents?.let { decryptBase64ToString(it) }
                        body?.apply {
                            lisener?.receivedMsg(body);
                        }

                    }
                }
            })
        }
    }

    fun decryptBase64ToString(base64String: String): String {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return String(decodedBytes, Charsets.UTF_8)
    }
}