package it.av.coroutines_core.collini.net

import it.av.coroutines_core.collini.net.model.Badge
import it.av.coroutines_core.collini.net.model.Repo
import it.av.coroutines_core.collini.net.model.Tag
import it.av.coroutines_core.collini.net.model.User

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

    override suspend fun fetchTopRepo(): List<Repo> {
        return listOf()
    }

}