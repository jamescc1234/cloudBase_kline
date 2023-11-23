package com.contants

import java.util.*

/**
 * @author: Galon
 * @date: 2022/8/4
 */
enum class Language(val language: String, val display: String, val nameCn: String, val zendesk: String, val locale: Locale) {
    CHINA("cn", "中文简体", "中文简体", "zh-cn", Locale.CHINA),
    ENGLISH("en", "English", "英语", "en-us", Locale.US),
    KOREA("kr", "한국어", "韩语", "ko", Locale.KOREA),
    JAPAN("jp", "日本語", "日语", "ja", Locale.JAPAN),
    CHINA_TW("tw", "中文繁體", "中文繁体", "zh-tw", Locale.TAIWAN),
    RUSSIAN("ru", "Русский", "俄语", "ru", Locale("RU")),
    SPANISH("es", "Español", "西班牙语", "es", Locale("ES")),
    FRENCH("fr", "Français", "法语", "fr", Locale.FRANCE),
    GERMAN("de", "Deutsch", "德语", "de", Locale.GERMAN),
    VIETNAMESE("vn", "Tiếng Việt", "越南语", "vi", Locale("vn")),
    TURKISH("tr", "Türkçe", "土耳其语", "tr", Locale("tr")),
    DUTCH("nl", "Nederlands", "荷兰语", "nl", Locale("nl")),
    PORTUGUESE("pt", "Português", "葡萄牙语", "pt", Locale("pt")),
    ITALIAN("it", "Italiano", "意大利语", "it", Locale.ITALIAN),
    POLISH("pl", "Polski", "波兰语", "pl", Locale("pl")),
    INDONESIAN("id", "Indonesia", "印尼语", "id", Locale("id")),
    UKRAINE("ua", "Українська", "乌克兰语", "uk", Locale("ua"));
}