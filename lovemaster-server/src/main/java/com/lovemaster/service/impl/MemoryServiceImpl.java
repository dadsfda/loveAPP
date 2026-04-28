package com.lovemaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lovemaster.dto.request.CreateMemoryRequest;
import com.lovemaster.dto.request.UpdateMemoryRequest;
import com.lovemaster.dto.response.MemoryResponse;
import com.lovemaster.entity.Couple;
import com.lovemaster.entity.Memory;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.mapper.CoupleMapper;
import com.lovemaster.mapper.MemoryMapper;
import com.lovemaster.service.MemoryService;
import com.lovemaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    private static final Set<String> VISIBILITIES = Set.of("PRIVATE", "COUPLE");

    private final MemoryMapper memoryMapper;
    private final CoupleMapper coupleMapper;
    private final UserService userService;

    @Override
    @Transactional
    public MemoryResponse create(Long userId, CreateMemoryRequest request) {
        User user = requireUser(userId);
        validateVisibility(request.getVisibility());

        Memory memory = new Memory();
        memory.setCreatorId(userId);
        memory.setCoupleId("COUPLE".equals(request.getVisibility()) ? requireCoupleId(user) : null);
        fill(memory, request.getTitle(), request.getMemoryDate(), request.getLocation(), request.getContent(),
                request.getImageUrl(), request.getVisibility(), request.getTags(), request.getRemark());
        memoryMapper.insert(memory);
        return MemoryResponse.fromEntity(memory);
    }

    @Override
    public List<MemoryResponse> list(Long userId, String visibility, LocalDate startDate, LocalDate endDate, String tag) {
        User user = requireUser(userId);
        validateFilterVisibility(visibility);

        Map<Long, Memory> memories = new LinkedHashMap<>();
        if (visibility == null || "PRIVATE".equals(visibility)) {
            selectOwnMemories(userId, visibility, startDate, endDate, tag).forEach(item -> memories.put(item.getId(), item));
        }

        Couple couple = findActiveCouple(user);
        if ((visibility == null || "COUPLE".equals(visibility)) && couple != null) {
            selectCoupleMemories(couple.getId(), startDate, endDate, tag).forEach(item -> memories.put(item.getId(), item));
        }

        return memories.values().stream()
                .sorted(Comparator.comparing(Memory::getMemoryDate).reversed()
                        .thenComparing(Memory::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Memory::getId, Comparator.reverseOrder()))
                .map(MemoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public MemoryResponse get(Long userId, Long memoryId) {
        return MemoryResponse.fromEntity(requireVisibleMemory(userId, memoryId));
    }

    @Override
    @Transactional
    public MemoryResponse update(Long userId, Long memoryId, UpdateMemoryRequest request) {
        User user = requireUser(userId);
        Memory memory = requireCreatedMemory(userId, memoryId);
        validateVisibility(request.getVisibility());

        Long coupleId = "COUPLE".equals(request.getVisibility()) ? requireCoupleId(user) : null;
        fill(memory, request.getTitle(), request.getMemoryDate(), request.getLocation(), request.getContent(),
                request.getImageUrl(), request.getVisibility(), request.getTags(), request.getRemark());
        memory.setCoupleId(coupleId);

        memoryMapper.update(null, new LambdaUpdateWrapper<Memory>()
                .eq(Memory::getId, memory.getId())
                .set(Memory::getCoupleId, coupleId)
                .set(Memory::getTitle, memory.getTitle())
                .set(Memory::getMemoryDate, memory.getMemoryDate())
                .set(Memory::getLocation, memory.getLocation())
                .set(Memory::getContent, memory.getContent())
                .set(Memory::getImageUrl, memory.getImageUrl())
                .set(Memory::getVisibility, memory.getVisibility())
                .set(Memory::getTags, memory.getTags())
                .set(Memory::getRemark, memory.getRemark()));
        return MemoryResponse.fromEntity(memoryMapper.selectById(memory.getId()));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long memoryId) {
        requireCreatedMemory(userId, memoryId);
        memoryMapper.deleteById(memoryId);
    }

    private void fill(Memory memory, String title, LocalDate memoryDate, String location, String content,
                      String imageUrl, String visibility, List<String> tags, String remark) {
        memory.setTitle(title);
        memory.setMemoryDate(memoryDate);
        memory.setLocation(location);
        memory.setContent(content);
        memory.setImageUrl(imageUrl);
        memory.setVisibility(visibility);
        memory.setTags(formatTags(tags));
        memory.setRemark(remark);
    }

    private User requireUser(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void validateVisibility(String visibility) {
        if (!VISIBILITIES.contains(visibility)) {
            throw new BusinessException(ErrorCode.MEMORY_VISIBILITY_INVALID);
        }
    }

    private void validateFilterVisibility(String visibility) {
        if (visibility != null && !VISIBILITIES.contains(visibility)) {
            throw new BusinessException(ErrorCode.MEMORY_VISIBILITY_INVALID);
        }
    }

    private List<Memory> selectOwnMemories(Long userId, String visibility, LocalDate startDate, LocalDate endDate, String tag) {
        LambdaQueryWrapper<Memory> wrapper = new LambdaQueryWrapper<Memory>()
                .eq(Memory::getCreatorId, userId);
        applyFilters(wrapper, visibility, startDate, endDate, tag);
        return memoryMapper.selectList(wrapper);
    }

    private List<Memory> selectCoupleMemories(Long coupleId, LocalDate startDate, LocalDate endDate, String tag) {
        LambdaQueryWrapper<Memory> wrapper = new LambdaQueryWrapper<Memory>()
                .eq(Memory::getCoupleId, coupleId)
                .eq(Memory::getVisibility, "COUPLE");
        applyFilters(wrapper, null, startDate, endDate, tag);
        return memoryMapper.selectList(wrapper);
    }

    private void applyFilters(LambdaQueryWrapper<Memory> wrapper, String visibility, LocalDate startDate, LocalDate endDate, String tag) {
        if (visibility != null) {
            wrapper.eq(Memory::getVisibility, visibility);
        }
        if (startDate != null) {
            wrapper.ge(Memory::getMemoryDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Memory::getMemoryDate, endDate);
        }
        if (tag != null && !tag.isBlank()) {
            wrapper.like(Memory::getTags, tag.trim());
        }
    }

    private Memory requireVisibleMemory(Long userId, Long memoryId) {
        User user = requireUser(userId);
        Memory memory = memoryMapper.selectById(memoryId);
        if (memory == null) {
            throw new BusinessException(ErrorCode.MEMORY_NOT_FOUND);
        }
        if (memory.getCreatorId().equals(userId)) {
            return memory;
        }

        Couple couple = findActiveCouple(user);
        boolean coupleVisible = couple != null
                && "COUPLE".equals(memory.getVisibility())
                && couple.getId().equals(memory.getCoupleId());
        if (!coupleVisible) {
            throw new BusinessException(ErrorCode.MEMORY_ACCESS_DENIED);
        }
        return memory;
    }

    private Memory requireCreatedMemory(Long userId, Long memoryId) {
        Memory memory = memoryMapper.selectById(memoryId);
        if (memory == null) {
            throw new BusinessException(ErrorCode.MEMORY_NOT_FOUND);
        }
        if (!memory.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.MEMORY_ACCESS_DENIED);
        }
        return memory;
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
