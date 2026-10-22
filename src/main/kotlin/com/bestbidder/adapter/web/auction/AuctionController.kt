package com.bestbidder.adapter.web.auction

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador REST que expone los endpoints realizando con las subastas incluyendo:
 * - creación
 * - borrado
 * - modificación.
 */

@RestController
@RequestMapping("/api/auction")
class AuctionController
