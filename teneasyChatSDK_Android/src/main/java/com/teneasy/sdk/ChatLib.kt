package com.teneasy.sdk

import android.util.Log
import com.google.protobuf.Timestamp
import com.teneasyChat.api.common.CEntrance.ClientType
import com.teneasyChat.api.common.CMessage
import com.teneasyChat.api.common.CMessage.Message
import com.teneasyChat.api.common.CMessage.MessageFormat
import com.teneasyChat.gateway.GAction
import com.teneasyChat.gateway.GGateway
import com.teneasyChat.gateway.GPayload
//import io.crossbar.autobahn.websocket.WebSocketConnection
//import io.crossbar.autobahn.websocket.WebSocketConnectionHandler
//import io.crossbar.autobahn.websocket.types.ConnectionResponse
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.*


interface TeneasySDKDelegate {
    // 收到消息
    fun receivedMsg(msg: CMessage.Message)

   /**
   消息回执
    @msg 已发送的消息
    @payloadId
    @msgId, 如果是0，表示服务器没有生成消息id, 发送失败
    */
    fun msgReceipt(msg: CMessage.Message, payloadId: Long, msgId: Long, errMsg: String = "") // 使用Long代替UInt64

    // 系统消息，用于显示Tip
    fun systemMsg(msg: Result)

    // 连接状态
    fun connected(c: GGateway.SCHi)

    // 客服更换回调
    fun workChanged(msg: GGateway.SCWorkerChanged)
}

/**
 * 通讯核心类，提供了发送消息、解析消息等功能
 */
class ChatLib constructor(cert: String, token:String, baseUrl:String = "", userId: Int, sign:String,  chatID: Long = 0){
    private val TAG = "ChatLib"
    // 通讯地址
   private var baseUrl = ""
   private fun isConnection() : Boolean {
        if (socket == null) return false
        try {
            return socket.isOpen
        }catch (e: Exception) {
            return false;
        }
    }

    // 当前发送的消息实体，便于上层调用的逻辑处理
    var sendingMessage: CMessage.Message? = null
    private var chatId: Long = 0L //2692944494608客服下线了
    private var token: String? = ""//qi xin
    private var cert: String? = ""
    private var userId: Int = 0
    private var mySign: String? = ""//qi xin
    private lateinit var socket: WebSocketClient
    var listener: TeneasySDKDelegate? = null
    var payloadId = 0L
    private val msgList: MutableMap<Long, CMessage.Message> = mutableMapOf()
    var replyMsgId: Long = 0L
    var consultId: Long = 0L

    init {
        this.chatId = chatID
        if (token.length > 10) {
            this.token = token
        }
        if (baseUrl.length > 10) {
            this.baseUrl = baseUrl
        }

        this.userId = userId;
        this.mySign = sign

        if (cert.length > 10) {
            this.cert = cert
        }
    }

    /**
     * 启动socket连接
      */
    fun makeConnect(){
//        val obj = JSONObject()
//        obj.put("event", "addChannel")
//        obj.put("channel", "ok_btccny_ticker")

        /*
        dt==当前日期 Date.now()
rd === 随即数 Math.floor(Math.random() * 1000000)
         */
        var rd = Random().nextInt(1000000) + 1000000
        var dt = Date().time
        val url = baseUrl + "cert=" + this.cert + "&token=" + token + "&userid=" + this.userId + "&ty=" + ClientType.CLIENT_TYPE_USER_APP.number + "&dt=" + dt + "&sign=" + mySign + "&rd=" + rd

       var result = Result();
        socket =
            object : WebSocketClient(URI(url), Draft_6455()) {
                override fun onMessage(message: String) {
                }

                override fun onMessage(bytes: ByteBuffer?) {
                    super.onMessage(bytes)
                    if (bytes != null)
                        receiveMsg(bytes.array())
                }
                override fun onOpen(handshake: ServerHandshake?) {
                    Log.i(TAG, "opened connection")
                    result.msg = "已连接上服务器"
                    listener?.systemMsg(result)
                }
                override fun onClose(code: Int, reason: String, remote: Boolean) {
                    result.code = code
                    if (code == 0){
                        result.code = 1006
                    }
                    result.msg = "已断开通信"
                    listener?.systemMsg(result)
                }
                override fun onError(ex: Exception) {
                    result.code = 1004
                    result.msg = ex.message ?:"未知错误"
                    //ex.printStackTrace()
                }
            }
        socket.connect()
    }

    /**
     * 发送文本类型的消息
     * @param msg   消息内容或图片url,音频url,视频url...
     */
     fun sendMessage(msg: String, type: MessageFormat, consultId: Long, replyMsgId: Long = 0) {
        this.replyMsgId = replyMsgId;
         this.consultId = consultId;
      if (type == MessageFormat.MSG_TEXT){
          sendTextMessage(msg)
      }else if (type == MessageFormat.MSG_IMG){
          sendImageMessage(msg)
      }else if (type == MessageFormat.MSG_VIDEO){
          sendVideoMessage(msg)
      }else if (type == MessageFormat.MSG_VOICE){
          sendAudioMessage(msg)
      }else if (type == MessageFormat.MSG_FILE){
          sendFileMessage(msg)
      }else {
          sendTextMessage(msg)
      }
        sendingMessage?.let {
            doSendMsg(it)
        }
    }

