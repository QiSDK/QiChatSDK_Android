package com.example.teneasychatsdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.teneasychatsdk.databinding.ActivityMainBinding
import com.teneasy.sdk.ChatLib
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
        chatLib = ChatLib("CCcQARgOICIowqaSjeIw.9rO3unQwFrUUa-vJ6HvUQAbiAZN7XWBbaE_Oyd48C0Ae4xhzWWSriIGZZdVSvOajS1h_RFlQHZiFzadgBBuwDQ", "wss://csapi.xdev.stream/v1/gateway/h5?token=", 1125324, "9zgd9YUc")
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
        chatLib.sendMessage(sayHello, CMessage.MessageFormat.MSG_TEXT)
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
        Log.i("MainAct systemMsg", msg.message)
        binding.tvContent.append(msg.message + "\n")
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

    override fun onDestroy() {
        super.onDestroy()
        chatLib.disConnect()
    }

}