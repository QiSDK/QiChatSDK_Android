package com.teneasy.sdk

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

interface LineDelegate {
    // 收到消息
    fun useTheLine(line: Line)
    fun lineError(error: Result)
}

class LineLib constructor(lines: Array<String>, linstener: LineDelegate) {
    private val lineList = lines
    private val TAG = "LineLib"
    private val listener: LineDelegate? = linstener
    private var usedLine = false
    private var retryTimes = 0
    //  private val
    fun getLine() {
        Log.i(TAG, lineList.toString())
        var found = false
        var myIndex = 0
        for (hostUrl in lineList) {
            if (usedLine){
                break
            }
            val url = "$hostUrl"
            val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS).build()
            val request: Request = Request.Builder().url(url).build()
            val call: okhttp3.Call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    myIndex += 1
                    if (myIndex == lineList.size){
                        failedAndRetry()
                    }
                }
                override fun onResponse(call: okhttp3.Call, response: Response) {
                    var f = false

                    if (response.isSuccessful) {
                        //未加密
                        //var body = response.body?.string()

                        //有加密
                        var contents  = response.body?.string()
                        var body = contents?.let { decryptBase64ToString(it) }

                        if (body != null && body.contains("VITE_API_BASE_URL")) {
                            val gson = Gson()
                            val appConfig = gson.fromJson(body, AppConfig::class.java)
                            if (appConfig != null) {
                                val lines = appConfig.lines
                                var lineStrs = mutableListOf<Line>();
                                for (l in lines){
                                    if (l.VITE_API_BASE_URL.contains("https")){
                                        lineStrs.add(l)
                                        f = true
                                    }
                                }
                                Log.i("LineLib", "txt："+ call.request().url.host)
                                step2(lineStrs, myIndex)
                            }
                        }

                        myIndex += 1

                        if (!f){
                            if (myIndex == lineList.size){
                                failedAndRetry()
                            }
                        }
                    }else{
                        myIndex += 1
                        if (myIndex == lineList.size){
                            failedAndRetry()
                        }
                    }
                }
            })
        }
    }

    fun step2(lines: MutableList<Line>, index: Int) {
        Log.i(TAG, lines.toString())
        var found = false
        var step2Index = 0
        for (line in lines) {
            if (found || usedLine){
                break
            }
            val url = "${line.VITE_API_BASE_URL}" + "/verify"
            val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS).build()
            val request: Request = Request.Builder().url(url ).build()
            val call: okhttp3.Call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    //print(call.request().url.host + " line failed")
                    step2Index += 1
                    if (step2Index == lines.size && (index + 1) == lineList.count()){
                        failedAndRetry()
                    }
                }
                override fun onResponse(call: okhttp3.Call, response: Response) {
                    var f = false
                    var body = response.body?.string()
                    if (response.isSuccessful && body != null && body!!.contains("10010")) {
                       if (!usedLine) {
                           usedLine = true
                           found = true
                           f = true
                           //listener?.useTheLine(call.request().url.host)
                           listener?.useTheLine(line)
                           Log.i("LineLib", "使用线路："+ call.request().url.host)
                       }
                    }

                    step2Index += 1

                    if (!f && step2Index == lines.size && (index + 1) == lineList.count()){
                        failedAndRetry()
                    }
                }
            })
        }
    }

    private fun failedAndRetry(){
        if (usedLine){
            return
        }
        val result = Result();
        if (retryTimes < 3){
            retryTimes += 1
            result.code = 1009
            result.message = "线路获取失败，重试" + retryTimes
            listener?.lineError(result)
            getLine()
        }else{
            result.code = 1008
            result.message = "没有可用线路"
            listener?.lineError(result)
        }
    }
    fun decryptBase64ToString(base64String: String): String {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return String(decodedBytes, Charsets.UTF_8)
    }
}