package com.back.global.security;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final MemberService memberService;
    private final Rq rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("CustomAuthenticationFilter is called");

        if(!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if(List.of("/api/v1/members/join", "/api/v1/members/login").contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey;
        String accessToken;

        String authorizationHeader = rq.getHeader("Authorization", "");

        if (!authorizationHeader.isBlank()) {
            // 헤더 방식
            if (!authorizationHeader.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "잘못된 형식의 인증데이터입니다.");
            }

            String[] headerAuthorizationBits = authorizationHeader.split(" ", 3);            apiKey = authorizationHeader.replace("Bearer ", "");

            apiKey = headerAuthorizationBits[1];
            accessToken = headerAuthorizationBits.length == 3 ? headerAuthorizationBits[2] : "";
        } else {
            apiKey = rq.getCookieValue("apiKey", "");
            accessToken = rq.getCookieValue("accessToken", "");
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
            rq.addCookie("accessToken", newAccessToken);
            rq.setHeader("accessToken", newAccessToken);
        }

        // SecurityContextHolder에 인증데이터 저장
        filterChain.doFilter(request, response);
    }
}