    fun deleteMessage(MsgId: Long){
        val msg = CMessage.Message.newBuilder()
        msg.msgId = MsgId
        msg.chatId = chatId
        msg.setConsultId(this.consultId)
        msg.setMsgOp(CMessage.MessageOperate.MSG_OP_DELETE)
        sendingMessage = msg.build()
        sendingMessage?.let {
            doSendMsg(it)
        }
    }

    /**
     * 发送文本类型的消息
     * @param msg   消息内容
     */
   private fun sendTextMessage(msg: String) {
        //第一层
        val content = CMessage.MessageContent.newBuilder()
        content.data = msg

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setConsultId(this.consultId)
        msg.setContent(content)
        msg.sender = 0
        msg.replyMsgId = this.replyMsgId
        msg.chatId = chatId
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }

    /**
     * 发送图片类型的消息
     * @param url   图片地址
     */
   private fun sendImageMessage(url: String) {
        //第一层
        val content = CMessage.MessageImage.newBuilder()
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setConsultId(this.consultId)
        msg.setImage(content)
        msg.replyMsgId = this.replyMsgId
        msg.sender = 0
        msg.chatId = chatId
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }

    /**
     * 发送视频类型的消息
     * @param url   视频地址
     */
    private fun sendVideoMessage(url: String) {
        //第一层
        val content = CMessage.MessageVideo.newBuilder()
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setConsultId(this.consultId)
        msg.setVideo(content)
        msg.sender = 0
        msg.replyMsgId = this.replyMsgId
        msg.chatId = chatId
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }

    /**
     * 发送音频类型的消息
     * @param url   音频地址
     */
    private fun sendAudioMessage(url: String) {
        //第一层
        val content = CMessage.MessageAudio.newBuilder()
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setConsultId(this.consultId)
        msg.setAudio(content)
        msg.sender = 0
        msg.replyMsgId = this.replyMsgId
        msg.chatId = chatId
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }

    /**
     * 发送文件类型的消息
     * @param url   文件地址
     */
    private fun sendFileMessage(url: String) {
        //第一层
        val content = CMessage.MessageFile.newBuilder()
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setConsultId(this.consultId)
        msg.setFile(content)
        msg.sender = 0
        msg.replyMsgId = this.replyMsgId
        msg.chatId = chatId
        msg.worker = 0
        msg.msgTime = TimeUtil.msgTime()

        sendingMessage = msg.build()
    }

    /**
     * 重发消息
     * @param cMsg: Message
     * @param payloadId: Long
     */
    fun resendMSg(cMsg: Message, payloadId: Long){
        doSendMsg(cMsg, GAction.Action.ActionCSSendMsg, payloadId)
    }

    /**
     * 发送文本消息
     * @param textMsg MessageItem
     */
    private fun doSendMsg(cMsg: CMessage.Message, act: GAction.Action = GAction.Action.ActionCSSendMsg, payload_Id: Long = 0) {
        if(!isConnection()) {
            failedToSend()
            makeConnect()
            return
        }
        // 第三层
        val cSendMsg = GGateway.CSSendMessage.newBuilder()
        cSendMsg.msg = cMsg

        val cSendMsgData = cSendMsg.build().toByteString()

        //第四层
        val payload = GPayload.Payload.newBuilder()
        payload.data = cSendMsgData
        payload.act = act

        //payload_id != 0的时候，可能是重发，重发不需要+1
        if (sendingMessage?.msgOp == CMessage.MessageOperate.MSG_OP_POST && payload_Id == 0L) {
            payloadId += 1
            msgList[payloadId] = cMsg
        }

        if (payload_Id != 0L){
            payload.id = payload_Id;
        }else {
            payload.id = payloadId
        }
        Log.i(TAG, "send payloadId: ${payloadId}")
        socket.send(payload.build().toByteArray())
    }

    /**
     *  心跳，一般建议每隔60秒调用
     */
    fun sendHeartBeat(){
        val buffer = ByteArray(1)
        buffer[0] = 0
        Log.i(TAG, "sending heart beat")
        socket.send(buffer)
    }

