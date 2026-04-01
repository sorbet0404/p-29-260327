package com.back.global.rq;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
//생명주기는 request수준으로, 요청이 있을때마다 새로운 객체가 생성되고 끝나면 소멸.
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest request;//얘가 사실 requestscope역할 해준다.
    private final MemberService memberService;
    private final HttpServletResponse response;



    public Member getActor() {
        String apiKey = null;
        //얘는 어떻게 넘어온거?
        String authorizationHeader = request.getHeader("Authorization");

        //헤더 방식일때.
        if(authorizationHeader != null && !authorizationHeader.isEmpty()) {

            if (!authorizationHeader.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "헤더의 인증 정보 형식이 올바르지 않습니다.");
            }
            apiKey = authorizationHeader.replace("Bearer ", "");
        } else {//쿠키 방식일때.
            //여러개가 들어올수 있으니까 배열로 받기
            Cookie[] cookies = request.getCookies();

            if(cookies == null) {
                throw new ServiceException("401-1", "인증 정보가 없습니다.");
            }

            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("apiKey")) {
                    apiKey = cookie.getValue();
                    break;
                }
            }
        }

        if(apiKey.isBlank()) {
            throw new ServiceException("401-3", "인증 정보가 존재하지 않습니다.");
        }

        return memberService.findByApiKey(apiKey).orElseThrow(
                () -> new ServiceException("401-1", "유효하지 않은 API 키입니다.")
        );
    }
    public void addCookie(String name, String value) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");//사용할수 있는 경로 설정
        cookie.setHttpOnly(true);//JS에서 가져갈수 있는지 설정
        cookie.setDomain("localhost");//사용할수 있는 도메인 설정

        response.addCookie(
                cookie
        );
    }
    public void deleteCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}
