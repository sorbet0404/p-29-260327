package com.back.global.rq;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
//생명주기는 request수준으로, 요청이 있을때마다 새로운 객체가 생성되고 끝나면 소멸.
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest request;//얘가 사실 requestscope역할 해준다.
    private final MemberService memberService;

    public Member getActor() {

        //얘는 어떻게 넘어온거?
        String authorizationHeader = request.getHeader("Authorization");

        if(authorizationHeader == null) {
            throw new ServiceException("401-1", "인증 정보가 헤더에 존재하지 않습니다.");
        }

        if(!authorizationHeader.startsWith("Bearer ")) {
            throw new ServiceException("401-2", "잘못된 형식의 인증데이터입니다.");
        }

        String apiKey = authorizationHeader.replace("Bearer ", "");

        return memberService.findByApiKey(apiKey).orElseThrow(
                () -> new ServiceException("401-1", "유효하지 않은 API 키입니다.")
        );
    }
}
