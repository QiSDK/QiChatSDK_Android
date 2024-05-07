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
import android.util.Patterns
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.random.Random

interface LineDetectDelegate {
    // 收到消息
    fun useTheLine(line: String)
    fun lineError(error: Result)
}

class LineDetectLib constructor(lines: String, linstener: LineDetectDelegate, tenantId: Int) {
    private val lineList = lines.split(",")
    private val TAG = "LineLib:"
    private val listener: LineDetectDelegate? = linstener
    private var usedLine = false
    private var retryTimes = 0
    private var tenantId = tenantId
    private var bodyStr = ""

    fun getLine() {
        val verifyBody = VerifyBody("wcs", tenantId)
        val gson = Gson()
        bodyStr = gson.toJson(verifyBody)

        Log.i(TAG, lineList.toString())
        var found = false
        var r = Random.nextInt(100000)
        var step2Index = 0
        for (line in lineList) {
            if (found || usedLine){
                break
            }
            if (!Patterns.WEB_URL.matcher(line).matches()){
                val result = Result();
                result.code = 1009
                result.msg = line + " 无效的url"
                listener?.lineError(result)
                continue
            }
            val url = line.trim() + "/v1/api/verify?r=$r"
            val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS).build()
            val requestBody = bodyStr.toRequestBody("application/json".toMediaTypeOrNull())
            val request: Request = Request.Builder()
                .post(requestBody)
                .url(url).build()
            val call: okhttp3.Call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    //print(call.request().url.host + " line failed")
                    step2Index += 1
                    if (step2Index == lineList.size){
                        failedAndRetry()
                    }
                }
                override fun onResponse(call: okhttp3.Call, response: Response) {
                    var f = false
                    var body = response.body?.string()//为了更快的速度，没做反序列化
                    //{"code":0,"msg":"OK","data":{"gnsId":"wcs","tenantId":123}}
                    Log.i(TAG, "成功body："+ body)
                    if (response.isSuccessful && body != null && body!!.lowercase().contains("tenantid")) {
                       if (!usedLine) {
                           usedLine = true
                           found = true
                           f = true
                           //listener?.useTheLine(call.request().url.host)
                           listener?.useTheLine(call.request().url.host)
                           Log.i(TAG, "使用线路："+ call.request().url.host)
                           Log.i(TAG, "使用线路wss："+ line)
                       }else{
                           Log.i(TAG, "线路已使用")
                       }
                    }
                    step2Index += 1
                    if (!f && step2Index == lineList.size){
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
            result.msg = "线路获取失败，重试" + retryTimes
            listener?.lineError(result)
            getLine()
        }else{
            result.code = 1008
            result.msg = "没有可用线路"
            retryTimes = 0
            listener?.lineError(result)
        }
    }
    fun decryptBase64ToString(base64String: String): String {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return String(decodedBytes, Charsets.UTF_8)
    }
}