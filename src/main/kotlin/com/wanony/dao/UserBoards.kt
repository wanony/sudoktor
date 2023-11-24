package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object UserBoards : IntIdTable() {
    val userId = reference("userId", Users.id)
    val boardId = reference("boardId", Boards.id)
    val isComplete = bool("isComplete").default(false)
    val score = integer("score").default(0)
}

class UserBoard(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserBoard>(UserBoards)
    val userId by UserBoards.userId
    val boardId by UserBoards.boardId
    val isComplete by UserBoards.isComplete
    val score by UserBoards.score
}