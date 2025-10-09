package com.teneasy.sdk

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import android.webkit.WebSettings
import com.google.protobuf.Timestamp
import com.teneasyChat.api.common.CEntrance.ClientType
import com.teneasyChat.api.common.CMessage
import com.teneasyChat.api.common.CMessage.Message
import com.teneasyChat.api.common.CMessage.MessageFormat
import com.teneasyChat.api.common.CMessage.WithAutoReply
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
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


interface TeneasySDKDelegate {
    // 收到消息
    fun receivedMsg(msg: CMessage.Message)

   /**
   消息回执
    @msg 已发送的消息
    @payloadId
    @msgId, 如果是0，表示服务器没有生成消息id, 发送失败
    */
    fun msgReceipt(msg: CMessage.Message?, payloadId: Long, msgId: Long, errMsg: String = "") // 使用Long代替UInt64

    /**
    消息删除回执
    @msg 已发送的消息
    @payloadId
    @msgId
     */
    fun msgDeleted(msg: CMessage.Message?, payloadId: Long, msgId: Long, errMsg: String = "") // 使用Long代替UInt64

    /**
     * 系统消息，用于显示Tip
     * @param msg
     */
    fun systemMsg(msg: Result)

    /**
     * 连接成功回调
     * @param SCHi
     */
    fun connected(c: GGateway.SCHi)

    // 客服更换回调
    fun workChanged(msg: GGateway.SCWorkerChanged)
}

/**
 * 通讯核心类，提供了发送消息、解析消息等功能
 */
class ChatLib {
//你可以在不创建类实例的情况下，直接通过类名来调用伴生对象里的方法和属性（例如 ChatLib.getInstance()）
    companion object {
        //确保了对 instance 变量的读写操作对于所有线程都是立即可见的。简单来说，当一个线程修改了 instance 的值，其他线程会立刻看到这个修改，这对于防止多线程环境下的初始化问题至关重要。
        @Volatile
        private var instance: ChatLib? = null

       //双重检查锁定（Double-Checked Locking） 模式来实现线程安全的懒汉式单例。
        fun getInstance(): ChatLib {
            //这个同步块确保在同一时间只有一个线程可以执行内部的代码
            return instance ?: synchronized(this) {
                instance ?: ChatLib().also { instance = it }
            }
        }
    }
    private val TAG = "ChatLib"

    // 协程作用域和调度器
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val websocketDispatcher = Dispatchers.IO
    private val messageDispatcher = Dispatchers.Default
    private val timerDispatcher = Dispatchers.Default
    private val stateDispatcher = Dispatchers.Default

    // 线程安全的锁
    private val stateMutex = Mutex()

    // 通讯地址
   private var baseUrl = ""
     var isConnected = false
        private set

    // 连接状态标识
    private var isConnecting = false

    // 待发送的消息队列
    private data class PendingPayload(val id: Long?, val data: ByteArray)
    private val pendingPayloads = mutableListOf<PendingPayload>()

    // 当前发送的消息实体，便于上层调用的逻辑处理
    var sendingMessage: CMessage.Message? = null
    private var chatId: Long = 0L //2692944494608客服下线了
    private var token: String? = ""//qi xin
    private var cert: String? = ""
    private var userId: Int = 0
    private var mySign: String? = ""//qi xin
    private var socket: WebSocketClient? = null
    var listener: TeneasySDKDelegate? = null
    var payloadId = 0L
    private val msgList: MutableMap<Long, CMessage.Message> = mutableMapOf()
    var replyMsgId: Long = 0L
    var consultId: Long = 0L
    private var heartTimer: Timer? = null

    private var sessionTime: Int = 0
    private var beatTimes = 0
    private var maxSessionMinutes = 9000000//相当于不设置会话实际限制 //测试放1分钟，上线放120或90
    private var withAutoReply: WithAutoReply? = null
    private var custom: String = ""
    private var msgFormat: MessageFormat = MessageFormat.MSG_TEXT
    private var fileSize = 0
    private var fileName = ""

    // 网络监听
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var applicationContext: Context? = null

