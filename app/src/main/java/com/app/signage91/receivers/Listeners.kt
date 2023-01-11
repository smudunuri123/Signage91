package com.app.signage91.models

import com.app.signage91.models.xml_parser.Article

interface ArticleListener {
    fun onItemClicked(account: Article, position: Int)
}
