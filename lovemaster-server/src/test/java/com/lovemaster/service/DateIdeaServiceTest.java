package com.lovemaster.service;

import com.lovemaster.dto.request.CreatePreferenceRequest;
import com.lovemaster.dto.request.DateIdeaQueryRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.response.DateIdeaResponse;
import com.lovemaster.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DateIdeaServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private PreferenceService preferenceService;

    @Autowired
    private DateIdeaService dateIdeaService;

    @Test
    @DisplayName("可以按预算、耗时、场景和兴趣标签筛选约会灵感")
    void listShouldFilterByQuery() {
        DateIdeaQueryRequest request = new DateIdeaQueryRequest();
        request.setBudgetLevel("FREE");
        request.setDurationLevel("ONE_HOUR");
        request.setScene("OUTDOOR");
        request.setTags(List.of("散步"));

        List<DateIdeaResponse> ideas = dateIdeaService.list(request);

        assertFalse(ideas.isEmpty());
        assertTrue(ideas.stream().allMatch(item -> "FREE".equals(item.getBudgetLevel())));
        assertTrue(ideas.stream().allMatch(item -> "ONE_HOUR".equals(item.getDurationLevel())));
        assertTrue(ideas.stream().allMatch(item -> "OUTDOOR".equals(item.getScene())));
        assertTrue(ideas.stream().anyMatch(item -> "傍晚散步拍照".equals(item.getTitle())));
    }

    @Test
    @DisplayName("推荐约会灵感会优先返回命中用户喜好标签的内容")
    void recommendShouldPreferPreferenceTags() {
        User user = register("idea_user_recommend");
        CreatePreferenceRequest preferenceRequest = new CreatePreferenceRequest();
        preferenceRequest.setTarget("SELF");
        preferenceRequest.setCategory("HOBBY");
        preferenceRequest.setContent("喜欢散步和拍照");
        preferenceRequest.setVisibility("PRIVATE");
        preferenceRequest.setTags(List.of("散步", "拍照"));
        preferenceService.create(user.getId(), preferenceRequest);

        DateIdeaQueryRequest request = new DateIdeaQueryRequest();
        request.setScene("OUTDOOR");

        List<DateIdeaResponse> ideas = dateIdeaService.recommend(user.getId(), request);

        assertFalse(ideas.isEmpty());
        assertEquals("傍晚散步拍照", ideas.get(0).getTitle());
        assertTrue(ideas.get(0).getMatchScore() >= 2);
    }

    private User register(String username) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("password123");
        authService.register(request);
        return userService.findByUsername(username);
    }
}
