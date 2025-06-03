package com.mygym.crm.backstages.core.services.communication;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class JwtTokenHolder {
    private String jwtToken;

}