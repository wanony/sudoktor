package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Users : IntIdTable() {
    val username = varchar("username", 255).uniqueIndex()
    val email = varchar("email", 255) // TODO update these to be secure
    val password = varchar("password", 255)
    val isAdmin = bool("isAdmin")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    var username by Users.username
    var email by Users.email
    var password by Users.password
    var isAdmin by Users.isAdmin
}

fun createUser(username: String, email: String, password: String, isAdmin: Boolean = false) {
    // TODO check if username already exists
    User.new {
        this.username = username
        this.email = email
        this.password = password
        this.isAdmin = isAdmin
    }
}

fun userByUsername(username: String): User? {
    return transaction { User.find { Users.username eq username } }.firstOrNull()
}

fun userByEmail(email: String): User? {
    return transaction { User.find { Users.email eq email } }.firstOrNull()
}