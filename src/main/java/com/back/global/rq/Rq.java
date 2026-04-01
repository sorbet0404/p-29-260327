package com.back.global.rq;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final MemberService memberService;

    public Member getActor() {

        String authorizationHeader = getHeader("Authorization", "");

        String apiKey;
        String accessToken;

        if (!authorizationHeader.isBlank()) {
            // 헤더 방식
            if (!authorizationHeader.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "잘못된 형식의 인증데이터입니다.");
            }

            String[] headerAuthorizationBits = authorizationHeader.split(" ", 3);            apiKey = authorizationHeader.replace("Bearer ", "");

            apiKey = headerAuthorizationBits[1];
            accessToken = headerAuthorizationBits.length == 3 ? headerAuthorizationBits[2] : "";
        } else {
            apiKey = getCookieValue("apiKey", "");
            accessToken = getCookieValue("accessToken", "");
        }

        Member member = null;

        if (apiKey.isBlank()) {
            throw new ServiceException("401-1", "apiKey가 존재하지 않습니다.");
        }

        if (!accessToken.isBlank()) {
            Map<String, Object> payload = memberService.payloadOrNull(accessToken);

            if (payload != null) {
                int id = (int) payload.get("id");
                member = memberService.findById(id)
                        .orElseThrow(() -> new ServiceException("401-3", "accessToken의 id에 해당하는 회원이 존재하지 않습니다."));
            }
        }

        // accessToken으로 인증이 제대로 이루어지지 않은 경우
        if (member == null) {
            member = memberService
                    .findByApiKey(apiKey)
                    .orElseThrow(() -> new ServiceException("401-4", "API 키가 유효하지 않습니다."));
        }

        return member;

    }

    private String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(request.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    private String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(request.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(cookies)
                                        .filter(cookie -> cookie.getName().equals(name))
                                        .map(Cookie::getValue)
                                        .filter(value -> !value.isBlank())
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void deleteCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    public void addCookie(String name, String value) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");

        response.addCookie(
                cookie
        );
    }
}