package com.lovemaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.response.PairingResponse;
import com.lovemaster.dto.response.PartnerResponse;
import com.lovemaster.entity.Couple;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.mapper.CoupleMapper;
import com.lovemaster.mapper.UserMapper;
import com.lovemaster.service.PairingService;
import com.lovemaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PairingServiceImpl implements PairingService {

    private final CoupleMapper coupleMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    @Override
    public PairingResponse getMyPairing(Long userId) {
        User user = requireUser(userId);
        if (user.getPartnerId() == null) {
            return PairingResponse.unpaired(user.getPairCode());
        }

        User partner = requireUser(user.getPartnerId());
        Couple couple = findActiveCouple(userId);
        PairingResponse response = new PairingResponse();
        response.setPaired(true);
        response.setPairCode(user.getPairCode());
        response.setCoupleId(couple != null ? couple.getId() : null);
        response.setPartner(PartnerResponse.fromEntity(partner));
        response.setPairedAt(user.getPairedAt());
        return response;
    }

    @Override
    @Transactional
    public PairingResponse bind(Long userId, BindPairRequest request) {
        User currentUser = requireUser(userId);
        User partner = userService.findByPairCode(request.getPairCode());

        if (partner == null) {
            throw new BusinessException(ErrorCode.PAIR_CODE_INVALID);
        }
        if (partner.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_PAIR_SELF);
        }
        if (currentUser.getPartnerId() != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_PAIRED);
        }
        if (partner.getPartnerId() != null) {
            throw new BusinessException(ErrorCode.PARTNER_ALREADY_PAIRED);
        }

        LocalDateTime now = LocalDateTime.now();
        Couple couple = new Couple();
        couple.setUserId1(currentUser.getId());
        couple.setUserId2(partner.getId());
        couple.setStatus(1);
        couple.setPairedAt(now);
        coupleMapper.insert(couple);

        currentUser.setPartnerId(partner.getId());
        currentUser.setPairedAt(now);
        partner.setPartnerId(currentUser.getId());
        partner.setPairedAt(now);
        userService.update(currentUser);
        userService.update(partner);

        return getMyPairing(userId);
    }

    @Override
    @Transactional
    public void unbind(Long userId) {
        User currentUser = requireUser(userId);
        if (currentUser.getPartnerId() == null) {
            throw new BusinessException(ErrorCode.PAIRING_NOT_FOUND);
        }

        User partner = requireUser(currentUser.getPartnerId());
        Couple couple = findActiveCouple(userId);
        if (couple != null) {
            couple.setStatus(0);
            coupleMapper.updateById(couple);
        }

        clearPairing(currentUser.getId());
        clearPairing(partner.getId());
    }

    private User requireUser(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private Couple findActiveCouple(Long userId) {
        return coupleMapper.selectOne(new LambdaQueryWrapper<Couple>()
                .eq(Couple::getStatus, 1)
                .and(wrapper -> wrapper
                        .eq(Couple::getUserId1, userId)
                        .or()
                        .eq(Couple::getUserId2, userId)));
    }

    private void clearPairing(Long userId) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getPartnerId, null)
                .set(User::getPairedAt, null));
    }
}
