package com.back.global.rq;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope//생명주기는 request수준으로, 요청이 있을때마다 새로운 객체가 생성되고 끝나면 소멸.
public class Rq {

}
