package org.epoque.tandem.data.local

import app.cash.sqldelight.db.SqlDriver
import org.epoque.tandem.data.local.adapter.EnumColumnAdapter
import org.epoque.tandem.data.local.adapter.GoalStatusAdapter
import org.epoque.tandem.data.local.adapter.GoalTypeAdapter
import org.epoque.tandem.data.local.adapter.instantAdapter
import org.epoque.tandem.data.local.adapter.localDateAdapter
import org.epoque.tandem.data.local.adapter.localTimeAdapter

/**
 * Factory for creating a TandemDatabase instance with custom type adapters.
 */
object TandemDatabaseFactory {
    fun create(driver: SqlDriver): TandemDatabase {
        return TandemDatabase(
            driver = driver,
            TaskAdapter = Task.Adapter(
                owner_typeAdapter = EnumColumnAdapter(),
                statusAdapter = EnumColumnAdapter(),
                priorityAdapter = EnumColumnAdapter(),
                scheduled_dateAdapter = localDateAdapter,
                scheduled_timeAdapter = localTimeAdapter,
                deadlineAdapter = instantAdapter,
                created_atAdapter = instantAdapter,
                updated_atAdapter = instantAdapter
            ),
            WeekAdapter = Week.Adapter(
                start_dateAdapter = localDateAdapter,
                end_dateAdapter = localDateAdapter,
                reviewed_atAdapter = instantAdapter,
                planning_completed_atAdapter = instantAdapter
            ),
            InviteAdapter = Invite.Adapter(
                created_atAdapter = instantAdapter,
                expires_atAdapter = instantAdapter,
                accepted_atAdapter = instantAdapter,
                statusAdapter = EnumColumnAdapter()
            ),
            PartnershipAdapter = Partnership.Adapter(
                created_atAdapter = instantAdapter,
                statusAdapter = EnumColumnAdapter()
            ),
            GoalAdapter = Goal.Adapter(
                typeAdapter = GoalTypeAdapter,
                statusAdapter = GoalStatusAdapter,
                created_atAdapter = instantAdapter,
                updated_atAdapter = instantAdapter
            ),
            GoalProgressAdapter = GoalProgress.Adapter(
                created_atAdapter = instantAdapter
            ),
            PartnerGoalAdapter = PartnerGoal.Adapter(
                typeAdapter = GoalTypeAdapter,
                statusAdapter = GoalStatusAdapter,
                created_atAdapter = instantAdapter,
                updated_atAdapter = instantAdapter,
                synced_atAdapter = instantAdapter
            )
        )
    }
}
