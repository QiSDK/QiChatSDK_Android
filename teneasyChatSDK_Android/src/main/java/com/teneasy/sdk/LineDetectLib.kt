package com.teneasy.sdk

import android.util.Base64
import android.util.Log
import android.util.Patterns
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
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
                Log.i(TAG, line + " 无效的url")
                step2Index += 1
                listener?.lineError(result)
                continue
            }
            val url = line.trim() + "/v1/api/verify?r=$r"
            val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).build()
            val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json"), bodyStr)
            //val requestBody = bodyStr.toRequestBody("application/json".toMediaTypeOrNull())
            val request: Request = Request.Builder()
                .post(requestBody)
                .url(url).build()
            val call: Call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    //print(call.request().url.host + " line failed")
                    step2Index += 1
                    if (step2Index == lineList.size){
                        failedAndRetry()
                    }
                    Log.i(TAG, "检测线路失败："+ url + " " + e.message)
                }
                override fun onResponse(call: Call, response: Response) {
                    var f = false
                    var body = response.body()//为了更快的速度，没做反序列化
                    //{"code":0,"msg":"OK","data":{"gnsId":"wcs","tenantId":123}}
                    Log.i(TAG, "成功body："+ body)
                    if (response.isSuccessful && body != null && body.string().lowercase().contains("tenantid")) {
                       if (!usedLine) {
                           usedLine = true
                           found = true
                           f = true
                           //listener?.useTheLine(call.request().url.host)
                           var port = call.request().url().port()
                           var base = call.request().url().host()
                           if (port != 443 && port != 80){
                               base = base + ":" + port
                           }
                           listener?.useTheLine(base)
                           Log.i(TAG, "使用线路："+ call.request().url().host())
                       }else{
                           Log.i(TAG, "线路已使用")
                       }
                    }else{
                        Log.i(TAG, line +" 线路失败：没有正确的数据返回")
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