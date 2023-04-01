package it.av.coroutines_core.collini.net

import it.av.coroutines_core.collini.net.model.Badge
import it.av.coroutines_core.collini.net.model.Repo
import it.av.coroutines_core.collini.net.model.Tag
import it.av.coroutines_core.collini.net.model.User

/**
 * Created by Antonio Vitiello on 13/03/2023.
 */
interface StackOverflowService {

//  @Get("/users")
    suspend fun fetchTopUsers(): List<User>

//  @Get("/users/{userId}/badges")
    suspend fun fetchBadges(
//      @Path("userId")
        userId: Int
    ): List<Badge>

//  @Get("/users/{userId}/top-tags")
    suspend fun fetchTags(
//      @Path("userId")
        userId: Int
    ): List<Tag>

//  @Get("/users/{userId}/stars")
    suspend fun fetchTopRepo(): List<Repo>

}