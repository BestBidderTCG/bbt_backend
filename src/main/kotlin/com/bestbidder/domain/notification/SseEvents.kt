package com.bestbidder.domain.notification

import com.bestbidder.domain.bid.Bid
import java.util.UUID

/**
 * Catálogo de eventos SSE. Concentra aquí los nombres de los campos del payload para que los casos
 * de uso no construyan mapas a mano (igual que [PushMessages] hace con los textos de las push).
 */
object SseEvents {
    fun auctionCreated(
        auctionId: UUID,
        creatorId: UUID,
    ) = SseEvent(
        type = SseEventType.AUCTION_CREATED,
        data = mapOf("auctionId" to auctionId, "creatorId" to creatorId),
    )

    fun auctionUpdated(auctionId: UUID) =
        SseEvent(
            type = SseEventType.AUCTION_UPDATED,
            data = mapOf("auctionId" to auctionId),
        )

    fun auctionStarted(auctionId: UUID) =
        SseEvent(
            type = SseEventType.AUCTION_STARTED,
            data = mapOf("auctionId" to auctionId),
        )

    /** [winningBid] es la puja más alta, o null si nadie pujó. */
    fun auctionEnded(
        auctionId: UUID,
        winningBid: Bid?,
    ) = SseEvent(
        type = SseEventType.AUCTION_ENDED,
        data =
            mapOf(
                "auctionId" to auctionId,
                "winnerUsername" to winningBid?.bidderUsername,
                "finalAmount" to winningBid?.amount,
            ),
    )

    fun bidPlaced(
        auctionId: UUID,
        amount: Double,
        bidderUsername: String,
    ) = SseEvent(
        type = SseEventType.BID_PLACED,
        data = mapOf("auctionId" to auctionId, "amount" to amount, "bidderUsername" to bidderUsername),
    )

    /**
     * Cambio en los seguidores de [userId] (el seguido). [followed] es true si [followerId] ha
     * empezado a seguirle y false si ha dejado de hacerlo.
     */
    fun followersChanged(
        userId: UUID,
        followerId: UUID,
        followed: Boolean,
        followersCount: Long,
    ) = SseEvent(
        type = SseEventType.FOLLOWERS_CHANGED,
        data =
            mapOf(
                "userId" to userId,
                "followerId" to followerId,
                "followed" to followed,
                "followersCount" to followersCount,
            ),
    )

    /** Nuevo saldo de un usuario al que le han devuelto una puja superada en [auctionId]. */
    fun balanceChanged(
        auctionId: UUID,
        balance: Double,
    ) = SseEvent(
        type = SseEventType.BALANCE_CHANGED,
        data = mapOf("auctionId" to auctionId, "balance" to balance),
    )
}
