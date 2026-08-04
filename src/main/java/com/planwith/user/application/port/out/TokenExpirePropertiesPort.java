package com.planwith.user.application.port.out;

public interface TokenExpirePropertiesPort {

    long getAccessTokenExpireMs();

    long getRefreshTokenExpireMs();
}
