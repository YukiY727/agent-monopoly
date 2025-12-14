package com.monopoly.domain.model.trade

import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.Property

/**
 * トレード提案を表すデータクラス
 *
 * 提案者（proposer）がターゲット（target）に対して、
 * アイテムの交換を提案する。
 *
 * @property proposer 提案者
 * @property target 提案を受ける相手
 * @property offeredProperties 提案者が提供する物件リスト
 * @property offeredMoney 提案者が提供する金額
 * @property offeredJailCards 提案者が提供する刑務所脱出カード数
 * @property requestedProperties 提案者が要求する物件リスト
 * @property requestedMoney 提案者が要求する金額
 * @property requestedJailCards 提案者が要求する刑務所脱出カード数
 */
data class TradeOffer(
    val proposer: Player,
    val target: Player,
    val offeredProperties: List<Property> = emptyList(),
    val offeredMoney: Int = 0,
    val offeredJailCards: Int = 0,
    val requestedProperties: List<Property> = emptyList(),
    val requestedMoney: Int = 0,
    val requestedJailCards: Int = 0,
) {
    init {
        require(proposer != target) { "Cannot trade with yourself" }
        require(offeredMoney >= 0) { "Offered money cannot be negative" }
        require(requestedMoney >= 0) { "Requested money cannot be negative" }
        require(offeredJailCards >= 0) { "Offered jail cards cannot be negative" }
        require(requestedJailCards >= 0) { "Requested jail cards cannot be negative" }
        require(isNotEmpty()) { "Trade offer must contain at least one item" }
    }

    /**
     * トレード提案が空でないことを確認
     * 少なくとも1つのアイテムが含まれている必要がある
     */
    private fun isNotEmpty(): Boolean =
        offeredProperties.isNotEmpty() ||
            offeredMoney > 0 ||
            offeredJailCards > 0 ||
            requestedProperties.isNotEmpty() ||
            requestedMoney > 0 ||
            requestedJailCards > 0

    /**
     * 提案者にとっての純現金フロー（負 = 支払い、正 = 受け取り）
     */
    val netMoneyForProposer: Int
        get() = requestedMoney - offeredMoney

    /**
     * ターゲットにとっての純現金フロー（負 = 支払い、正 = 受け取り）
     */
    val netMoneyForTarget: Int
        get() = offeredMoney - requestedMoney
}
