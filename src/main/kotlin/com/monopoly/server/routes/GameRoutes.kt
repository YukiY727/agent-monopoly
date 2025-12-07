package com.monopoly.server.routes

import com.monopoly.server.GameSession
import com.monopoly.server.dto.toDto
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * ゲーム関連のルート定義
 */
fun Route.gameRoutes() {
    // 新しいゲームを開始
    post("/api/game/new") {
        val gameState = GameSession.startNewGame()
        call.respond(gameState.toDto())
    }

    // 次のターンを実行
    post("/api/game/turn") {
        val gameState = GameSession.executeTurn()
        call.respond(gameState.toDto())
    }

    // 現在のゲーム状態を取得
    get("/api/game/state") {
        if (!GameSession.hasActiveGame()) {
            call.respond(mapOf("error" to "No active game"))
            return@get
        }
        val gameState = GameSession.getCurrentState()
        call.respond(gameState.toDto())
    }
}
