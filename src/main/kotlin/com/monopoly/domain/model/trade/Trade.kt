package com.monopoly.domain.model.trade

/**
 * トレードの状態を表すsealed class
 *
 * トレードは以下の状態遷移を持つ:
 * Proposed -> Accepted -> Completed
 *          -> Rejected
 */
sealed class Trade {
    /**
     * 提案された状態
     *
     * @property offer トレード提案
     */
    data class Proposed(
        val offer: TradeOffer,
    ) : Trade() {
        /**
         * 提案を受け入れる
         */
        fun accept(): Accepted = Accepted(offer)

        /**
         * 提案を拒否する
         */
        fun reject(): Rejected = Rejected(offer)
    }

    /**
     * 受け入れられた状態
     *
     * @property offer トレード提案
     */
    data class Accepted(
        val offer: TradeOffer,
    ) : Trade() {
        /**
         * トレードを完了する
         */
        fun complete(): Completed = Completed(offer)
    }

    /**
     * 拒否された状態
     *
     * @property offer トレード提案
     */
    data class Rejected(
        val offer: TradeOffer,
    ) : Trade()

    /**
     * 完了した状態
     *
     * @property offer トレード提案
     */
    data class Completed(
        val offer: TradeOffer,
    ) : Trade()

    companion object {
        /**
         * 新しいトレードを提案する
         *
         * @param offer トレード提案
         * @return 提案状態のトレード
         */
        fun propose(offer: TradeOffer): Proposed = Proposed(offer)
    }
}
