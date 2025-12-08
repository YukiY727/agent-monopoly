package com.monopoly.domain.model.game

import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.Property

/**
 * 入札情報を表すデータクラス
 *
 * @property bidder 入札者
 * @property amount 入札額
 */
data class Bid(
    val bidder: Player,
    val amount: Int,
) {
    init {
        require(amount >= MIN_BID) { "Bid amount must be at least $MIN_BID" }
    }

    companion object {
        /** 最低入札額 */
        const val MIN_BID: Int = 1
    }
}

/**
 * オークションを管理するsealed class
 *
 * モノポリーのオークションルール：
 * - プレイヤーがプロパティを購入しない場合、オークションが開始される
 * - 全プレイヤーが入札できる（購入を拒否したプレイヤーも含む）
 * - 最低入札額は$1
 * - プレイヤーは順番に入札するか、パスする
 * - 1人を除いて全員がパスした場合、そのプレイヤーが落札
 * - 誰も入札せずに全員がパスした場合、オークション不成立
 */
sealed class Auction {
    /**
     * 入札処理
     *
     * @param player 入札するプレイヤー
     * @param amount 入札額
     * @return 更新されたオークション状態
     */
    abstract fun placeBid(
        player: Player,
        amount: Int,
    ): Auction

    /**
     * パス処理
     *
     * @param player パスするプレイヤー
     * @return 更新されたオークション状態
     */
    abstract fun pass(player: Player): Auction

    /**
     * 進行中のオークション
     *
     * @property property オークション対象のプロパティ
     * @property eligiblePlayers オークションに参加できるプレイヤーリスト
     * @property currentBid 現在の最高入札（null = まだ誰も入札していない）
     * @property passedPlayers パスしたプレイヤーのセット
     */
    data class InProgress(
        val property: Property,
        val eligiblePlayers: List<Player>,
        val currentBid: Bid? = null,
        val passedPlayers: Set<Player> = emptySet(),
    ) : Auction() {
        /**
         * 入札処理
         *
         * @param player 入札するプレイヤー
         * @param amount 入札額
         * @return 更新されたオークション状態
         * @throws IllegalArgumentException 無効な入札の場合
         */
        override fun placeBid(
            player: Player,
            amount: Int,
        ): Auction {
            // バリデーション
            require(player in eligiblePlayers) { "Player is not eligible for this auction" }
            require(player !in passedPlayers) { "Player has already passed" }
            require(amount >= Bid.MIN_BID) { "Bid must be at least ${Bid.MIN_BID}" }
            require(amount <= player.money) { "Player does not have enough money" }

            currentBid?.let { current ->
                require(amount > current.amount) { "Bid must be higher than current bid of ${current.amount}" }
            }

            val newBid: Bid = Bid(player, amount)
            val updatedAuction: InProgress = copy(currentBid = newBid)

            // オークション完了チェック
            return updatedAuction.checkCompletion()
        }

        /**
         * パス処理
         *
         * @param player パスするプレイヤー
         * @return 更新されたオークション状態
         * @throws IllegalArgumentException プレイヤーが既にパスしている場合
         */
        override fun pass(player: Player): Auction {
            require(player in eligiblePlayers) { "Player is not eligible for this auction" }
            require(player !in passedPlayers) { "Player has already passed" }

            val updatedPassedPlayers: Set<Player> = passedPlayers + player
            val updatedAuction: InProgress = copy(passedPlayers = updatedPassedPlayers)

            // オークション完了チェック
            return updatedAuction.checkCompletion()
        }

        /**
         * オークションが完了したかチェックし、完了していればCompletedに遷移
         *
         * 完了条件：
         * 1. 1人を除いて全員がパス → その1人が落札
         * 2. 全員がパス → オークション不成立
         */
        private fun checkCompletion(): Auction {
            val activePlayers: List<Player> = eligiblePlayers.filter { it !in passedPlayers }

            return when {
                // 全員がパスした場合
                activePlayers.isEmpty() -> {
                    if (currentBid != null) {
                        // 入札があった場合、最後の入札者が落札
                        Completed(property, currentBid.bidder, currentBid.amount)
                    } else {
                        // 誰も入札しなかった場合、オークション不成立
                        Completed(property, null, 0)
                    }
                }
                // 1人だけ残っている場合
                activePlayers.size == 1 -> {
                    if (currentBid != null && currentBid.bidder == activePlayers[0]) {
                        // 残っているプレイヤーが最後の入札者なら落札
                        Completed(property, currentBid.bidder, currentBid.amount)
                    } else {
                        // まだオークション継続
                        this
                    }
                }
                // 複数人が残っている場合、オークション継続
                else -> this
            }
        }
    }

    /**
     * 完了したオークション
     *
     * @property property オークション対象のプロパティ
     * @property winner 落札者（null = オークション不成立）
     * @property winningBid 落札額
     */
    data class Completed(
        val property: Property,
        val winner: Player?,
        val winningBid: Int,
    ) : Auction() {
        override fun placeBid(
            player: Player,
            amount: Int,
        ): Auction {
            throw IllegalStateException("Auction has already completed")
        }

        override fun pass(player: Player): Auction {
            throw IllegalStateException("Auction has already completed")
        }
    }

    /**
     * オークションを開始する
     *
     * @param property オークション対象のプロパティ
     * @param players オークションに参加するプレイヤーリスト
     * @return 開始されたオークション
     */
    companion object {
        fun start(
            property: Property,
            players: List<Player>,
        ): Auction {
            require(players.isNotEmpty()) { "At least one player is required for auction" }
            return InProgress(property, players)
        }
    }
}
