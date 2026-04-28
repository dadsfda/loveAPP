package com.lovemaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.UpdateAnniversaryRequest;
import com.lovemaster.dto.response.AnniversaryResponse;
import com.lovemaster.entity.Anniversary;
import com.lovemaster.entity.Couple;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.mapper.AnniversaryMapper;
import com.lovemaster.mapper.CoupleMapper;
import com.lovemaster.service.AnniversaryService;
import com.lovemaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnniversaryServiceImpl implements AnniversaryService {

    private static final Set<String> TYPES = Set.of("LOVE_ANNIVERSARY", "BIRTHDAY", "CUSTOM");
    private static final Set<String> VISIBILITIES = Set.of("PRIVATE", "COUPLE");
    private static final Set<Integer> ALLOWED_REMIND_DAYS = Set.of(0, 1, 3, 7);

    private final AnniversaryMapper anniversaryMapper;
    private final CoupleMapper coupleMapper;
    private final UserService userService;

    @Override
    @Transactional
    public AnniversaryResponse create(Long userId, CreateAnniversaryRequest request) {
        User user = requireUser(userId);
        validateRequest(request.getType(), request.getVisibility(), request.getRemindDays(), request.getSurpriseMode());

        Anniversary anniversary = new Anniversary();
        anniversary.setCreatorId(userId);
        anniversary.setCoupleId("COUPLE".equals(request.getVisibility()) ? requireCoupleId(user) : null);
        anniversary.setTitle(request.getTitle());
        anniversary.setDate(request.getDate());
        anniversary.setType(request.getType());
        anniversary.setVisibility(request.getVisibility());
        anniversary.setRemindDays(formatRemindDays(request.getRemindDays()));
        anniversary.setSurpriseMode(Boolean.TRUE.equals(request.getSurpriseMode()));
        anniversary.setRemark(request.getRemark());
        anniversaryMapper.insert(anniversary);
        return AnniversaryResponse.fromEntity(anniversary);
    }

    @Override
    public List<AnniversaryResponse> list(Long userId, String type, String visibility) {
        User user = requireUser(userId);
        validateFilters(type, visibility);

        Map<Long, Anniversary> anniversaries = new LinkedHashMap<>();
        if (visibility == null || "PRIVATE".equals(visibility)) {
            selectOwnAnniversaries(userId, type, visibility).forEach(item -> anniversaries.put(item.getId(), item));
        }

        Couple couple = findActiveCouple(user);
        if ((visibility == null || "COUPLE".equals(visibility)) && couple != null) {
            selectCoupleAnniversaries(couple.getId(), type).forEach(item -> anniversaries.put(item.getId(), item));
        }

        return anniversaries.values().stream()
                .sorted(Comparator.comparing(Anniversary::getDate).thenComparing(Anniversary::getId))
                .map(AnniversaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AnniversaryResponse get(Long userId, Long anniversaryId) {
        return AnniversaryResponse.fromEntity(requireVisibleAnniversary(userId, anniversaryId));
    }

    @Override
    @Transactional
    public AnniversaryResponse update(Long userId, Long anniversaryId, UpdateAnniversaryRequest request) {
        User user = requireUser(userId);
        Anniversary anniversary = requireCreatedAnniversary(userId, anniversaryId);
        validateRequest(request.getType(), request.getVisibility(), request.getRemindDays(), request.getSurpriseMode());

        anniversary.setTitle(request.getTitle());
        anniversary.setDate(request.getDate());
        anniversary.setType(request.getType());
        anniversary.setVisibility(request.getVisibility());
        anniversary.setCoupleId("COUPLE".equals(request.getVisibility()) ? requireCoupleId(user) : null);
        anniversary.setRemindDays(formatRemindDays(request.getRemindDays()));
        anniversary.setSurpriseMode(Boolean.TRUE.equals(request.getSurpriseMode()));
        anniversary.setRemark(request.getRemark());
        anniversaryMapper.updateById(anniversary);
        return AnniversaryResponse.fromEntity(anniversaryMapper.selectById(anniversary.getId()));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long anniversaryId) {
        requireCreatedAnniversary(userId, anniversaryId);
        anniversaryMapper.deleteById(anniversaryId);
    }

    @Override
    public List<AnniversaryResponse> reminders(Long userId, Integer days) {
        LocalDate today = LocalDate.now();
        return list(userId, null, null).stream()
                .filter(item -> !item.getDate().isBefore(today))
                .filter(item -> {
                    long diff = ChronoUnit.DAYS.between(today, item.getDate());
                    return days == null
                            ? item.getRemindDays().contains((int) diff)
                            : diff == days && item.getRemindDays().contains(days);
                })
                .sorted(Comparator.comparing(AnniversaryResponse::getDate).thenComparing(AnniversaryResponse::getId))
                .collect(Collectors.toList());
    }

    private User requireUser(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
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

    private List<Anniversary> selectOwnAnniversaries(Long userId, String type, String visibility) {
        LambdaQueryWrapper<Anniversary> wrapper = new LambdaQueryWrapper<Anniversary>()
                .eq(Anniversary::getCreatorId, userId);
        applyFilters(wrapper, type, visibility);
        return anniversaryMapper.selectList(wrapper);
    }

    private List<Anniversary> selectCoupleAnniversaries(Long coupleId, String type) {
        LambdaQueryWrapper<Anniversary> wrapper = new LambdaQueryWrapper<Anniversary>()
                .eq(Anniversary::getCoupleId, coupleId)
                .eq(Anniversary::getVisibility, "COUPLE");
        applyFilters(wrapper, type, null);
        return anniversaryMapper.selectList(wrapper);
    }

    private Anniversary requireVisibleAnniversary(Long userId, Long anniversaryId) {
        User user = requireUser(userId);
        Anniversary anniversary = anniversaryMapper.selectById(anniversaryId);
        if (anniversary == null) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_NOT_FOUND);
        }
        if (anniversary.getCreatorId().equals(userId)) {
            return anniversary;
        }

        Couple couple = findActiveCouple(user);
        boolean coupleVisible = couple != null
                && "COUPLE".equals(anniversary.getVisibility())
                && couple.getId().equals(anniversary.getCoupleId());
        if (!coupleVisible) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_ACCESS_DENIED);
        }
        return anniversary;
    }

    private Anniversary requireCreatedAnniversary(Long userId, Long anniversaryId) {
        Anniversary anniversary = anniversaryMapper.selectById(anniversaryId);
        if (anniversary == null) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_NOT_FOUND);
        }
        if (!anniversary.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_ACCESS_DENIED);
        }
        return anniversary;
    }

    private void validateFilters(String type, String visibility) {
        if (type != null && !TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_TYPE_INVALID);
        }
        if (visibility != null && !VISIBILITIES.contains(visibility)) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_VISIBILITY_INVALID);
        }
    }

    private void applyFilters(LambdaQueryWrapper<Anniversary> wrapper, String type, String visibility) {
        if (type != null) {
            wrapper.eq(Anniversary::getType, type);
        }
        if (visibility != null) {
            wrapper.eq(Anniversary::getVisibility, visibility);
        }
    }

    private void validateRequest(String type, String visibility, List<Integer> remindDays, Boolean surpriseMode) {
        if (!TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_TYPE_INVALID);
        }
        if (!VISIBILITIES.contains(visibility)) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_VISIBILITY_INVALID);
        }
        if (Boolean.TRUE.equals(surpriseMode) && "COUPLE".equals(visibility)) {
            throw new BusinessException(ErrorCode.SURPRISE_MODE_VISIBILITY_CONFLICT);
        }
        if (remindDays != null && !ALLOWED_REMIND_DAYS.containsAll(remindDays)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "提醒天数只能是0、1、3、7");
        }
    }

    private String formatRemindDays(List<Integer> remindDays) {
        if (remindDays == null || remindDays.isEmpty()) {
            return null;
        }
        return new ArrayList<>(remindDays).stream()
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
