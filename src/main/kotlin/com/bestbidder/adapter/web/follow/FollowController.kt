package com.bestbidder.adapter.web.follow

import com.bestbidder.adapter.web.auction.dto.AuctionListItemResponse
import com.bestbidder.adapter.web.follow.dto.FollowListItemResponse
import com.bestbidder.adapter.web.follow.dto.FollowStatsResponse
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.follow.GetFollowUseCase
import com.bestbidder.usecase.follow.ManageFollowUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Endpoints del apartado social: seguir, seguidos, seguidores y subastas de seguidos.
 */
@RestController
@RequestMapping("/api/social")
class FollowController(
    private val manageFollowUseCase: ManageFollowUseCase,
    private val getFollowUseCase: GetFollowUseCase,
) {
    /**
     * Sigue a un usuario. Es idempotente: si ya lo seguías también devuelve 201.
     */
    @PostMapping("/{id}/follow")
    fun follow(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<Void> {
        manageFollowUseCase.follow(principal.id, id)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /**
     * Deja de seguir a un usuario. Es idempotente.
     */
    @DeleteMapping("/{id}/follow")
    fun unfollow(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<Void> {
        manageFollowUseCase.unfollow(principal.id, id)
        return ResponseEntity.noContent().build()
    }

    /**
     * Usuarios a los que sigues.
     */
    @GetMapping("/me/following")
    fun myFollowing(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<FollowListItemResponse>> {
        val users = getFollowUseCase.getFollowing(principal.id, principal.id, page, size)
        return ResponseEntity.ok(users.map { FollowListItemResponse.fromDomain(it) })
    }

    /**
     * Usuarios que te siguen.
     */
    @GetMapping("/me/followers")
    fun myFollowers(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<FollowListItemResponse>> {
        val users = getFollowUseCase.getFollowers(principal.id, principal.id, page, size)
        return ResponseEntity.ok(users.map { FollowListItemResponse.fromDomain(it) })
    }

    /**
     * Subastas activas o próximas de los usuarios que sigues.
     * Con `status` (UPCOMING o IN_PROGRESS) se filtra por estado.
     */
    @GetMapping("/me/following/auctions")
    fun followingAuctions(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestParam(required = false) status: AuctionStatus?,
    ): ResponseEntity<List<AuctionListItemResponse>> {
        val auctions = getFollowUseCase.getFollowingAuctions(principal.id, status)
        return ResponseEntity.ok(auctions.map { AuctionListItemResponse.fromDomain(it) })
    }

    /**
     * Usuarios a los que sigue otro usuario (perfil público).
     */
    @GetMapping("/{id}/following")
    fun followingOf(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<FollowListItemResponse>> {
        val users = getFollowUseCase.getFollowing(id, principal.id, page, size)
        return ResponseEntity.ok(users.map { FollowListItemResponse.fromDomain(it) })
    }

    /**
     * Seguidores de otro usuario (perfil público).
     */
    @GetMapping("/{id}/followers")
    fun followersOf(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<FollowListItemResponse>> {
        val users = getFollowUseCase.getFollowers(id, principal.id, page, size)
        return ResponseEntity.ok(users.map { FollowListItemResponse.fromDomain(it) })
    }

    /**
     * Contadores sociales de un perfil y si lo sigues.
     */
    @GetMapping("/{id}/follow-stats")
    fun stats(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<FollowStatsResponse> {
        val stats = getFollowUseCase.getStats(id, principal.id)
        return ResponseEntity.ok(FollowStatsResponse.fromDomain(stats))
    }
}
