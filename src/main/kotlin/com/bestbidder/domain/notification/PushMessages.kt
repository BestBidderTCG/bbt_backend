package com.bestbidder.domain.notification

import com.bestbidder.domain.bid.Bid
import java.util.Locale
import java.util.UUID

/**
 * Catálogo de mensajes push. Concentra aquí todos los textos para que los casos de uso
 * no construyan strings y sea fácil cambiarlos en un único sitio.
 */
@Suppress("TooManyFunctions")
object PushMessages {
    private val SPANISH = Locale.forLanguageTag("es-ES")

    private fun money(amount: Double): String = String.format(SPANISH, "%.2f €", amount)

    fun newFollower(
        followerId: UUID,
        followerUsername: String,
    ) = PushMessage(
        type = NotificationType.NEW_FOLLOWER,
        title = "Nuevo seguidor",
        body = "$followerUsername ha empezado a seguirte",
        data = mapOf("userId" to followerId.toString()),
    )

    fun followeeCreatedAuction(
        auctionId: UUID,
        creatorUsername: String,
        cardName: String,
    ) = PushMessage(
        type = NotificationType.FOLLOWEE_CREATED_AUCTION,
        title = "Nueva subasta de $creatorUsername",
        body = "$creatorUsername ha creado una subasta de $cardName",
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun favoriteStarted(
        auctionId: UUID,
        cardName: String,
    ) = PushMessage(
        type = NotificationType.FAVORITE_STARTED,
        title = "Ha empezado una subasta que guardaste",
        body = "Ya puedes pujar por $cardName",
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun favoriteEnded(
        auctionId: UUID,
        cardName: String,
    ) = PushMessage(
        type = NotificationType.FAVORITE_ENDED,
        title = "Ha terminado una subasta que guardaste",
        body = "La subasta de $cardName ha finalizado",
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun favoriteNewBid(
        auctionId: UUID,
        cardName: String,
        amount: Double,
    ) = PushMessage(
        type = NotificationType.FAVORITE_NEW_BID,
        title = "Nueva puja en una subasta que guardaste",
        body = "Alguien ha pujado ${money(amount)} por $cardName",
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun outbid(
        auctionId: UUID,
        cardName: String,
        newAmount: Double,
    ) = PushMessage(
        type = NotificationType.OUTBID,
        title = "Han superado tu puja",
        body = "Alguien ha pujado ${money(newAmount)} por $cardName",
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun auctionWon(
        auctionId: UUID,
        cardName: String,
        amount: Double,
    ) = PushMessage(
        type = NotificationType.AUCTION_WON,
        title = "¡Has ganado la subasta!",
        body = "Te has llevado $cardName por ${money(amount)}",
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun ownAuctionStarted(
        auctionId: UUID,
        cardName: String,
    ) = PushMessage(
        type = NotificationType.OWN_AUCTION_STARTED,
        title = "Tu subasta ha empezado",
        body = "La subasta de $cardName ya está activa",
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun ownAuctionEnded(
        auctionId: UUID,
        cardName: String,
        winningBid: Bid?,
    ) = PushMessage(
        type = NotificationType.OWN_AUCTION_ENDED,
        title = "Tu subasta ha terminado",
        body =
            if (winningBid != null) {
                "${winningBid.bidderUsername} ha ganado $cardName con una puja de ${money(winningBid.amount)}"
            } else {
                "La subasta de $cardName ha terminado sin pujas"
            },
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun ownAuctionNewBid(
        auctionId: UUID,
        cardName: String,
        bidderUsername: String,
        amount: Double,
    ) = PushMessage(
        type = NotificationType.OWN_AUCTION_NEW_BID,
        title = "Nueva puja en tu subasta",
        body = "$bidderUsername ha pujado ${money(amount)} por $cardName",
        data = mapOf("auctionId" to auctionId.toString()),
    )

    fun test() =
        PushMessage(
            type = NotificationType.TEST,
            title = "Notificación de prueba",
            body = "Si ves esto, FCM funciona",
        )
}
