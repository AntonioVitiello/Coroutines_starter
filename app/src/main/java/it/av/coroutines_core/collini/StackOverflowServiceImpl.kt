package it.av.coroutines_core.collini

/**
 * Created by Antonio Vitiello on 13/03/2023.
 */
class StackOverflowServiceImpl : StackOverflowService {

    override suspend fun fetchTopUsers(): List<User> {
        return listOf()
    }

    override suspend fun fetchBadges(userId: Int): List<Badge> {
        return listOf()
    }

    override suspend fun fetchTags(userId: Int): List<Tag> {
        return listOf()
    }

}