package com.monopoly.domain.model.jail

/**
 * 刑務所の状態を表すenum
 */
sealed class JailStatus {
    /**
     * 自由（刑務所にいない）
     */
    object Free : JailStatus()
    
    /**
     * 刑務所マスを訪問（収監されていない）
     */
    object Visiting : JailStatus()
    
    /**
     * 収監中
     */
    object Jailed : JailStatus()
}
