package com.bestbidder.domain.user.exceptions

class AccountDeletionBlockedException(val blockers: List<String>) :
    RuntimeException("No puedes eliminar tu cuenta ahora mismo. Debido a: ${blockers.joinToString("; ")}")
