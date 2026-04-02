package com.back.global.rq;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

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

        boolean isAccessTokenExists = !accessToken.isBlank();
        boolean isAccessTokenValid = false;

        if (apiKey.isBlank()) {
            throw new ServiceException("401-1", "apiKey가 존재하지 않습니다.");
        }

        if (isAccessTokenExists) {
            Map<String, Object> payload = memberService.payloadOrNull(accessToken);

            if (payload != null) {
                int id = (int) payload.get("id");
                String username = (String) payload.get("username");
                String nickname = (String) payload.get("nickname");
                member = new Member(id, username, nickname);
                isAccessTokenValid = true;


            }
        }

        // accessToken으로 인증이 제대로 이루어지지 않은 경우
        if (member == null) {
            member = memberService
                    .findByApiKey(apiKey)
                    .orElseThrow(() -> new ServiceException("401-4", "API 키가 유효하지 않습니다."));
        }
        if (isAccessTokenExists && !isAccessTokenValid) {
            String newAccessToken = memberService.genAccessToken(member);
            addCookie("accessToken", newAccessToken);
            setHeader("accessToken", newAccessToken);
        }

        return member;
    }
    private void setHeader(String name, String value) {
        response.setHeader(name, value);
    }


    private String getHeader(String name, String defaultValue) {
        // 1. 요청(request)에서 해당 이름의 헤더 값을 가져옵니다.
        String value = request.getHeader(name);

        // 2. 값이 존재(null이 아님)하고, 공백이 아닌지 확인합니다.
        if (value != null && !value.isBlank()) {
            return value; // 유효한 값이면 즉시 반환합니다.
        }

        // 3. 값이 없거나 공백이라면 준비해둔 기본값을 반환합니다.
        return defaultValue;
    }

    private String getCookieValue(String name, String defaultValue) {
        // 1. 요청(request)에서 모든 쿠키를 가져옵니다.
        Cookie[] cookies = request.getCookies();

        // 2. 쿠키가 하나도 없으면(null이면) 바로 기본값을 돌려줍니다.
        if (cookies == null) {
            return defaultValue;
        }

        // 3. 쿠키 배열을 하나씩 살펴봅니다. (반복문)
        for (Cookie cookie : cookies) {
            // 4. 쿠키의 이름이 내가 찾는 이름(name)과 같은지 확인합니다.
            if (cookie.getName().equals(name)) {
                String value = cookie.getValue(); // 값을 꺼내서

                // 5. 값이 null이 아니고 비어있지(blank) 않은지 확인합니다.
                if (value != null && !value.isBlank()) {
                    return value; // 조건을 만족하면 즉시 그 값을 반환하고 함수를 끝냅니다.
                }
            }
        }

        // 6. 반복문을 다 돌았는데도 못 찾았다면 기본값을 반환합니다.
        return defaultValue;
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