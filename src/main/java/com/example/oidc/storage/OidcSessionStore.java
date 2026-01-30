package com.example.oidc.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.oidc.dto.IdCardSession;
import com.example.oidc.dto.MobileIdSession;
import com.example.oidc.dto.SmartIdSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OidcSessionStore implements IOidcSessionStore {
    private static final String MOBILEID_SESSION_PREFIX = "mobileid:session:";
    private static final String CODE_PREFIX = "oidc:code:";
    private static final String TOKEN_PREFIX = "oidc:token:";
    private static final String SMARTID_SESSION_PREFIX = "smartid:session:";
    private static final String PKCE_PREFIX = "oidc:pkce:";

    private static final Logger log = LoggerFactory.getLogger(OidcSessionStore.class);

    private final IRedisClient redisClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String IDCARD_SESSION_PREFIX = "idcard-session:";

    @Autowired
    public OidcSessionStore(IRedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public void storeMobileIdSession(String sessionId, MobileIdSession session) {
        try {
            redisClient.setObject(MOBILEID_SESSION_PREFIX + sessionId, session);
        } catch (Exception e) {
            log.error("Failed to store MobileId session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public MobileIdSession getMobileIdSession(String sessionId) {
        try {
            return redisClient.getObject(MOBILEID_SESSION_PREFIX + sessionId, MobileIdSession.class);
        } catch (Exception e) {
            log.error("Failed to get MobileId session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    @Override
    public void storeSmartIdSession(String sessionId, SmartIdSession session) {
        try {
            redisClient.setObject(SMARTID_SESSION_PREFIX + sessionId, session);
        } catch (Exception e) {
            log.error("Failed to store SmartId session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public SmartIdSession getSmartIdSession(String sessionId) {
        try {
            return redisClient.getObject(SMARTID_SESSION_PREFIX + sessionId, SmartIdSession.class);
        } catch (Exception e) {
            log.error("Failed to get SmartId session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    @Override
    public void storeCode(String code, UserInfo user) {
        storeCode(code, user, null);
    }

    @Override
    public void storeCode(String code, UserInfo user, com.example.oidc.dto.PkceData pkceData) {
        try {
            redisClient.setObject(CODE_PREFIX + code, user);
            if (pkceData != null) {
                redisClient.setObject(PKCE_PREFIX + code, pkceData);
            }
        } catch (Exception e) {
            log.error("Failed to store code {}: {}", code, e.getMessage());
        }
    }

    @Override
    public com.example.oidc.dto.PkceData getPkceDataByCode(String code) {
        try {
            return redisClient.getObject(PKCE_PREFIX + code, com.example.oidc.dto.PkceData.class);
        } catch (Exception e) {
            log.error("Failed to get PKCE data for code {}: {}", code, e.getMessage());
            return null;
        }
    }

    @Override
    public void storeToken(String token, UserInfo user) {
        try {
            redisClient.setObject(TOKEN_PREFIX + token, user);
        } catch (Exception e) {
            log.error("Failed to store token {}: {}", token, e.getMessage());
        }
    }

    @Override
    public UserInfo getUserByCode(String code) {
        String json;
        try {
            json = redisClient.getValue(CODE_PREFIX + code);
        } catch (Exception e) {
            log.error("Failed to fetch code {}: {}", code, e.getMessage());
            return null;
        }
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, UserInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize UserInfo for code {}: {}", code, e.getMessage());
            return null;
        }
    }

    @Override
    public UserInfo getUserByToken(String token) {
        String json;
        try {
            json = redisClient.getValue(TOKEN_PREFIX + token);
        } catch (Exception e) {
            log.error("Failed to fetch token {}: {}", token, e.getMessage());
            return null;
        }
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, UserInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize UserInfo for token {}: {}", token, e.getMessage());
            return null;
        }
    }

    @Override
    public void storeIdCardSession(String sessionId, IdCardSession session) {
        // Store in Redis or in-memory map as appropriate for your implementation
        // Example for in-memory map:
        // idCardSessionMap.put(sessionId, session);

        // If using RedisClient:
        redisClient.setObject(IDCARD_SESSION_PREFIX + sessionId, session);
    }

    @Override
    public IdCardSession getIdCardSession(String sessionId) {
        try {
            return redisClient.getObject(IDCARD_SESSION_PREFIX + sessionId, IdCardSession.class);
        } catch (Exception e) {
            log.error("Failed to get IdCard session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }
}