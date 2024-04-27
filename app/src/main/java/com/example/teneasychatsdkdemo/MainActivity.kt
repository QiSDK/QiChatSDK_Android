package com.example.teneasychatsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.teneasychatsdk.databinding.ActivityMainBinding
import com.example.teneasychatsdkdemo.ReadTextDelegate
import com.example.teneasychatsdkdemo.ReadTxtLib
import com.teneasy.sdk.ChatLib
import com.teneasy.sdk.Line
import com.teneasy.sdk.LineDelegate
import com.teneasy.sdk.LineLib
import com.teneasy.sdk.Result
import com.teneasy.sdk.TeneasySDKDelegate
import com.teneasyChat.api.common.CMessage
import com.teneasyChat.gateway.GGateway

class MainActivity : AppCompatActivity(), TeneasySDKDelegate {

    private lateinit var chatLib: ChatLib
    private lateinit var binding: ActivityMainBinding
    private var lastMsgId: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //https://qlqiniu.quyou.tech/gw3config.txt
        //https://ydqlacc.weletter05.com/gw3config.txt
        //val lines = arrayOf("https://dtest/gw3config.txt", "https://qlqiniu.quyou.tech/gw3config.txt","https://ydqlacc.weletter05.com/gw3config.txt",  "https://ydqlacc.weletter05.com/gw3config.txt", "https://ddtest/gw3config.txt", "https://ddtest.com/gw3config.txt", "https://ddtest.x/gw3config.txt", "https://ddtest.cx/verify/d5", "https://ddtest.net/gw3config.txt") ;
        //val lines = arrayOf("https://ydqlacc.weletter05.com/gw3config.txt")

        //生产的线路
        //val lines = arrayOf("https://qlqiniu.quyou.tech/gw1config.txt","https://ydqlacc.weletter05.com/gw1config.txt")

        //测试的线路
        //val lines = arrayOf("https://qlqiniu.quyou.tech/gw3config.txt","https://ydqlacc.weletter05.com/gw3config.txt")

        binding.btnTestLine.setOnClickListener {
            initLine()
        }

