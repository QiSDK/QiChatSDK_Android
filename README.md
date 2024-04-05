引用SDK:

repositories {
   mavenCentral()
……
   maven {
       url "https://jitpack.io"
   }
}

implementation 'com.github.tianxuefeng:TeneasyChatSDK_Android:1.8.3'
（版本号会不断递增，文档只是例子)

初始化SDK:
private lateinit var chatLib: ChatLib
override fun onCreate(savedInstanceState: Bundle?) {
chatLib = ChatLib()
chatLib.listener = this
chatLib?.makeConnect()
}

发消息:

普通文本消息
val sayHello = "你好！"
chatLib.sendMessage(sayHello, CMessage.MessageFormat.MSG_TEXT)
