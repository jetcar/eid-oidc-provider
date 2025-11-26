package com.example.oidc.storage;

import com.example.oidc.dto.IdCardSession;
import com.example.oidc.dto.MobileIdSession;
import com.example.oidc.dto.SmartIdSession;

/**
 * Interface for OIDC session storage operations.
 * Handles storage and retrieval of authentication sessions and user
 * information.
 */
public interface IOidcSessionStore {

    /**
     * Stores a Mobile-ID authentication session.
     *
     * @param sessionId the unique session identifier
     * @param session   the Mobile-ID session data
     */
    void storeMobileIdSession(String sessionId, MobileIdSession session);

    /**
     * Retrieves a Mobile-ID authentication session.
     *
     * @param sessionId the unique session identifier
     * @return the Mobile-ID session data or null if not found
     */
    MobileIdSession getMobileIdSession(String sessionId);

    /**
     * Stores a Smart-ID authentication session.
     *
     * @param sessionId the unique session identifier
     * @param session   the Smart-ID session data
     */
    void storeSmartIdSession(String sessionId, SmartIdSession session);

    /**
     * Retrieves a Smart-ID authentication session.
     *
     * @param sessionId the unique session identifier
     * @return the Smart-ID session data or null if not found
     */
    SmartIdSession getSmartIdSession(String sessionId);

    /**
     * Stores an ID-Card authentication session.
     *
     * @param sessionId the unique session identifier
     * @param session   the ID-Card session data
     */
    void storeIdCardSession(String sessionId, IdCardSession session);

    /**
     * Retrieves an ID-Card authentication session.
     *
     * @param sessionId the unique session identifier
     * @return the ID-Card session data or null if not found
     */
    IdCardSession getIdCardSession(String sessionId);

    /**
     * Stores an authorization code associated with user information.
     *
     * @param code the authorization code
     * @param user the user information to associate with the code
     */
    void storeCode(String code, UserInfo user);

    /**
     * Stores an access token associated with user information.
     *
     * @param token the access token
     * @param user  the user information to associate with the token
     */
    void storeToken(String token, UserInfo user);

    /**
     * Retrieves user information by authorization code.
     *
     * @param code the authorization code
     * @return the user information or null if not found
     */
    UserInfo getUserByCode(String code);

    /**
     * Retrieves user information by access token.
     *
     * @param token the access token
     * @return the user information or null if not found
     */
    UserInfo getUserByToken(String token);
}
