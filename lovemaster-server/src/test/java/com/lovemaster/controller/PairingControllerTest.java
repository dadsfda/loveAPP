package com.lovemaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.response.PairingResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.PairingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PairingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PairingService pairingService;

    @Test
    @DisplayName("GET /api/v1/pairings/me - 成功")
    void getMyPairingShouldReturnSuccess() throws Exception {
        User principal = new User();
        principal.setId(1L);
        PairingResponse response = PairingResponse.unpaired("ABCDEFGH");
        when(pairingService.getMyPairing(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/pairings/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paired").value(false))
                .andExpect(jsonPath("$.data.pairCode").value("ABCDEFGH"));
    }

    @Test
    @DisplayName("POST /api/v1/pairings/bind - 成功")
    void bindShouldReturnSuccess() throws Exception {
        User principal = new User();
        principal.setId(1L);
        BindPairRequest request = new BindPairRequest();
        request.setPairCode("ABCDEFGH");
        PairingResponse response = new PairingResponse();
        response.setPaired(true);
        when(pairingService.bind(eq(1L), any(BindPairRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/pairings/bind")
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paired").value(true));
    }
}
