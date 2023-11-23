package com.utils
import com.contants.KlineConfig
import com.contants.Language

/**
 * 多语言工具类
 *
 * @author: Galon
 * @date: 2022/8/4
 */
object KlineLanguageUtils {
    val curLanguage: Language
        get() {
            val langStr = LocalStore.getInstance()
                .getString(KlineConfig.KEY_LANGUAGE, Language.ENGLISH.language)
                ?: Language.ENGLISH.language
            return convertNameToLanguage(langStr)
        }

    private fun convertNameToLanguage(languageStr: String): Language {
        return when (languageStr) {
            "" -> {
                Language.ENGLISH
            }
            Language.CHINA.language -> {
                Language.CHINA
            }
            Language.KOREA.language -> {
                Language.KOREA
            }
            Language.JAPAN.language -> {
                Language.JAPAN
            }
            Language.CHINA_TW.language -> {
                Language.CHINA_TW
            }
            Language.RUSSIAN.language -> {
                Language.RUSSIAN
            }
            Language.SPANISH.language -> {
                Language.SPANISH
            }
            Language.FRENCH.language -> {
                Language.FRENCH
            }
            Language.GERMAN.language -> {
                Language.GERMAN
            }
            Language.VIETNAMESE.language -> {
                Language.VIETNAMESE
            }
            Language.TURKISH.language -> {
                Language.TURKISH
            }
            Language.DUTCH.language -> {
                Language.DUTCH
            }
            Language.PORTUGUESE.language -> {
                Language.PORTUGUESE
            }
            Language.ITALIAN.language -> {
                Language.ITALIAN
            }
            Language.POLISH.language -> {
                Language.POLISH
            }
            Language.INDONESIAN.language -> {
                Language.INDONESIAN
            }
            Language.UKRAINE.language -> {
                Language.UKRAINE
            }
            else -> {
                Language.ENGLISH
            }
        }
    }
}