    /**
     * socket消息解析，内部方法
     * @param data
     */
    private fun receiveMsg(data: ByteArray) {
        if(data.size == 1) {
            Log.i(TAG, "在别处登录了/或无效的Token")
            var result = Result();
            result.code = 1003
            result.msg = "在别处登录了/或无效的Token"
            listener?.systemMsg(result)
        }
        else {
            val payLoad = GPayload.Payload.parseFrom(data)
            val msgData = payLoad.data
            //收到消息
            if(payLoad.act == GAction.Action.ActionSCRecvMsg) {
                val recvMsg = GGateway.SCRecvMessage.parseFrom(msgData)
                //收到对方撤回消息
                if (recvMsg.msg.msgOp == CMessage.MessageOperate.MSG_OP_DELETE){
                    listener?.msgReceipt(recvMsg.msg, payLoad.id, -1)
                }else{
                    recvMsg.msg.let {
                        listener?.receivedMsg(it)
                    }
                }
            } else if(payLoad.act == GAction.Action.ActionSCHi) {
                val msg = GGateway.SCHi.parseFrom(msgData)
                token = msg.token
                payloadId = payLoad.id
                print("初始payloadId:" + payloadId + "\n")
                listener?.connected(msg)
            } else if(payLoad.act == GAction.Action.ActionForward) {
                val msg = GGateway.CSForward.parseFrom(msgData)
                Log.i(TAG, "forward: ${msg.data}")
            }  else if(payLoad.act == GAction.Action.ActionSCDeleteMsgACK) {
                //这部分实际没有用上
                val scMsg = GGateway.SCSendMessage.parseFrom(msgData)
                val msg = CMessage.Message.newBuilder()
                msg.msgId = scMsg.msgId;
                msg.msgOp = CMessage.MessageOperate.MSG_OP_DELETE;
                Log.i(TAG, "删除回执收到A：消息ID: ${msg.msgId}")
                var cMsg = msgList[payLoad.id]
                if (cMsg != null) {
                    Log.i(TAG, "删除成功")
                    listener?.msgReceipt(msg.build(), payLoad.id, -1)
                }
            }  else if(payLoad.act == GAction.Action.ActionSCDeleteMsg) {
                val scMsg = GGateway.SCRecvMessage.parseFrom(msgData)
                val msg = CMessage.Message.newBuilder()
                msg.msgId = scMsg.msg.msgId;
                msg.msgOp == CMessage.MessageOperate.MSG_OP_DELETE
                listener?.msgReceipt(msg.build(), payLoad.id, -1)
                Log.i(TAG, "对方删除了消息： payload ID${payLoad.id}")
            } else if(payLoad.act == GAction.Action.ActionSCSendMsgACK) {//消息回执
                val scMsg = GGateway.SCSendMessage.parseFrom(msgData)
                chatId = scMsg.chatId
                Log.i(TAG, "收到消息回执B msgId: ${scMsg.msgId}")

                var cMsg = msgList[payLoad.id]
                if (cMsg != null){
                    if (scMsg.errMsg != null && !scMsg.errMsg.isNullOrEmpty()){
                        listener?.msgReceipt(cMsg, payLoad.id, -2, scMsg.errMsg)
                    }
                    else if (sendingMessage?.msgOp == CMessage.MessageOperate.MSG_OP_DELETE){
                        listener?.msgReceipt(cMsg, payLoad.id, -1)
                        Log.i(TAG, "删除成功")
                    }else{
                        listener?.msgReceipt(cMsg, payLoad.id, scMsg.msgId)
                    }
                }
                Log.i(TAG, "消息ID: ${scMsg.msgId}")
            } else if(payLoad.act == GAction.Action.ActionSCWorkerChanged){
                val scMsg = GGateway.SCWorkerChanged.parseFrom(msgData)
                consultId = scMsg.consultId
                scMsg.apply {
                    listener?.workChanged(scMsg);
                }
            }
            else
                Log.i(TAG, "received data: $data")
        }
    }

    /**
     * 通过指定的文本内容，创建消息实体。一般用于UI层对用户显示的自定义消息（该方法并未调用socket发送消息）。
     * 如需发送至后端，需获取返回的消息实体，再调用发送方法
     * @param textMsg
     * @param isLeft    指定消息显示方式
     */
    //撰写一条信息
    fun composeALocalMessage(textMsg: String) : CMessage.Message{
        //第一层
        var cMsg = CMessage.Message.newBuilder()
        //第二层
        var cMContent = CMessage.MessageContent.newBuilder()

        var d = Timestamp.newBuilder()
        val cal = Calendar.getInstance()
        cal.time = Date()
        val millis = cal.timeInMillis
        d.seconds = (millis * 0.001).toLong()

        //d.t = msgDate.time
        cMsg.msgTime = d.build()
        cMContent.data = textMsg
        cMsg.setContent(cMContent)

        return cMsg.build()
    }


    private fun failedToSend(){
//        sendingMessageItem?.let {
//            var eventBus = MessageEventBus<MessageItem>()
//            it.sendStatus = MessageSendState.发送失败
//            eventBus.setData(it)
//            EventBus.getDefault().post(eventBus)
//        }
        var result = Result();
        result.code = 1004
        result.msg = "已断开通信"
        listener?.systemMsg(result)
    }

    /**
     * 关闭socket连接，在停止使用时，需调用该方法。
     */
    fun disConnect(){
        socket.close()
    }
}
