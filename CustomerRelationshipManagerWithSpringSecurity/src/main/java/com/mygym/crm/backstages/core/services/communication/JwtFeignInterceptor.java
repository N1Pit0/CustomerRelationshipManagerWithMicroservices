package com.mygym.crm.backstages.core.services.communication;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class JwtFeignInterceptor implements RequestInterceptor {
    private final JwtTokenHolder jwtTokenHolder;

    public JwtFeignInterceptor(JwtTokenHolder jwtTokenHolder) {
        this.jwtTokenHolder = jwtTokenHolder;
    }

    @Override
    public void apply(RequestTemplate template) {
        String jwtToken = jwtTokenHolder.getJwtToken();

        System.out.println(jwtToken + " I am here");

        if (jwtToken != null) {
            template.header("Authorization", jwtToken);
        }
    }
}