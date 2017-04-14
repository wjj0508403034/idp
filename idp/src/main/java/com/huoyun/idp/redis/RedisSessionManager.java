package com.huoyun.idp.redis;

import java.util.List;

public interface RedisSessionManager {
	void addApplicationSession(String userId, String sessionId);

	void addIDPSession(String userId, String sessionId);

	List<String> getApplicationSessions(String userId);

	List<String> getIDPSessions(String userId);

	void deleteApplicationSession(String userId, String sessionId);

	void deleteIDPSession(String userId, String sessionId);

	Object getRedisSessionAttribute(String sessionId, String redisNamespace,
			String attributeName);

	void expireSession(String sessionId, String redisNamespace);

	boolean isExpired(String sessionId, String redisNamespace);

	void setRedisSessionAttribute(String sessionId, String redisNamespace,
			String attributeName, Object value);

	void invalidateAllSessions(String userId);

	void invalidateAllSessions(String userId, String exceptedSessionIndex);

	void checkConcurrentSessionCount(String userId);

	void invalidateApplicationSession(String userId, String sessionId);
}
