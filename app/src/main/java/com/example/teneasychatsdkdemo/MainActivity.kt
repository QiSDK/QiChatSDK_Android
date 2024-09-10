package com.example.teneasychatsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.teneasychatsdk.databinding.ActivityMainBinding
import com.example.teneasychatsdkdemo.ReadTextDelegate
import com.example.teneasychatsdkdemo.ReadTxtLib
import com.teneasy.sdk.ChatLib
import com.teneasy.sdk.Line
import com.teneasy.sdk.LineDetectDelegate
import com.teneasy.sdk.LineDetectLib
import com.teneasy.sdk.Result
import com.teneasy.sdk.TeneasySDKDelegate
import com.teneasyChat.api.common.CMessage
import com.teneasyChat.gateway.GGateway
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), TeneasySDKDelegate {

    private lateinit var chatLib: ChatLib
    private lateinit var binding: ActivityMainBinding
    private var lastMsgId: Long = 0
    private val Tag = "MainActivity"
    private var domain: String? = null
    private var isConnected = false

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
            //binding.tvContent.text = ""
            initLine()
        }

        binding.btnReadTxt.setOnClickListener {
            binding.tvContent.text = ""
            var lines = mutableListOf<String>("")
            var lineStr = binding.etInput.text.toString()
            if (lineStr.isNotEmpty()) {
                var ar = lineStr.split(",").toTypedArray()
                lines.clear()
                for (l in ar) {
                    if (l.trim().isNotEmpty()) {
                        lines.add(l.trim())
                    }
                }
            } else {
                binding.etInput.hint = "请输入线路，以逗号分开";
            }

            if (!verifyLines(lines.toTypedArray())) {
                return@setOnClickListener
            }

            var lib = ReadTxtLib(lines.toTypedArray(), object : ReadTextDelegate {
                override fun receivedMsg(msg: String) {
                    appendText(msg)
                }
            })
            lib.getLine()

//            binding.btnTestLine.visibility = View.GONE
//            binding.btnSend.visibility = View.GONE
        }

        binding.root.setOnTouchListener(
            View.OnTouchListener { v, event ->
                binding.etInput.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etInput.windowToken, 0)
                false
        })

        if (!BuildConfig.DEBUG) {
           binding.btnSend.visibility = View.GONE
        }
    }

    private fun appendText(msg: String){
        this@MainActivity.runOnUiThread {
            binding.tvContent.append(msg + "\n")
            binding.tvContent.scrollTo(0, binding.tvContent.height + 30)
        }
    }

    private fun initLine(){
        var lines = mutableListOf<String>("")
        var lineStr =  binding.etInput.text.toString()

        val shanghuNo = binding.etShangHuNo.text.toString();
        if (shanghuNo.isEmpty()){
            Toast.makeText(this, "请输入商户号", Toast.LENGTH_LONG).show()
            return
        }
//httos://csh5.hfxg.xyz,https://csapi.dev.stream
        //https://csapi.xdev.stream,https://wcsapi.qixin14.xyz,https://wcsapi.qixin14.xyz
        //httpo://csh5.hfxg.xyz,http://csh5.hfxg.xyz,https://csapi.xdev.stream,https://xx.xdev.stream
        //android:usesCleartextTraffic="true"
        val lineLib = LineDetectLib("https://61.184.8.23:7040",  object : LineDetectDelegate {
            override fun useTheLine(line: String) {
                Log.i("LineLib", "使用线路："+ line)
                appendText("Wss: " + line + "\n")
                domain = line
                if (BuildConfig.DEBUG) {
                    initChatSDK(line)
                }
            }
            override fun lineError(error: Result) {
                if (error.code == 1008){
                    runOnUiThread({
                        //binding.tvContent.text = error.msg + "\n";
                        appendText(error.msg + "\n")
                    });
                }else {
                    appendText(error.msg + "\n")
                }
            }
        }, shanghuNo.toInt())
        lineLib.getLine()
    }

    private fun initChatSDK(baseUrl: String){
        var wssUrl = "wss://" + baseUrl + "/v1/gateway/h5?"
        //token: COYBEAIYzNdEIPIBKJDZrOP3MQ.maPNGL2-vih71Eg4ghU4aTMSY6Sl0Zt8GTH6colScbTZQiTM5hak9do9qyxvhxSes-HuKbsNMLlBE72Z3J-4Bg
        //666668，364154
        chatLib = ChatLib("COYBEAUYASDyASiG2piD9zE.te46qua5ha2r-Caz03Vx2JXH5OLSRRV2GqdYcn9UslwibsxBSP98GhUKSGEI0Z84FRMkp16ZK8eS-y72QVE2AQ", "", wssUrl, 666668, "9zgd9YUc")
        chatLib.listener = this
        chatLib.makeConnect()

        binding.btnSend.setOnClickListener {
            sendMsg()
        }

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
    }

    private fun sendMsg(){
        val sayHello = "你好！今天去哪玩？"
        //val msgItem = chatLib.composeALocalMessage(sayHello)
        //addMsgItem(msgItem)
        chatLib.sendMessage(sayHello, CMessage.MessageFormat.MSG_TEXT, 1, 0)
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
            appendText(msg.content.toString() + "\n")
        }else if (msg.video != null){
            appendText(msg.video.toString() + "\n")
        }
        println(msg)
    }

    override fun msgReceipt(msg: CMessage.Message, payloadId: Long, msgId: Long, errMsg: String) {
        //println(msg)
        val suc = if (msgId == 0L) "发送失败" else "发送成功"
        println("payloadId："  + payloadId.toString()   +suc)
        runOnUiThread({
            if (msg.content.toString() != "") {
                appendText(msg.content.toString() + "\n")
            }else if (msg.video.toString() != ""){
                appendText(msg.video.toString() + "\n")
            }else if (msg.audio.toString() != ""){
                appendText(msg.audio.toString() + "\n")
            }

            if (msgId > 0){
                lastMsgId = msgId
            }
            appendText(payloadId.toString() + " msgId:" + msgId + " " + errMsg + " "+ suc +"\n")
        })
    }

    override fun msgDeleted(msg: CMessage.Message, payloadId: Long, msgId: Long, errMsg: String) {
        runOnUiThread({
            appendText("删除成功")
        })
    }

    override fun systemMsg(msg: Result) {
        //TODO("Not yet implemented")
        if (msg.code >= 1000 && msg.code < 1010) {
            isConnected = false
            //失去链接，重试连接
            //startTimer()
        }
        appendText(msg.msg + "\n")
    }

    //成功连接，并返回相关信息，例如workerId
    override fun connected(c: GGateway.SCHi) {
        val workerId = c.workerId
        //Log.i("MainAct connected", "成功连接")
        //chatLib.sendMessage("1.mp4", CMessage.MessageFormat.MSG_VIDEO)
        //Log.i(Tag, "workerId:" + workerId)
        Log.i(Tag, "token:" + c.token)
        isConnected = true
        appendText("成功连接 workerId:" + workerId)
    }

    override fun workChanged(msg: GGateway.SCWorkerChanged) {
        Log.i("MainAct connected", "已经更换客服")
        //appendText("已经更换客服\n")
        appendText(msg.workerName + "\n")
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

    override fun onResume() {
        super.onResume()
        if (!isConnected && domain != null) {
            lifecycleScope.launch {
                delay(3000)
                initChatSDK(domain!!)
            }
        }
    }

    override fun onPause() {
        super.onPause()

    }

}