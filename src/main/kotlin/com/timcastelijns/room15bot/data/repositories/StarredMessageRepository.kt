package com.timcastelijns.room15bot.data.repositories

import com.timcastelijns.chatexchange.chat.ChatHost
import com.timcastelijns.room15bot.network.StarService
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class StarredMessageRepository(
        private val starService: StarService
) {

    suspend fun getStarredMessagesByPage(page: Int): List<StarredMessage> {
        val data = starService.getStarsDataByPage(page).await()
        return Jsoup.parse(data).extractStarredMessages()
    }

    suspend fun getNumberOfStarredMessagesPages(): Int {
        val data = starService.getStarsDataByPage(1).await()
        return Jsoup.parse(data)
                .select("div.pager").first()
                .select("a[href^=\"?tab=stars&page=\"]")
                .secondToLast()?.run {
                    getElementsByClass("page-numbers").first().text().toInt()
                } ?: 0
    }
}

data class StarredMessage(
        val username: String,
        val message: String,
        val stars: Int,
        val permanentLink: String
)

private fun Document.extractStarredMessages(): List<StarredMessage> {
    val starredMessages = mutableListOf<StarredMessage>()
    val elements = select("div.monologue")

    elements.forEach {
        val message = with(it.select("div.message").first().select("div.content").first()) {
            if (hasText()) {
                text()
            } else {
                "-image-"
            }
        }
        val username = try {
            it.getElementsByAttribute("title").first().text()
        } catch (e: Exception) {
            // Deleted users do not have a a[title] because their is no profile to link to. Their name is in div.username
            it.getElementsByClass("username").first().text()
        }

        val times = it.getElementsByClass("times").first().text()
        val stars = if (times.isNotEmpty()) times.toInt() else 1

        val href = it.select("div.message").first().select("a[href^=\"/transcript\"]").attr("href")
        val permalink = "${ChatHost.STACK_OVERFLOW.baseUrl}$href"

        starredMessages += StarredMessage(username, message, stars, permalink)
    }

    return starredMessages
}

private fun <T> List<T>.secondToLast(): T? = get(size - 2)
