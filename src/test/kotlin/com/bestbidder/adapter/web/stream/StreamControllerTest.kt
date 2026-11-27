package com.bestbidder.adapter.web.stream

import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.infrastructure.notification.SseEventAdapter
import com.bestbidder.infrastructure.security.AuthenticatedUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@WebMvcTest(StreamController::class)
@Import(GlobalExceptionHandler::class)
class StreamControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var sseEventAdapter: SseEventAdapter

    private val userId = UUID.randomUUID()

    @Test
    @DisplayName("GET /api/stream abre el stream SSE para el usuario autenticado")
    fun `should subscribe the authenticated user`() {
        val principal = AuthenticatedUser(id = userId, username = "paula")
        val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
        `when`(sseEventAdapter.subscribe(userId)).thenReturn(SseEmitter())

        mockMvc.perform(get("/api/stream").accept(MediaType.TEXT_EVENT_STREAM))
            .andExpect(request().asyncStarted())

        verify(sseEventAdapter, times(1)).subscribe(userId)
    }
}
