package com.app.signage91.models.xml_parser

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Path
import org.simpleframework.xml.Root

@Root(name = "rss", strict = false)
data class Feed @JvmOverloads constructor(
    @field:Element(name = "title")
    @param:Element(name = "title")
    @field:Path("channel")
    @param:Path("channel")
    var channelTitle: String? = null,


    @field:ElementList(name = "item", inline = true, required = false)
    @param:ElementList(name = "item", inline = true, required = false)
    @field:Path("channel")
    @param:Path("channel")
    var articleList: List<Article>? = null,


    @field:Element(name = "image")
    @param:Element(name = "image")
    @field:Path("channel")
    @param:Path("channel")
    var feedImage: FeedImage? = null,

    )


@Root(name = "image", strict = false)
data class FeedImage @JvmOverloads constructor(
    @field:Element(name = "title", required = false)
    //@param:Element(name = "title")
    var title: String? = null,

    @field:Element(name = "url", required = false)
    //@param:Element(name = "title")
    var url: String? = null,

    @field:Element(name = "link", required = false)
    //@param:Element(name = "link")
    var link: String? = null,
)


@Root(name = "item", strict = false)
data class Article @JvmOverloads constructor(
    @field:Element(name = "title", required = false)
    //@param:Element(name = "title")
    var title: String? = null,

    @field:Element(name = "link", required = false)
    //@param:Element(name = "link")
    var link: String? = null,

    @field:Element(name = "description", required = false)
    //@param:Element(name = "description")
    var description: String? = null,

    @field:Element(name = "guid", required = false)
    //@param:Element(name = "guid")
    var guid: String? = null,

    @field:Element(name = "pubDate", required = false)
    //@param:Element(name = "pubDate")
    var pubDate: String? = null

)