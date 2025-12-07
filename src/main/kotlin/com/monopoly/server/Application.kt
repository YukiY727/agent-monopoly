package com.monopoly.server

import com.monopoly.server.routes.gameRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // CORS設定（フロントエンドからのアクセスを許可）
    install(CORS) {
        anyHost()
    }

    // Content Negotiation（JSON）
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
            },
        )
    }

    routing {
        // 静的ファイルの提供
        staticResources("/", "static")

        get("/health") {
            call.respondText("OK")
        }

        // ゲーム関連のルート
        gameRoutes()
    }
}
