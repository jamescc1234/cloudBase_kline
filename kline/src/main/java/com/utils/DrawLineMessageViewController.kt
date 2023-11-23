package com.utils

/**
 *
 * @time 2021/4/15 11:06 AM
 * @auther Michael.Xia
 */
interface DrawLineMessageViewController {
    fun updateTitle(title: CharSequence?)

    fun updateContent(content: CharSequence?)

    fun showDrawLineMessageView(show: Boolean)
}