package com.lovemaster.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OpenAPI JSON 可以匿名访问并包含主要业务接口")
    void openApiDocsShouldBePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/register")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/preferences")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/date-ideas")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/date-ideas/recommend")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/memories")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/memories/{id}")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/files/images")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/ai/communication-advice")));
    }
}
