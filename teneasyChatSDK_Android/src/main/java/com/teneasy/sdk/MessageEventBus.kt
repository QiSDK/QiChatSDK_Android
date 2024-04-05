package com.teneasy.sdk

/**
 * EventBus自定义数据实体
 */
class MessageEventBus<T> {
    var what = 0
    var arg = 0
    var data: T? = null
        private set

    fun setData(data: T) {
        this.data = data
        what = 1
    }
}