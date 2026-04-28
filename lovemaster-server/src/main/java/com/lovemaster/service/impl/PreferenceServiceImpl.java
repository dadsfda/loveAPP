package com.lovemaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lovemaster.dto.request.CreatePreferenceRequest;
import com.lovemaster.dto.request.UpdatePreferenceRequest;
import com.lovemaster.dto.response.PreferenceResponse;
import com.lovemaster.entity.Couple;
import com.lovemaster.entity.Preference;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.mapper.CoupleMapper;
import com.lovemaster.mapper.PreferenceMapper;
import com.lovemaster.service.PreferenceService;
import com.lovemaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenceServiceImpl implements PreferenceService {

    private static final Set<String> TARGETS = Set.of("SELF", "PARTNER_OBSERVED");
    private static final Set<String> CATEGORIES = Set.of(
            "FOOD_TABOO", "FAVORITE_FOOD", "HOBBY", "GIFT", "LIFE_BOUNDARY", "CUSTOM"
    );
    private static final Set<String> VISIBILITIES = Set.of("PRIVATE", "COUPLE");

    private final PreferenceMapper preferenceMapper;
    private final CoupleMapper coupleMapper;
    private final UserService userService;

    @Override
    @Transactional
    public PreferenceResponse create(Long userId, CreatePreferenceRequest request) {
        User user = requireUser(userId);
        validateRequest(request.getTarget(), request.getCategory(), request.getVisibility());

        Preference preference = new Preference();
        preference.setCreatorId(userId);
        preference.setCoupleId("COUPLE".equals(request.getVisibility()) ? requireCoupleId(user) : null);
        preference.setTarget(request.getTarget());
        preference.setCategory(request.getCategory());
        preference.setContent(request.getContent());
        preference.setVisibility(request.getVisibility());
        preference.setTags(formatTags(request.getTags()));
        preference.setRemark(request.getRemark());
        preferenceMapper.insert(preference);
        return PreferenceResponse.fromEntity(preference);
    }

    @Override
    public List<PreferenceResponse> list(Long userId, String category, String visibility) {
        User user = requireUser(userId);
        validateFilters(category, visibility);

        Map<Long, Preference> preferences = new LinkedHashMap<>();
        if (visibility == null || "PRIVATE".equals(visibility)) {
            selectOwnPreferences(userId, category, visibility).forEach(item -> preferences.put(item.getId(), item));
        }

        Couple couple = findActiveCouple(user);
        if ((visibility == null || "COUPLE".equals(visibility)) && couple != null) {
            selectCouplePreferences(couple.getId(), category).forEach(item -> preferences.put(item.getId(), item));
        }

        return preferences.values().stream()
                .sorted(Comparator.comparing(Preference::getCreatedAt).thenComparing(Preference::getId))
                .map(PreferenceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PreferenceResponse get(Long userId, Long preferenceId) {
        return PreferenceResponse.fromEntity(requireVisiblePreference(userId, preferenceId));
    }

    @Override
    @Transactional
    public PreferenceResponse update(Long userId, Long preferenceId, UpdatePreferenceRequest request) {
        User user = requireUser(userId);
        Preference preference = requireCreatedPreference(userId, preferenceId);
        validateRequest(request.getTarget(), request.getCategory(), request.getVisibility());

        preference.setCoupleId("COUPLE".equals(request.getVisibility()) ? requireCoupleId(user) : null);
        preference.setTarget(request.getTarget());
        preference.setCategory(request.getCategory());
        preference.setContent(request.getContent());
        preference.setVisibility(request.getVisibility());
        preference.setTags(formatTags(request.getTags()));
        preference.setRemark(request.getRemark());
        preferenceMapper.updateById(preference);
        return PreferenceResponse.fromEntity(preferenceMapper.selectById(preference.getId()));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long preferenceId) {
        requireCreatedPreference(userId, preferenceId);
        preferenceMapper.deleteById(preferenceId);
    }

    private User requireUser(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void validateRequest(String target, String category, String visibility) {
        if (!TARGETS.contains(target)) {
            throw new BusinessException(ErrorCode.PREFERENCE_TARGET_INVALID);
        }
        if (!CATEGORIES.contains(category)) {
            throw new BusinessException(ErrorCode.PREFERENCE_CATEGORY_INVALID);
        }
        if (!VISIBILITIES.contains(visibility)) {
            throw new BusinessException(ErrorCode.PREFERENCE_VISIBILITY_INVALID);
        }
    }

    private void validateFilters(String category, String visibility) {
        if (category != null && !CATEGORIES.contains(category)) {
            throw new BusinessException(ErrorCode.PREFERENCE_CATEGORY_INVALID);
        }
        if (visibility != null && !VISIBILITIES.contains(visibility)) {
            throw new BusinessException(ErrorCode.PREFERENCE_VISIBILITY_INVALID);
        }
    }

    private List<Preference> selectOwnPreferences(Long userId, String category, String visibility) {
        LambdaQueryWrapper<Preference> wrapper = new LambdaQueryWrapper<Preference>()
                .eq(Preference::getCreatorId, userId);
        applyFilters(wrapper, category, visibility);
        return preferenceMapper.selectList(wrapper);
    }

    private List<Preference> selectCouplePreferences(Long coupleId, String category) {
        LambdaQueryWrapper<Preference> wrapper = new LambdaQueryWrapper<Preference>()
                .eq(Preference::getCoupleId, coupleId)
                .eq(Preference::getVisibility, "COUPLE");
        applyFilters(wrapper, category, null);
        return preferenceMapper.selectList(wrapper);
    }

    private void applyFilters(LambdaQueryWrapper<Preference> wrapper, String category, String visibility) {
        if (category != null) {
            wrapper.eq(Preference::getCategory, category);
        }
        if (visibility != null) {
            wrapper.eq(Preference::getVisibility, visibility);
        }
    }

    private Preference requireVisiblePreference(Long userId, Long preferenceId) {
        User user = requireUser(userId);
        Preference preference = preferenceMapper.selectById(preferenceId);
        if (preference == null) {
            throw new BusinessException(ErrorCode.PREFERENCE_NOT_FOUND);
        }
        if (preference.getCreatorId().equals(userId)) {
            return preference;
        }

        Couple couple = findActiveCouple(user);
        boolean coupleVisible = couple != null
                && "COUPLE".equals(preference.getVisibility())
                && couple.getId().equals(preference.getCoupleId());
        if (!coupleVisible) {
            throw new BusinessException(ErrorCode.PREFERENCE_ACCESS_DENIED);
        }
        return preference;
    }

    private Preference requireCreatedPreference(Long userId, Long preferenceId) {
        Preference preference = preferenceMapper.selectById(preferenceId);
        if (preference == null) {
            throw new BusinessException(ErrorCode.PREFERENCE_NOT_FOUND);
        }
        if (!preference.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.PREFERENCE_ACCESS_DENIED);
        }
        return preference;
    }

    private Long requireCoupleId(User user) {
        Couple couple = findActiveCouple(user);
        if (couple == null) {
            throw new BusinessException(ErrorCode.PAIRING_NOT_FOUND);
        }
        return couple.getId();
    }

    private Couple findActiveCouple(User user) {
        if (user.getPartnerId() == null) {
            return null;
        }
        return coupleMapper.selectOne(new LambdaQueryWrapper<Couple>()
                .eq(Couple::getStatus, 1)
                .and(wrapper -> wrapper
                        .eq(Couple::getUserId1, user.getId())
                        .or()
                        .eq(Couple::getUserId2, user.getId())));
    }

    private String formatTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
    }
}
