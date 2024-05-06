*起聊安卓SDK，集成文档*


**引用SDK：**
```
repositories {
   mavenCentral()
……
   maven {
       url "https://jitpack.io"
   }
}

implementation 'com.github.QiSDK:QiChatSDK_Android:1.2.7'
（版本号会不断递增，文档只是例子)
```

如果要使用没有线路智能选择的版本，请指定到1.0.3


**侦测线路：**
```
override fun onCreate(savedInstanceState: Bundle?) {

val lines = arrayOf(["https://qlqiniu.quyou.tech/gw3config.txt","https://ydqlacc.weletter05.com/gw3config.txt"]) ;
val lineLib = LineLib(lines, object : LineDelegate {
   override fun useTheLine(line: Line) {
       initChatSDK(line.VITE_WSS_HOST)
   }
   override fun lineError(error: Result) {
       this@MainActivity.runOnUiThread{
           //binding.tvContent.append(error.message + "\n")
           Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
       }
   }
}, 123) //123是商户id
lineLib.getLine()
}
```


**初始化SDK:**
```
private lateinit var chatLib: ChatLib

private fun initChatSDK(baseUrl: String){
    var wssUrl = "wss://" + baseUrl + "/v1/gateway/h5?"
   //第三个参数是userid, 第四个参数Sign
   chatLib = ChatLib("cert", "token", wssUrl, 1125324, "9zgd9YUc")
   chatLib.listener = this
   chatLib?.makeConnect()
}
```


**发消息:**

```
普通文本消息
val sayHello = "你好！"
chatLib.sendMessage(sayHello, CMessage.MessageFormat.MSG_TEXT)
```

```
发送图片消息：
chatLib.sendMessage("https://sssacc.wwc09.com/3/public/1/1695821236_29310.jpg", CMessage.MessageFormat._MSG_IMG_)

或者这样发：

chatLib.sendMessage("3/public/1/1695821236_29310.jpg", CMessage.MessageFormat._MSG_IMG_)
```


```

视频消息
chatLib.sendMessage("https://www.video.123", CMessage.MessageFormat.MSG_VIDEO)
```


**回复消息：**

```
例如使用一个视频回复msgId

chatLib.sendMessage("2.mp4", CMessage.MessageFormat._MSG_VIDEO_, msgId)

```

**删除/撤回消息：**

```
chatLib.deleteMessage(**msgId**)

删除消息之后在msgReceipt会收到回执，判断msgId是否小于0，通过payloadId从列表找到消息，然后进行UI上的删除

对方删除/撤回也是在msgReceipt里面收到
```


**获取正在发送消息的payloadId：**
```
val payloadId = chatLib.payloadId
```

**客服更换成功：**

```
  客服更换消息包含这些字段
  workerID: Int32 = 0
  workerName: String = String()
  workerAvatar: String = String()
  target: Int64 = 0
  consultId: Int64 = 0
  reason: Gateway_WorkerChangedReason = .unknown
  ```

```
回调函数：workChanged  

收到workChanged的回调后，做这样的判断：
if (msg.reason == .transferWorker
){
   //客服更换了
}
if (msg.reason == .workerDeleted
){
  //被拉黑到xx客服了
}

把consultId存到App里面的一个全局变量
```


**重发消息：**

```
chatLib.resendMSg(msg, payloadId);
```


**实现接口函数：**

```
override fun receivedMsg(msg: CMessage.Message) {
   if (msg.consultId != consultId){
    //忽略消息或给个提示
}
   println(msg)
}

override fun msgReceipt(msg: CMessage.Message, payloadId: Long, msgId: Long, errMsg: String) {
   //println(msg)
   val suc = if (msgId == 0L) "发送失败" else "发送成功"
   println(payloadId.toString() + " " +suc)
}

override fun systemMsg(msg: String) {
   //TODO("Not yet implemented")
   Log.i("MainAct systemMsg", msg)
}

//成功连接，并返回相关信息，例如workerId
override fun connected(c: GGateway.SCHi) {
   val workerId = c.workerId
   Log.i("MainAct connected", "成功连接")
}

override fun workChanged(msg: GGateway.SCWorkerChanged) {
   Log.i("MainAct connected", "已经更换客服")
}
```
**发送心跳：**
```
chatLib.sendHeartBeat()
```
