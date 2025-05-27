package com.teneasy.sdk
import java.io.Serializable

class Result {
    var code = 0
    var msg = ""
    var data: Any? = null
}




/**
 * API返回数据实体，统一化返回结构，便于解析处理
 * @param <T>
 */
data class ReturnData<T>(
    var code: Int = 0, // Default value for code
    var msg: String? = null, // Default value for msg, nullable
    var data: T? = null // Default value for data, nullable
) : Serializable

/*
{
    "code": 0,
    "msg": "OK",
    "data": {
        "gnsId": "wcs",
        "tenantId": 123
    }
}
 */