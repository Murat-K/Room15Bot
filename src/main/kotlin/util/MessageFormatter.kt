package util

import bot.usecases.StarsData
import bot.usecases.truncate
import com.timcastelijns.chatexchange.chat.User
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MessageFormatter {

    fun asTableString(starsData: StarsData) = with(starsData) {
        if (starredMessages.isEmpty()) {
            return "No starred messages found"
        }

        val nameColumnMinLength = 6
        val nameColumnMaxLength = 10
        val messageColumnMaxLength = 48

        val longestNameLength = starredMessages.maxBy { it.username.length }!!.username.length
        val nameColumnLength = when {
            longestNameLength >= nameColumnMaxLength -> nameColumnMaxLength
            longestNameLength < nameColumnMinLength -> nameColumnMinLength
            else -> longestNameLength
        }

        val userHeader = "User".padEnd(nameColumnLength)
        val messageHeader = "Message ($totalStarredMessages)".padEnd(messageColumnMaxLength)
        val starsHeader = "Stars ($totalStars)"

        val header = " $userHeader | $messageHeader | $starsHeader | Link"
        val separator = "-".repeat(header.length)

        val table = mutableListOf<String>()
        table.add(header)
        table.add(separator)

        starredMessages.forEach {
            val user = it.username.truncate(nameColumnLength).padEnd(nameColumnLength)
            val message = it.message.sanitize().truncate(messageColumnMaxLength).padEnd(messageColumnMaxLength)
            val stars = it.stars.toString().truncate(starsHeader.length).padEnd(starsHeader.length)
            val permanentLink = ""
            val line = " $user | $message | $stars |$permanentLink"
            table.add(line)
        }

        table.joinToString("\n") { "    $it" }
    }

    fun asDoneString() = "Done."

    fun asReminderString(triggerDate: Instant): String {
        val dtf = DateTimeFormatter.ofPattern("'at' HH:mm 'on' dd MMMM yyyy")
                .withZone(ZoneOffset.UTC)

        return "Ok, I will remind you ${dtf.format(triggerDate)} (UTC)"
    }

    fun asStatsString(user: User, stats: String) = "Stats for ${user.name} -- $stats"

    fun asRequestedAccessString(user: User, stats: String) = "${user.name} requested access. $stats"

    private fun String.sanitize() = this.replace("\r", "").replace("\n", " ").trimEnd()

}