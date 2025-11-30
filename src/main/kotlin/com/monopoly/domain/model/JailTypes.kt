package com.monopoly.domain.model

/**
 * 刑務所に送られる理由
 */
enum class JailReason {
    THREE_CONSECUTIVE_DOUBLES, // 3回連続ゾロ目
    GO_TO_JAIL_SPACE,          // "Go to Jail" マスに止まった
    CARD_EFFECT,               // チャンス/共同基金カードの効果
}

/**
 * 刑務所から脱出する方法
 */
enum class JailEscapeMethod {
    DOUBLES, // ゾロ目を出した
    PAYMENT, // $50支払った
    CARD,    // "Get Out of Jail Free" カードを使用
    FORCED,  // 3ターン経過後の強制支払い
}
