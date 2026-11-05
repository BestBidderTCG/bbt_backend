package com.bestbidder.adapter.persistence.card

import com.bestbidder.adapter.persistence.user.UserEntity
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "card")
class CardEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: UserEntity,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val image: String,
    @Column(nullable = false)
    val category: String,
    @Column(nullable = true)
    val collection: String? = null,
    @Column(nullable = true)
    val num: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val state: CardState,
    @Column(nullable = true, length = 500)
    val description: String? = null,
) {
    fun toDomain(): Card =
        Card(
            id = id,
            ownerId = owner.id,
            name = name,
            image = image,
            category = category,
            collection = collection,
            num = num,
            state = state,
            description = description,
        )

    companion object {
        fun fromDomain(
            card: Card,
            ownerEntity: UserEntity,
        ): CardEntity =
            CardEntity(
                id = card.id,
                owner = ownerEntity,
                name = card.name,
                image = card.image,
                category = card.category,
                collection = card.collection?.ifBlank { null },
                num = if (card.num == 0) null else card.num,
                state = card.state,
                description = card.description?.ifBlank { null },
            )
    }
}
