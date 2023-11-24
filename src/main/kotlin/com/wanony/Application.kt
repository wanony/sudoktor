package com.wanony

import com.wanony.dao.Boards
import com.wanony.dao.UserBoards
import com.wanony.dao.Users
import com.wanony.dao.createUser
import com.wanony.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.html.*
import io.ktor.server.auth.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.css.CssBuilder
import kotlinx.css.hex
import kotlinx.css.px

fun getEnvOrProperty(name: String): String {
    return try {
        System.getenv(name)
    } catch (e: NullPointerException) {
        try {
            System.getProperty(name) ?: throw NullPointerException(name)
        } catch (e: NullPointerException) {
            println("Error: Missing environment or system property named: $name")
            throw e
        }
    }
}

fun getEnvOrPropertyWithFallback(name: String, fallback: String): String {
    return try {
        System.getenv(name)
    } catch (e: NullPointerException) {
        try {
            System.getProperty(name) ?: throw NullPointerException(name)
        } catch (e: NullPointerException) {
            getEnvOrProperty(fallback)
        }
    }
}

val database = Database.connect(
    "jdbc:postgresql://${
        getEnvOrPropertyWithFallback(
            "TEST_DATABASE_URI",
            "DATABASE_URI"
        )
    }:${
        getEnvOrPropertyWithFallback(
            "TEST_DATABASE_PORT",
            "DATABASE_PORT"
        )
    }/${getEnvOrPropertyWithFallback("TEST_DATABASE_NAME", "DATABASE_NAME")}",
    driver = "org.postgresql.Driver",
    user = getEnvOrPropertyWithFallback("TEST_DATABASE_USERNAME", "DATABASE_USERNAME"),
    password = getEnvOrPropertyWithFallback("TEST_DATABASE_PASSWORD", "DATABASE_PASSWORD")
)

enum class PageType(val pageName: String, val url: String, val iconClass: String) {
    HOME("Home","/", "fa-home"),
    LOGIN("Login", "/login", "fa-sign-in"),
    SIGN_UP("SignUp", "/sign-up", "fa-user-plus"),
    PLAY("Play", "/play", "fa-gamepad"),
    // TODO Multiplayer game
    PROFILE("Profile", "/profile", "fa-user"),
}

class LayoutTemplate(val call: ApplicationCall, val pageType: PageType, val pageTitle: String = ""): Template<HTML> {
    private val navContent = TemplatePlaceholder<TopNavBarTemplate>()
    val content = Placeholder<FlowContent>()
    override fun HTML.apply() {
        lang = "en"
        head {
            title { +(pageTitle.ifEmpty { pageType.pageName } + " - Sudoku")}
            link("/styles.css", "stylesheet", "text/css")
        }
        body {
            insert(TopNavBarTemplate(pageType), navContent)
            insert(content)
        }
    }
}

private class TopNavBarTemplate(val pageType: PageType) : Template<FlowContent> {
    override fun FlowContent.apply() {
        nav(classes = "navbar navbar-expand-lg navbar-dark navbar-background-color") {
            div ("container-fluid") {
                a(href="/", classes = "navbar-brand") {
                    img(src = "/static/logo.png") {
                        height = 50.px.toString()
                    }
                }
                button(type = ButtonType.button, classes = "navbar-toggler") {
                    onClick = "$('.collapse').collapse('toggle');"
                    span(classes = "navbar-toggler-icon")
                    +""
                }
                div(classes = "navbar-collapse collapse text-center") {
                    ul(classes = "navbar-nav me-auto mb-2 mb-lg-0 gap-2 gap-lg-0") {
                        li(classes = "nav-item") {
                            insertNavLink(PageType.HOME, pageType == PageType.HOME)
                        }
                        li(classes = "nav-item") {
                            insertNavLink(PageType.PLAY, pageType == PageType.PLAY)
                        }

                    }
                    ul("navbar-nav gap-2 gap-lg-0") {
                        // TODO Update this when authentication is added
                        if (true) {
                            li(classes = "nav-item") {
                                insertNavLink(PageType.LOGIN, pageType == PageType.LOGIN)
                            }
                            li(classes = "nav-item") {
                                insertNavLink(PageType.SIGN_UP, pageType == PageType.SIGN_UP)
                            }
                        } else {
                            li(classes = "nav-item") {
                                insertNavLink(PageType.PROFILE, pageType == PageType.PROFILE)
                            }
                            li(classes = "nav-item") {
                                div(classes = "logout") {
                                    a("", classes = "fa fa-sign-out nav-link link-effect navbar-link-contact ps-3") {
                                        +"Logout"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.insertNavLink(pageType: PageType, isActive: Boolean) {
    a {
        classes = setOf(
            "fa",
            pageType.iconClass,
            "nav-link",
            "link-effect",
            "navbar-link-" + pageType.toString().lowercase(),
            "ps-3"
        )
        if (isActive) {
            classes = classes.plus("active")
        }
        href = pageType.url
        +pageType.pageName
    }
}

fun initDB() {
    // Create tables if they do not exist.
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Users,
            Boards,
            UserBoards,
        )
        // Add two default users for testing
        createUser(
            username = "tracy",
            password = "12345",
            email = "tracyellis@email.com"
        )
    }
}


fun main() {
    embeddedServer(Netty, port = getEnvOrProperty("PORT").toInt(), host = getEnvOrProperty("HOST")) {
//        initDB()
        initModules()
    }.start(wait = true)
}

fun Application.initModules() {
    configureRouting()
    home()

}