    fun init(cert: String, token:String, baseUrl:String = "", userId: Int, sign:String,  chatID: Long = 0, custom: String = "", maxSessionMinutes: Int = 9000000, context: Context? = null) {
        this.chatId = chatID
        this.token = token

        fileSize = 0
        fileName = ""
        this.baseUrl = baseUrl
        this.userId = userId;
        this.mySign = sign
        this.cert = cert

        sessionTime = 0
        beatTimes = 0
        this.custom = custom
        this.maxSessionMinutes = maxSessionMinutes

        // 初始化网络监听
        context?.let {
            this.applicationContext = it.applicationContext
            setupNetworkMonitoring()
        }
    }

    /**
     * 设置网络状态监听
     */
    private fun setupNetworkMonitoring() {
        applicationContext?.let { context ->
            connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "网络可用")
                    // 如果之前因网络问题断开，可以尝试重连
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "网络断开")
                    scope.launch(stateDispatcher) {
                        stateMutex.withLock {
                            if (isConnected) {
                                disConnected(1009, "网络中断了")
                            }
                        }
                    }
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    val hasCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    Log.d(TAG, "网络类型变化: WiFi=$hasWifi, Cellular=$hasCellular")
                }
            }

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            try {
                connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
            } catch (e: Exception) {
                Log.e(TAG, "注册网络监听失败: ${e.message}")
            }
        }
    }

    /**
     * 停止网络监听
     */
    private fun stopNetworkMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e(TAG, "取消网络监听失败: ${e.message}")
            }
        }
        networkCallback = null
        connectivityManager = null
    }

    fun getHeaders(context: Context): Map<String, String> {
        val userAgent = WebSettings.getDefaultUserAgent(context)
        return mapOf(
            "User-Agent" to userAgent,
            "x-trace-id" to UUID.randomUUID().toString()
        )
    }

    /**
     * 启动socket连接
      */
    fun makeConnect(){
        scope.launch(stateDispatcher) {
            stateMutex.withLock {
                if (isConnecting || isConnected) {
                    Log.i(TAG, "已经在连接中或已连接，跳过重复连接")
                    return@launch
                }
                isConnecting = true
            }

            enqueueWebsocketConnection()
        }
    }

    /**
     * 安全构建 WebSocket URL
     */
    private fun buildWebSocketUrl(): String? {
        try {
            val rd = Random().nextInt(1000000) + 1000000
            val dt = Date().time

            // 使用 URLEncoder 安全编码参数
            val params = mutableMapOf<String, String>()
            params["cert"] = cert ?: ""
            params["token"] = token ?: ""
            params["userid"] = userId.toString()
            params["custom"] = custom
            params["ty"] = ClientType.CLIENT_TYPE_USER_APP_ANDROID.number.toString()
            params["dt"] = dt.toString()
            params["sign"] = mySign ?: ""
            params["rd"] = rd.toString()

            val queryString = params.entries.joinToString("&") { (key, value) ->
                "$key=${URLEncoder.encode(value, "UTF-8")}"
            }

            return "$baseUrl$queryString"
        } catch (e: Exception) {
            Log.e(TAG, "构建URL失败: ${e.message}")
            return null
        }
    }

    /**
     * 将 WebSocket 连接加入队列
     */
    private fun enqueueWebsocketConnection() {
        scope.launch(websocketDispatcher) {
            val url = buildWebSocketUrl()
            if (url == null) {
                stateMutex.withLock {
                    isConnecting = false
                }
                Log.e(TAG, "URL构建失败，连接中止")
                return@launch
            }

            Log.i(TAG, "连接WSS (URL已安全编码)")

            val headers = applicationContext?.let { getHeaders(it) } ?: mapOf(
                "x-trace-id" to UUID.randomUUID().toString()
            )
            Log.d(TAG, "x-trace-id: ${headers["x-trace-id"]}")

            withContext(Dispatchers.Main) {
                socket = object : WebSocketClient(URI(url), Draft_6455(), headers) {
                    override fun onMessage(message: String) {
                    }

                    override fun onMessage(bytes: ByteBuffer?) {
                        super.onMessage(bytes)
                        if (this@ChatLib.socket != this) return
                        if (bytes != null)
                            receiveMsg(bytes.array())
                    }
                    override fun onOpen(handshake: ServerHandshake?) {
                        Log.i(TAG, "opened connection")
                        if (this@ChatLib.socket != this) return
                    }
                    override fun onClose(code: Int, reason: String, remote: Boolean) {
                        Log.i(TAG, "closed connection code: $code reason: $reason")
                        if (this@ChatLib.socket != this) return
                        disConnected(code)
                    }
                    override fun onError(ex: Exception) {
                        if (this@ChatLib.socket != this) return
                        disConnected(1001,"未知错误")
                        Log.i(TAG, ex.message ?:"未知错误")
                    }
                }
                socket?.connect()
            }
        }
    }

    /**
     * 发送文本类型、图片类型的消息
     * @param msg   消息内容或图片url,音频url..。
     */
     fun sendMessage(msg: String, type: MessageFormat, consultId: Long, replyMsgId: Long = 0, withAutoReply: WithAutoReply? = null, fileSize: Int = 0, fileName: String = "") {
        this.replyMsgId = replyMsgId;
         this.consultId = consultId;
        this.withAutoReply = withAutoReply
        this.fileSize = fileSize
        this.fileName = fileName
        this.msgFormat = type
      if (type == MessageFormat.MSG_TEXT){
          sendTextMessage(msg)
      }else if (type == MessageFormat.MSG_IMG){
          sendImageMessage(msg)
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

    /**
     * 删除消息
     * @param MsgId 消息ID
     */
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
        msg.msgFmt = this.msgFormat
        msg.msgTime = TimeUtil.msgTime()

        if (withAutoReply != null && (withAutoReply?.id ?: 0) > 0) {
            var aList = ArrayList<WithAutoReply>()
            aList.add(withAutoReply!!)
            msg.addAllWithAutoReplies(aList)
        }

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
        msg.msgFmt = this.msgFormat
        msg.msgTime = TimeUtil.msgTime()

        if (withAutoReply != null && (withAutoReply?.id ?: 0) > 0) {
            var aList = ArrayList<WithAutoReply>()
            aList.add(withAutoReply!!)
            msg.addAllWithAutoReplies(aList)
        }

        sendingMessage = msg.build()
    }

    /**
     * 发送视频类型的消息
     * @param url   视频地址
     */
     fun sendVideoMessage(url: String, thumbnail: String = "", hlsUri: String = "", consultId: Long, replyMsgId: Long = 0, withAutoReply: WithAutoReply? = null) {
        this.replyMsgId = replyMsgId;
        this.consultId = consultId;
        this.withAutoReply = withAutoReply
        //第一层
        val content = CMessage.MessageVideo.newBuilder()
        content.thumbnailUri = thumbnail
        content.hlsUri = hlsUri
        content.uri = url

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setConsultId(this.consultId)
        msg.setVideo(content)
        msg.sender = 0
        msg.replyMsgId = this.replyMsgId
        msg.chatId = chatId
        msg.worker = 0
        msg.msgFmt = MessageFormat.MSG_VIDEO
        msg.msgTime = TimeUtil.msgTime()

        if (withAutoReply != null && (withAutoReply?.id ?: 0) > 0) {
            var aList = ArrayList<WithAutoReply>()
            aList.add(withAutoReply!!)
            msg.addAllWithAutoReplies(aList)
        }
        sendingMessage = msg.build()
        sendingMessage?.let {
            doSendMsg(it)
        }
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
        msg.msgFmt = this.msgFormat
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
        content.fileName = this.fileName
        content.size = this.fileSize

        //第二层
        val msg = CMessage.Message.newBuilder()
        msg.setConsultId(this.consultId)
        msg.setFile(content)
        msg.msgFmt = MessageFormat.MSG_FILE
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
     * 发送文本消息（同步执行，匹配iOS实现）
     * @param textMsg MessageItem
     */
    private fun doSendMsg(cMsg: CMessage.Message, act: GAction.Action = GAction.Action.ActionCSSendMsg, payload_Id: Long = 0) {
        //scope.launch(messageDispatcher) { //这是异步，会导致App拿不到对应的payloadId
        var payloadIdentifier = payload_Id
        var shouldTrackMessage = false
        runBlocking {
            stateMutex.withLock {
                //payload_id != 0的时候，可能是重发，重发不需要+1
                if (sendingMessage?.msgOp == CMessage.MessageOperate.MSG_OP_POST && payload_Id == 0L) {
                    payloadId += 1
                    payloadIdentifier = payloadId
                    Log.i(TAG, "payloadId + 1: ${payloadId}")
                    msgList[payloadId] = cMsg
                    shouldTrackMessage = true
                } else if (payload_Id == 0L) {
                    payloadIdentifier = payloadId
                } else if (sendingMessage?.msgOp == CMessage.MessageOperate.MSG_OP_POST) {
                    shouldTrackMessage = true
                    msgList[payloadIdentifier] = cMsg
                }

                if(!isConnected && payload_Id == 0L) {
                    // 将消息加入待发送队列
                    val cSendMsg = GGateway.CSSendMessage.newBuilder()
                    cSendMsg.msg = cMsg
                    val cSendMsgData = cSendMsg.build().toByteString()

                    val payload = GPayload.Payload.newBuilder()
                    payload.data = cSendMsgData
                    payload.act = act
                    payload.id = payloadIdentifier

                    try {
                        val payloadData = payload.build().toByteArray()
                        pendingPayloads.add(PendingPayload(payloadIdentifier, payloadData))
                        Log.i(TAG, "消息已加入待发送队列: payloadId=$payloadIdentifier")
                    } catch (e: Exception) {
                        Log.e(TAG, "序列化消息失败: ${e.message}")
                        if (shouldTrackMessage) {
                            msgList.remove(payloadIdentifier)
                        }
                    }

                    // 如果未连接且未在连接中，尝试连接
                    if (!isConnecting) {
                        isConnecting = true
                        scope.launch(websocketDispatcher) {
                            enqueueWebsocketConnection()
                        }
                    }
                    return@runBlocking
                }
            }
        }

        // 第三层
        val cSendMsg = GGateway.CSSendMessage.newBuilder()
        cSendMsg.msg = cMsg

        val cSendMsgData = cSendMsg.build().toByteString()

        //第四层
        val payload = GPayload.Payload.newBuilder()
        payload.data = cSendMsgData
        payload.act = act
        payload.id = payloadIdentifier

        Log.i(TAG, "sending payloadId: ${payload.id}")

        val canSend = runBlocking {
            stateMutex.withLock {
                isConnected && socket != null && socket?.isOpen == true
            }
        }

        if (!canSend) {
            Log.w(TAG, "连接未就绪，消息未发送")
            return
        }

        try {
            val payloadData = payload.build().toByteArray()
            writeToSocket(payloadData, payloadIdentifier)
        } catch (ex: Exception) {
            Log.e(TAG, "发送消息异常: ${ex.message}")
            if (shouldTrackMessage) {
                runBlocking {
                    stateMutex.withLock {
                        msgList.remove(payloadIdentifier)
                    }
                }
            }
        }
    }

    /**
     * 写入 Socket 数据
     */
    private fun writeToSocket(data: ByteArray, payloadId: Long? = null) {
        scope.launch(Dispatchers.Main) {
            val currentSocket = socket
            if (currentSocket == null || !currentSocket.isOpen) {
                stateMutex.withLock {
                    // 检查是否已在队列中
                    val exists = pendingPayloads.any { it.id == payloadId && it.data.contentEquals(data) }
                    if (!exists) {
                        pendingPayloads.add(PendingPayload(payloadId, data))
                    }

                    if (!isConnecting) {
                        isConnecting = true
                        scope.launch(websocketDispatcher) {
                            enqueueWebsocketConnection()
                        }
                    }
                    isConnected = false
                }
                return@launch
            }

            try {
                Log.d(TAG, "开始发送 payloadId=$payloadId")
                currentSocket.send(data)
                Log.d(TAG, "消息已发送 payloadId=$payloadId")
            } catch (e: Exception) {
                Log.e(TAG, "发送失败: ${e.message}")
            }
        }
    }

    /**
     * 刷新待发送队列
     */
    private fun flushPendingPayloads() {
        scope.launch(messageDispatcher) {
            val queuedItems = stateMutex.withLock {
                val items = pendingPayloads.toList()
                pendingPayloads.clear()
                items
            }

            Log.i(TAG, "刷新待发送队列: ${queuedItems.size} 条消息")
            for (item in queuedItems) {
                writeToSocket(item.data, item.id)
            }
        }
    }

  private fun updateSecond() {
        sessionTime++
        if (sessionTime % 30 == 0) { // Send a heartbeat every 8 seconds
            beatTimes++
            // Log the sending of the heartbeat
            Log.d(TAG, "Sending heartbeat $beatTimes ${Date()}")
            sendHeartBeat()
        }

        if (sessionTime > maxSessionMinutes * 60) { // Stop sending heartbeats after the maximum session time
           disConnected(1005, "会话超时")
           // disConnect()//停止计时器等
        }
    }

    /**
     *  心跳，一般建议每隔60秒调用
     */
   private fun sendHeartBeat(){
       scope.launch(stateDispatcher) {
           stateMutex.withLock {
               if (!isConnected || socket == null || socket?.isOpen == false) return@launch
           }

           val buffer = ByteArray(1)
           buffer[0] = 0
           try {
               withContext(Dispatchers.Main) {
                   socket?.send(buffer)
               }
           }catch (ex: Exception){
               Log.i(TAG, "心跳发送失败: ${ex.message}")
           }
       }
    }

    /**
     * socket消息解析，内部方法
     * @param data
     */
    private fun receiveMsg(data: ByteArray) {
        scope.launch(messageDispatcher) {
            /*
            walter, [17 May 2024 at 3:32:00 PM (17 May 2024 at 3:32:23 PM)]:\n...HeartBeatFlag         = 0x0\nKickFlag              = 0x1\nInvalidTokenFlag      = 0x2\n PermChangedFlag       = 0x3\nEntranceNotExistsFlag = 0x4\n\n如果这个字节的值是 0 ，表示心跳...
             */
            if (data.size == 1) {
                val d = String(data, StandardCharsets.UTF_8)

                when {
                    d.contains("\u0000") -> {
                        Log.i(TAG, "收到心跳回执\n")
                    }
                    d.contains("\u0001") -> {
                        disConnected(1010, "在别处登录了")
                        Log.i(TAG, "收到1字节回执$d 在别处登录了\n{$token")
                    }
                    d.contains("\u0002") -> {
                        disConnected(1002, "无效的Token\n")
                        Log.i(TAG, "收到1字节回执$d 无效的Token\n")
                    }
                    d.contains("\u0003") -> {
                        //disConnected(1003, "PermChangedFlag\n")
                        Log.i(TAG, "收到1字节回执 0003")
                    }
                    else -> {
                        Log.i(TAG, "收到1字节回执$d\n")
                    }
                }
            }
            else {
                val recvPayLoad = GPayload.Payload.parseFrom(data)
                val msgData = recvPayLoad.data
                //有收到消息，就重设超时时间。
                resetSessionTime()
                //收到消息
                if(recvPayLoad.act == GAction.Action.ActionSCRecvMsg) {
                    val recvMsg = GGateway.SCRecvMessage.parseFrom(msgData)
                    //收到对方撤回消息
                    if (recvMsg.msg.msgOp == CMessage.MessageOperate.MSG_OP_DELETE){
                        listener?.msgDeleted(recvMsg.msg, recvPayLoad.id, -1)
                    }else{
                        recvMsg.msg.let {
                            listener?.receivedMsg(it)
                        }
                    }
                } else if(recvPayLoad.act == GAction.Action.ActionSCHi) {
                    val msg = GGateway.SCHi.parseFrom(msgData)

                    stateMutex.withLock {
                        token = msg.token
                        payloadId = recvPayLoad.id
                        isConnected = true
                        isConnecting = false
                    }

                    Log.i(TAG, "初始payloadId:" + payloadId + "\n")

                    withContext(Dispatchers.Main) {
                        listener?.connected(msg)
                    }

                    startTimer()

                    // 刷新待发送队列
                    flushPendingPayloads()

                } else if(recvPayLoad.act == GAction.Action.ActionForward) {
                    val msg = GGateway.CSForward.parseFrom(msgData)
                    Log.i(TAG, "forward: ${msg.data}")
                }  else if(recvPayLoad.act == GAction.Action.ActionSCDeleteMsgACK) {
                    //手机端撤回消息，并收到成功回执
                    val scMsg = GGateway.SCSendMessage.parseFrom(msgData)
                    val msg = Message.newBuilder()
                    msg.msgId = scMsg.msgId;
                    msg.msgOp = CMessage.MessageOperate.MSG_OP_DELETE;
                    Log.i(TAG, "删除回执收到A：消息ID: ${msg.msgId}")
                    //var cMsg = msgList[payLoad.id]
                    //if (cMsg != null) {
                        Log.i(TAG, "删除成功")
                        listener?.msgDeleted(msg.build(), recvPayLoad.id, -1)
                    //}
                }  else if(recvPayLoad.act == GAction.Action.ActionSCDeleteMsg) {
                    val scMsg = GGateway.SCRecvMessage.parseFrom(msgData)
                    val msg = CMessage.Message.newBuilder()
                    msg.msgId = scMsg.msg.msgId;
                    msg.msgOp = CMessage.MessageOperate.MSG_OP_DELETE
                    listener?.msgDeleted(msg.build(), recvPayLoad.id, -1)
                    Log.i(TAG, "对方删除了消息： payload ID${recvPayLoad.id}")
                } else if(recvPayLoad.act == GAction.Action.ActionSCSendMsgACK) {//消息回执
                    val scMsg = GGateway.SCSendMessage.parseFrom(msgData)
                    chatId = scMsg.chatId
                    Log.i(TAG, "收到消息回执 ActionSCSendMsgACK")

                    val cMsg: Message? = stateMutex.withLock {
                        msgList[recvPayLoad.id]
                    }
                    //if (cMsg != null){
                    Log.i(TAG, "收到消息回执: ${scMsg.msgId} : ${recvPayLoad.id}")
                    if (sendingMessage?.msgOp == CMessage.MessageOperate.MSG_OP_DELETE){
                        listener?.msgDeleted(cMsg, recvPayLoad.id, -1)
                        Log.i(TAG, "删除成功")
                    }else{
                        listener?.msgReceipt(cMsg, recvPayLoad.id, scMsg.msgId, scMsg.errMsg)
                    }
                    //}
                    Log.i(TAG, "消息ID: ${scMsg.msgId}")
                } else if(recvPayLoad.act == GAction.Action.ActionSCWorkerChanged){
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
    }

    fun resetSessionTime(){
        scope.launch(timerDispatcher) {
            sessionTime = 0
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

    private fun disConnected(code: Int = 1006, msg: String = "已断开通信"){
        scope.launch(messageDispatcher) {
            var result = Result();
            result.code = code
            result.msg = msg

            withContext(Dispatchers.Main) {
                listener?.systemMsg(result)
            }

            disConnectInternal()
            sendingMessage = null

            stateMutex.withLock {
                isConnected = false
                isConnecting = false
            }

            Log.i(TAG, "ChatLib disConnected code:" + code + " msg:" + msg);
        }
    }

    /**
     * 内部断开连接方法
     */
    private suspend fun disConnectInternal() {
        withContext(Dispatchers.Main) {
            stopTimer()
            socket?.close()
            socket = null
        }
    }

    // 启动计时器，持续调用心跳方法
    private fun startTimer() {
        scope.launch(timerDispatcher) {
            if (heartTimer == null) {
                heartTimer = Timer()
                heartTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        //需要执行的任务
                        scope.launch(timerDispatcher) {
                            stateMutex.withLock {
                                if (isConnected) {
                                    updateSecond()
                                }
                            }
                        }
                    }
                }, 0,1000)
            }
        }
    }

    // 关闭计时器
    private fun stopTimer() {
        sessionTime = 0
        beatTimes = 0
        if(heartTimer != null) {
            heartTimer?.cancel()
            heartTimer = null
        }
    }

    /**
     * 关闭socket连接，在停止使用时，需调用该方法。
     */
    fun disConnect(){
        scope.launch(websocketDispatcher) {
            stopTimer()
            stopNetworkMonitoring()

            withContext(Dispatchers.Main) {
                if (socket != null) {
                    socket?.close()
                    socket = null
                }
            }

            stateMutex.withLock {
                isConnected = false
                isConnecting = false
                pendingPayloads.clear()
                msgList.clear()
                payloadId = 0
            }

            sendingMessage = null
            Log.i(TAG, "ChatLib 已断开并清理资源")
        }
    }
}
