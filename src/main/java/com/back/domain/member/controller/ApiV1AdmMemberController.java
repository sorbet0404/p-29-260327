package com.back.domain.member.controller;

import com.back.domain.member.dto.MemberWithUsernameDto;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.rq.Rq;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/adm/members")
public class ApiV1AdmMemberController {

    private final MemberService memberService;
    private final Rq rq;

    @GetMapping
    public List<MemberWithUsernameDto> list() {
        Member actor = rq.getActor(); // 인증

        // 관리자 권한 체크
        if(!actor.isAdmin()){
            throw new ServiceException("403-1", "권한이 없습니다.");
        }

        return memberService.findAll()
                .stream()
                .map(MemberWithUsernameDto::new)
                .toList();

    }

}