        binding.btnReadTxt.setOnClickListener {
            var lines = mutableListOf<String>("")
            var lineStr =  binding.etInput.text.toString()
            if (lineStr.isNotEmpty()){
                var  ar = lineStr.split(",").toTypedArray()
                lines.clear()
                for (l in ar){
                    if (l.trim().isNotEmpty()) {
                        lines.add(l.trim())
                    }
                }
            }else{
                binding.etInput.hint = "请输入线路，以逗号分开";
            }

            if (!verifyLines(lines.toTypedArray())) {

                return@setOnClickListener
            }

           var lib = ReadTxtLib(lines.toTypedArray(), object : ReadTextDelegate {
               override fun receivedMsg(msg: String) {
                   this@MainActivity.runOnUiThread {
                       binding.tvContent.append(msg)
                   }
               }
           })
            lib.getLine()

            binding.btnTestLine.visibility = View.GONE
            binding.btnSend.visibility = View.GONE
        }
    }

    private fun initLine(){
        var lines = mutableListOf<String>("")
        var lineStr =  binding.etInput.text.toString()
        if (lineStr.isNotEmpty()){
            var  ar = lineStr.split(",").toTypedArray()
            lines.clear()
            for (l in ar){
                if (l.trim().isNotEmpty()) {
                    lines.add(l.trim())
                }
            }
        }else{
            binding.etInput.hint = "请输入线路，以逗号分开";
        }

        if (!verifyLines(lines.toTypedArray())) {

            return
        }

        val lineLib = LineLib(lines.toTypedArray(),  object : LineDelegate {
            override fun useTheLine(line: Line) {
                Log.i("LineLib", "使用线路："+ line)
                this@MainActivity.runOnUiThread{
                    binding.tvContent.append("Api: " + line.VITE_API_BASE_URL + "\n")
                    binding.tvContent.append("Img: " + line.VITE_IMG_URL + "\n")
                    binding.tvContent.append("Wss: " + line.VITE_WSS_HOST + "\n")
                }
                initChatSDK(line.VITE_WSS_HOST)
            }
            override fun lineError(error: Result) {
                this@MainActivity.runOnUiThread{
                    binding.tvContent.append(error.msg + "\n")
                    Toast.makeText(this@MainActivity, error.msg, Toast.LENGTH_LONG).show()
                }
            }
        }, 123)
        lineLib.getLine()
    }

    private fun initChatSDK(baseUrl: String){
        var wssUrl = "wss://" + baseUrl + "/v1/gateway/h5?token="
        /*
        老token，一直有效，很好
        CCcQARgOICIowqaSjeIw.9rO3unQwFrUUa-vJ6HvUQAbiAZN7XWBbaE_Oyd48C0Ae4xhzWWSriIGZZdVSvOajS1h_RFlQHZiFzadgBBuwDQ

        CH0QARiX9w4gogEo9MS-08wx.R07hSs5oXQxe9s0bV0WsaislYcvHDNYvUYT-2JNEo4wcBC1LNEHmHAFSjCoY8g60oW31zZiIs1kZhejQEaEhBQ

       CH0QARib9w4gogEo8_nL1cwx.gXxoS2IK7cv4JWQb8LRmGI-cSEFHwfyBmoyErwSw0h1BXdkotxH4OgoiHvi6B6CON8LX7ei5AKwn3v1epXB9Cg
         */
        /*
        1125324  1125397 1125417
        //1125324, "9zgd9YUc"
         */
        chatLib = ChatLib("CCcQARgOICIowqaSjeIw.9rO3unQwFrUUa-vJ6HvUQAbiAZN7XWBbaE_Oyd48C0Ae4xhzWWSriIGZZdVSvOajS1h_RFlQHZiFzadgBBuwDQ", wssUrl, 1125324, "9zgd9YUc")
        chatLib.listener = this
        chatLib.makeConnect()

        binding.btnSend.setOnClickListener {
            sendMsg()
        }
    }

    private fun sendMsg(){
        val sayHello = "你好！今天去哪玩？"
        //val msgItem = chatLib.composeALocalMessage(sayHello)
        //addMsgItem(msgItem)
        chatLib.sendMessage(sayHello, CMessage.MessageFormat.MSG_TEXT, 100, 12)
        val payloadId = chatLib.payloadId
        val sendingMsg = chatLib.sendingMessage

        //chatLib.sendMessage("/3/public/1/1695821236_29310.jpg", CMessage.MessageFormat.MSG_IMG)

        //chatLib.sendMessage("1.mp3", CMessage.MessageFormat.MSG_VOICE)

        //chatLib.sendMessage("1.mp4", CMessage.MessageFormat.MSG_VIDEO)

        //chatLib.sendMessage("2.mp4", CMessage.MessageFormat.MSG_VIDEO, 564321055359893503)
        //chatLib.deleteMessage(lastMsgId)
        //chatLib.resendMSg(msg,10000);
    }

    override fun receivedMsg(msg: CMessage.Message) {
        if (msg.content != null) {
            binding.tvContent.append(msg.content.toString() + "\n")
        }else if (msg.video != null){
            binding.tvContent.append(msg.video.toString() + "\n")
        }
        println(msg)
    }

    override fun msgReceipt(msg: CMessage.Message, payloadId: Long, msgId: Long, errMsg: String) {
        //println(msg)
        val suc = if (msgId == 0L) "发送失败" else "发送成功"
        println("payloadId："  + payloadId.toString()   +suc)
        runOnUiThread({
            if (msg.content.toString() != "") {
                binding.tvContent.append(msg.content.toString() )
            }else if (msg.video.toString() != ""){
                binding.tvContent.append(msg.video.toString() )
            }else if (msg.audio.toString() != ""){
                binding.tvContent.append(msg.audio.toString() )
            }

            if (msgId > 0){
                lastMsgId = msgId
            }
            binding.tvContent.append(payloadId.toString() + " msgId:" + msgId + " " + errMsg + " "+ suc +"\n")
        })
    }

    override fun systemMsg(msg: Result) {
        //TODO("Not yet implemented")
        Log.i("MainAct systemMsg", msg.msg)
        binding.tvContent.append(msg.msg + "\n")
    }

    //成功连接，并返回相关信息，例如workerId
    override fun connected(c: GGateway.SCHi) {
        val workerId = c.workerId
        Log.i("MainAct connected", "成功连接")
        //chatLib.sendMessage("1.mp4", CMessage.MessageFormat.MSG_VIDEO)
        runOnUiThread({
            binding.tvContent.append("成功连接\n")
        })
    }

    override fun workChanged(msg: GGateway.SCWorkerChanged) {
        Log.i("MainAct connected", "已经更换客服")
    }

    fun verifyLines(lines : Array<String>) : Boolean{
        var verify = true
        for (line in lines){
            if (!line.startsWith("https:")){
                Toast.makeText(this, "线路格式错误", Toast.LENGTH_LONG).show()
                verify = false
            }
        }
        return verify
    }

    override fun onDestroy() {
        super.onDestroy()
        if (chatLib != null) {
            chatLib.disConnect()
        }
    }

}