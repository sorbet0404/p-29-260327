package com.back.domain.member.controller;

import com.back.domain.member.dto.MemberWithUsernameDto;
import com.back.domain.member.service.MemberService;
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

    @GetMapping
    public List<MemberWithUsernameDto> list() {

        return memberService.findAll()
                .stream()
                .map(MemberWithUsernameDto::new)
                .toList();

    }
}