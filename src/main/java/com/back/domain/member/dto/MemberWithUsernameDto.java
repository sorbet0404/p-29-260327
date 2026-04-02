package com.back.domain.member.dto;

import com.back.domain.member.entity.Member;

import java.time.LocalDateTime;

public record MemberWithUsernameDto(
        int id,
        String name,
        String username,
        String nickname,
        LocalDateTime createDate,
        LocalDateTime modifyDate
) {
    public MemberWithUsernameDto(Member member) {
        this(
                member.getId(),
                member.getName(),
                member.getUsername(),
                member.getNickname(),
                member.getCreateDate(),
                member.getModifyDate()
        );
    }
}