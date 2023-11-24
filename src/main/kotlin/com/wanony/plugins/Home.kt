package com.wanony.plugins

import com.wanony.LayoutTemplate
import com.wanony.PageType
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.FlowContent

fun Application.home() {
    routing {
        get("/") {
            call.respondHtmlTemplate(LayoutTemplate(call, PageType.HOME)) {
                content {
                    insertHomePage()
                }
            }
        }
    }
}

fun FlowContent.insertHomePage() {
    div {
        h1 {
            +"Welcome to My Sudoku App!"
        }
        p {
            +"Enjoy playing Sudoku and have fun."
        }
        a(href = "/play") {
            +"Start Playing"
        }
    }
}
