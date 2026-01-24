package org.epoque.tandem.domain.usecase.feed

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.model.FeedItemType
import org.epoque.tandem.domain.repository.FeedRepository

/**
 * A group of feed items for a single day.
 */
data class FeedItemGroup(
    val date: LocalDate,
    val dayLabel: String,     // "Today", "Yesterday", "Monday", etc.
    val dateLabel: String,    // "Thursday, Jan 23", "Jan 20", etc.
    val items: List<FeedItem>
)

/**
 * Filter options for the feed.
 */
enum class FeedFilter {
    ALL,      // Shows all feed items
    TASKS,    // Shows only task-related items
    MESSAGES  // Shows only messages
}

/**
 * Use case for getting feed items grouped by day.
 * Returns a Flow of FeedItemGroup list, with items grouped by their date
 * and sorted in reverse chronological order (newest first).
 */
class GetFeedItemsUseCase(
    private val feedRepository: FeedRepository
) {
    /**
     * Get feed items for a user, optionally filtered by type.
     *
     * @param userId The current user's ID
     * @param filter The filter to apply (ALL, TASKS, or MESSAGES)
     * @param today Today's date for calculating relative day labels
     * @return Flow of grouped feed items
     */
    operator fun invoke(
        userId: String,
        filter: FeedFilter = FeedFilter.ALL,
        today: LocalDate
    ): Flow<List<FeedItemGroup>> {
        val itemsFlow = when (filter) {
            FeedFilter.ALL -> feedRepository.observeFeedItems(userId)
            FeedFilter.TASKS -> feedRepository.observeFeedItemsByType(
                userId,
                listOf(
                    FeedItemType.TASK_COMPLETED,
                    FeedItemType.TASK_ASSIGNED,
                    FeedItemType.TASK_ACCEPTED,
                    FeedItemType.TASK_DECLINED
                )
            )
            FeedFilter.MESSAGES -> feedRepository.observeFeedItemsByType(
                userId,
                listOf(FeedItemType.MESSAGE)
            )
        }

        return itemsFlow.map { items ->
            groupItemsByDay(items, today)
        }
    }

    /**
     * Group feed items by their date and create day labels.
     */
    private fun groupItemsByDay(items: List<FeedItem>, today: LocalDate): List<FeedItemGroup> {
        val timeZone = TimeZone.currentSystemDefault()

        // Group by date
        val grouped = items.groupBy { item ->
            item.timestamp.toLocalDateTime(timeZone).date
        }

        // Convert to FeedItemGroup with labels
        return grouped.map { (date, dayItems) ->
            FeedItemGroup(
                date = date,
                dayLabel = getDayLabel(date, today),
                dateLabel = getDateLabel(date, today),
                items = dayItems.sortedByDescending { it.timestamp }
            )
        }.sortedByDescending { it.date }
    }

    /**
     * Get the day label for a date (Today, Yesterday, day name, or date).
     */
    private fun getDayLabel(date: LocalDate, today: LocalDate): String {
        val yesterday = today.minus(DatePeriod(days = 1))

        return when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                // Check if within the last week
                val daysAgo = today.toEpochDays() - date.toEpochDays()
                if (daysAgo in 2..6) {
                    date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercaseChar() }
                } else {
                    // Format as "Month Day" for older dates
                    formatMonthDay(date)
                }
            }
        }
    }

    /**
     * Get the date label for a date (full date or just month/day).
     */
    private fun getDateLabel(date: LocalDate, today: LocalDate): String {
        val yesterday = today.minus(DatePeriod(days = 1))

        return when (date) {
            today -> formatFullDate(date)      // "Thursday, Jan 23"
            yesterday -> formatFullDate(date)  // "Wednesday, Jan 22"
            else -> {
                val daysAgo = today.toEpochDays() - date.toEpochDays()
                if (daysAgo in 2..6) {
                    formatFullDate(date)
                } else {
                    formatShortDate(date)  // "Jan 20"
                }
            }
        }
    }

    private fun formatFullDate(date: LocalDate): String {
        val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercaseChar() }
        val monthDay = formatMonthDay(date)
        return "$dayName, $monthDay"
    }

    private fun formatMonthDay(date: LocalDate): String {
        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }
        return "$month ${date.dayOfMonth}"
    }

    private fun formatShortDate(date: LocalDate): String {
        return formatMonthDay(date)
    }
}
