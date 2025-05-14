package com.teneasy.sdk

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

interface UploadListener {
    fun uploadSuccess(path: Urls, isVideo: Boolean);
    fun uploadProgress(progress: Int)
    fun uploadFailed(msg: String);
}


class UploadUtil(lis: UploadListener, baseApiUrl: String, xToken: String) {
    private var listener: UploadListener? = null
    private var TAG = "UploadUtil"
    private var baseUrlApi: String = baseApiUrl
    private var xToken: String = xToken

    companion object {
        var uploadProgress: Int = 0
    }

    init {
        listener = lis
    }

    fun uploadFile(file: File) {
        val calendar = Calendar.getInstance()
        var mSec = calendar.timeInMillis.toString()

        if (!fileTypes.contains(file.extension) && !imageTypes.contains(file.extension) && !videoTypes.contains(file.extension)){
            listener?.uploadFailed("不支持的文件类型");
            return
        }
        uploadProgress = 1
        listener?.uploadProgress(uploadProgress)

        Thread(Runnable {
            kotlin.run {
                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("myFile",  mSec + "." + file.extension,  RequestBody.create(
                        MediaType.parse("multipart/form-data"), file))
                    .addFormDataPart("type", "4")
                    .build()// + file.extension

                println("上传地址：" + this.baseUrlApi + "/v1/assets/upload-v4" + "\n" + file.path)
                val request2 = Request.Builder().url("https://" + this.baseUrlApi + "/v1/assets/upload-v4")
                    .addHeader("X-Token", xToken)
                    .post(multipartBody).build()

                val okHttpClient: OkHttpClient = OkHttpClient().newBuilder()
                    .connectTimeout(10, TimeUnit.MINUTES)
                    .writeTimeout(15, TimeUnit.MINUTES)
                    .readTimeout(15, TimeUnit.MINUTES)
                    .build()
                val call = okHttpClient.newCall(request2)
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        listener?.uploadFailed(e.message ?: "上传失败");
                        print(e.message ?: "上传失败")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body()
                        if((response.code() == 200 || response.code() == 202) && body != null) {
                            val bodyStr = body.string()
                            val gson = Gson()
                            if (response.code() == 200){//bodyStr.contains("code\":200")
                                val type: Type = object : TypeToken<ReturnData<FilePath>>() {}.getType()
                                val b: ReturnData<FilePath> = gson.fromJson(bodyStr, type)
                                //var b = gson.fromJson(bodyStr, ReturnData<FilePath>()::class.java)
                                if (b.data?.filepath == null || (b.data?.filepath ?:"").isEmpty()){
                                    listener?.uploadFailed("上传失败，path为空");
                                    return;
                                }else {
                                    val urls = Urls()
                                    urls.fileName = file.name
                                    urls.fileSize = file.length().toInt()
                                    urls.uri = b.data?.filepath?: ""
                                    listener?.uploadSuccess(
                                        urls,
                                        false
                                    )
                                    return;
                                }
                            }else if (response.code() == 202){
                                if (uploadProgress < 70){
                                    uploadProgress = 70
                                }else{
                                    uploadProgress += 10
                                }
                                listener?.uploadProgress(uploadProgress)
                                var b = gson.fromJson(bodyStr, ReturnData<String>()::class.java)
                                subscribeToSSE(
                                    "https://" + baseUrlApi + "/v1/assets/upload-v4?uploadId=" + b.data,
                                    file.extension
                                )
                            }else{
                                print("上传失败：" + response.code() + "")

                                listener?.uploadFailed("上传失败 " + bodyStr)
                            }
                        } else {
                            listener?.uploadFailed("上传失败 " + response.code())
                            print("上传失败 " + response.code())
                        }
                    }
                })

            }
        }).start()
    }

   private fun subscribeToSSE(url: String, ext: String) {
         val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // Set timeouts as needed
            .readTimeout(0, TimeUnit.SECONDS)      // Set readTimeout to 0 for long-lived connections
            .build()

       println("上传监听地址：" + url)
        val request = Request.Builder()
            .addHeader("X-Token", xToken)
            //.addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                listener?.uploadFailed("SSE 上传失败 Code:" + e.message)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    print("上传成功：" + response.code() + "")
                    val body = response.body()
                    if(response.code() == 200 && body != null) {
                        val strData = body.string()
                        val lines = strData.split("\n");
                        var event = ""
                        var data = ""

                        print("上传监听返回 " + strData);

                        if (lines.size <= 0){
                            listener?.uploadFailed("数据为空，上传失败")
                            return
                        }

                        for (line in lines) {
                            if (line.startsWith("event:", ignoreCase = true))  {
                                event = line.replace("event:", "")
                            } else if (line.startsWith("data:", ignoreCase = true)) {
                                data = line.replace("data:", "")
                                val gson = Gson()
                                val result = gson.fromJson(data, UploadPercent::class.java)

                                if (result.percentage == 100 && result.data != null) {
                                    listener?.uploadSuccess(
                                        result.data!!,
                                        true
                                    )
                                    Log.i(TAG, ("上传成功" + result.data?.uri))
                                    Log.i(TAG, (Date().toString() + "上传进度 " + result.percentage))
                                } else {
                                    listener?.uploadProgress(result.percentage)
                                    Log.i(TAG, (Date().toString() + "上传进度 " + result.percentage))
                                }
                            }
                        }
                    } else {
                        listener?.uploadFailed("SSE 上传失败 Code:" + response.code())
                        print("SSE 上传失败 Code:" + response.code())
                    }
                } else {
                    listener?.uploadFailed("Failed to connect to SSE stream")
                }
            }
        })
    }
}

class UploadPercent {
    var percentage: Int = 0
    var data: Urls? = null
}

class Urls {
    var uri: String = ""
    var hlsUri: String = ""
    var thumbnailUri = ""
    var fileName: String = ""
    var fileSize: Int = 0
}

class FilePath (
    var filepath : String? = null
)

val imageTypes = arrayOf(
    "tif",
    "tiff",
    "bmp",
    "jpg",
    "jpeg",
    "jfif",
    "png",
    "gif",
    "webp",
    "heic",
    "ico",
    "svg"
)  // 支持的图片格式
val fileTypes = arrayOf("docx", "doc", "pdf", "xls", "xlsx", "csv")  // 支持的文档格式
val videoTypes = arrayOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm")  // 支持的视频格式
/*
```
图片: jpg, jpeg, png, webp, gif, bmp, jfif
视频: mp4, avi, mkv, mov, wmv, flv, webm
文档: docx, doc, pdf, xls, xlsx, csv
```
 */