package com.teneasy.sdk

import com.google.protobuf.Timestamp
import com.teneasy.sdk.TimeUtil
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间格式化工具
 */
object TimeUtil {
    /**
     * 返回指定pattern样的日期时间字符串。
     *
     * @param dt
     * @param pattern
     * @return 如果时间转换成功则返回结果，否则返回空字符串""
     */
    fun getTimeString(dt: Date?, pattern: String?): String {
        return try {
            val sdf = SimpleDateFormat(pattern) //"yyyy-MM-dd HH:mm:ss"
            sdf.timeZone = TimeZone.getDefault()
            sdf.format(dt)
        } catch (e: Exception) {
            ""
        }
    }

    fun serverTimeToLocal(){
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val dateTime = simpleDateFormat.format(calendar.time).toString()

    }

    fun msgTime() : Timestamp{
        var d = Timestamp.newBuilder()
        val cal = Calendar.getInstance()
        cal.time = Date()
        val millis = cal.timeInMillis
        d.seconds = (millis * 0.001).toLong()
        return d.build()
    }

    /**
     * 1）7天之内的日期显示逻辑是：今天、昨天(-1d)、前天(-2d)、星期？（只显示总计7天之内的星期数，即<=-4d）；<br></br>
     * 2）7天之外（即>7天）的逻辑：直接显示完整日期时间。
     *
     * @param srcDate         要处理的源日期时间对象
     * @param mustIncludeTime true表示输出的格式里一定会包含“时间:分钟”，否则不包含（参考微信，不包含时分的情况，用于首页“消息”中显示时）
     * @return 输出格式形如：“10:30”、“昨天 12:04”、“前天 20:51”、“星期二”、“2019/2/21 12:09”等形式
     * @since 4.5
     */
    fun getTimeStringAutoShort2(srcDate: Date?, mustIncludeTime: Boolean): String {
        var ret = ""
        try {
            val gcCurrent = GregorianCalendar()
            gcCurrent.time = Date()
            val currentYear = gcCurrent[GregorianCalendar.YEAR]
            val currentMonth = gcCurrent[GregorianCalendar.MONTH] + 1
            val currentDay = gcCurrent[GregorianCalendar.DAY_OF_MONTH]
            val gcSrc = GregorianCalendar()
            gcSrc.time = srcDate
            val srcYear = gcSrc[GregorianCalendar.YEAR]
            val srcMonth = gcSrc[GregorianCalendar.MONTH] + 1
            val srcDay = gcSrc[GregorianCalendar.DAY_OF_MONTH]

            // 要额外显示的时间分钟
            val timeExtraStr = if (mustIncludeTime) " " + getTimeString(srcDate, "HH:mm") else ""

            // 当年
            ret = if (currentYear == srcYear) {
                val currentTimestamp = gcCurrent.timeInMillis
                val srcTimestamp = gcSrc.timeInMillis

                // 相差时间（单位：毫秒）
                val delta = currentTimestamp - srcTimestamp
                if (currentMonth == srcMonth && currentDay == srcDay) {
                    // 时间相差60秒以内
//                    if (delta < 60 * 1000)
//                        ret = "刚刚";
//                    else
                    getTimeString(srcDate, "HH:mm")
                } else {
                    // 昨天（以“现在”的时候为基准-1天）
                    val yesterdayDate = GregorianCalendar()
                    yesterdayDate.add(GregorianCalendar.DAY_OF_MONTH, -1)

                    // 前天（以“现在”的时候为基准-2天）
                    val beforeYesterdayDate = GregorianCalendar()
                    beforeYesterdayDate.add(GregorianCalendar.DAY_OF_MONTH, -2)

                    // 用目标日期的“月”和“天”跟上方计算出来的“昨天”进行比较，是最为准确的（如果用时间戳差值
                    // 的形式，是不准确的，比如：现在时刻是2019年02月22日1:00、而srcDate是2019年02月21日23:00，
                    // 这两者间只相差2小时，直接用“delta/(3600 * 1000)” > 24小时来判断是否昨天，就完全是扯蛋的逻辑了）
                    if (srcMonth == yesterdayDate[GregorianCalendar.MONTH] + 1
                        && srcDay == yesterdayDate[GregorianCalendar.DAY_OF_MONTH]
                    ) {
                        "昨天$timeExtraStr" // -1d
                    } else if (srcMonth == beforeYesterdayDate[GregorianCalendar.MONTH] + 1
                        && srcDay == beforeYesterdayDate[GregorianCalendar.DAY_OF_MONTH]
                    ) {
                        "前天$timeExtraStr" // -2d
                    } else {
                        // 跟当前时间相差的小时数
                        val deltaHour = delta / (3600 * 1000)

                        // 如果小于 7*24小时就显示星期几
                        if (deltaHour < 7 * 24) {
                            val weekday = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")

                            // 取出当前是星期几
                            val weedayDesc = weekday[gcSrc[GregorianCalendar.DAY_OF_WEEK] - 1]
                            weedayDesc + timeExtraStr
                        } else getTimeString(srcDate, "yyyy/M/d") + timeExtraStr
                    }
                }
            } else getTimeString(srcDate, "yyyy/M/d") + timeExtraStr
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ret
    }
}