/*
 * Copyright (c) 2023 Macula
 *   macula.dev, China
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.macula.cloud.iam.service.oauth2;

import dev.macula.boot.constants.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.Assert;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * {@code MaculaOAuth2AuthorizationService} TOKEN缓存服务
 *
 * @author rain
 * @since 2023/4/11 08:14
 */
@RequiredArgsConstructor
public class MaculaOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final static Long TIMEOUT = 10L;

    private static final String AUTHORIZATION = CacheConstants.OAUTH2_TOKEN_KEY;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        if (isState(authorization)) {
            String token = authorization.getAttribute(OAuth2ParameterNames.STATE);
            redisTemplate.opsForValue()
                .set(buildKey(OAuth2ParameterNames.STATE, token), authorization, TIMEOUT, TimeUnit.MINUTES);
        }

        if (isCode(authorization)) {
            OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
            OAuth2AuthorizationCode authorizationCodeToken = authorizationCode.getToken();
            long between =
                ChronoUnit.MINUTES.between(authorizationCodeToken.getIssuedAt(), authorizationCodeToken.getExpiresAt());
            redisTemplate.opsForValue()
                .set(buildKey(OAuth2ParameterNames.CODE, authorizationCodeToken.getTokenValue()), authorization,
                    between, TimeUnit.MINUTES);
        }

        if (isRefreshToken(authorization)) {
            OAuth2RefreshToken refreshToken = authorization.getRefreshToken().getToken();
            long between = ChronoUnit.SECONDS.between(refreshToken.getIssuedAt(), refreshToken.getExpiresAt());
            redisTemplate.opsForValue()
                .set(buildKey(OAuth2ParameterNames.REFRESH_TOKEN, refreshToken.getTokenValue()), authorization, between,
                    TimeUnit.SECONDS);
        }

        if (isAccessToken(authorization)) {
            OAuth2AccessToken accessToken = authorization.getAccessToken().getToken();
            long between = ChronoUnit.SECONDS.between(accessToken.getIssuedAt(), accessToken.getExpiresAt());
            redisTemplate.opsForValue()
                .set(buildKey(OAuth2ParameterNames.ACCESS_TOKEN, accessToken.getTokenValue()), authorization, between,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        List<String> keys = new ArrayList<>();
        if (isState(authorization)) {
            String token = authorization.getAttribute(OAuth2ParameterNames.STATE);
            keys.add(buildKey(OAuth2ParameterNames.STATE, token));
        }

        if (isCode(authorization)) {
            OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
            OAuth2AuthorizationCode authorizationCodeToken = authorizationCode.getToken();
            keys.add(buildKey(OAuth2ParameterNames.CODE, authorizationCodeToken.getTokenValue()));
        }

        if (isRefreshToken(authorization)) {
            OAuth2RefreshToken refreshToken = authorization.getRefreshToken().getToken();
            keys.add(buildKey(OAuth2ParameterNames.REFRESH_TOKEN, refreshToken.getTokenValue()));
        }

        if (isAccessToken(authorization)) {
            OAuth2AccessToken accessToken = authorization.getAccessToken().getToken();
            keys.add(buildKey(OAuth2ParameterNames.ACCESS_TOKEN, accessToken.getTokenValue()));
        }
        redisTemplate.delete(keys);
    }

    @Override
    @Nullable
    public OAuth2Authorization findById(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nullable
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        Assert.notNull(tokenType, "tokenType cannot be empty");
        return (OAuth2Authorization)redisTemplate.opsForValue().get(buildKey(tokenType.getValue(), token));
    }

    private String buildKey(String type, String id) {
        return String.format("%s:%s:%s", AUTHORIZATION, type, id);
    }

    private static boolean isState(OAuth2Authorization authorization) {
        return Objects.nonNull(authorization.getAttribute(OAuth2ParameterNames.STATE));
    }

    private static boolean isCode(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
            authorization.getToken(OAuth2AuthorizationCode.class);
        return Objects.nonNull(authorizationCode);
    }

    private static boolean isRefreshToken(OAuth2Authorization authorization) {
        return Objects.nonNull(authorization.getRefreshToken());
    }

    private static boolean isAccessToken(OAuth2Authorization authorization) {
        return Objects.nonNull(authorization.getAccessToken());
    }
}
