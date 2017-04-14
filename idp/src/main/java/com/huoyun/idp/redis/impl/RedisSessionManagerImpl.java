package com.huoyun.idp.redis.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.huoyun.idp.encrypt.EncryptService;
import com.huoyun.idp.redis.RedisSessionContext;
import com.huoyun.idp.redis.RedisSessionManager;

public class RedisSessionManagerImpl implements RedisSessionManager {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RedisSessionManagerImpl.class);

	private static final int ConcurrentSessionCount = 5;

	@SuppressWarnings("rawtypes")
	private RedisTemplate redisTemplate;
	private EncryptService cipher ;

	private final static String RedisDKey = "xxx";

	@SuppressWarnings("rawtypes")
	public RedisSessionManagerImpl(RedisTemplate redisTemplate,EncryptService encryptService) {
		this.redisTemplate = redisTemplate;
		this.cipher = encryptService;
	}

	public static final String IDP_REDIS_NAMESPACE = "sap:anywhere:idp";

	public static final String APPS_REDIS_NAMESPACE = "sap:anywhere:apps";

	private static final String APP_SESSIONIDS_KEY = "sap:anywhere:apps:session:%s:sessionids";

	private static final String IDP_SESSIONIDS_KEY = "sap:anywhere:idp:session:%s:sessionids";

	private static final String SPRING_SESSION_PREFIX = "spring:session:%s:sessions:%s";

	private static final String SPRING_SESSION_EXPIRES_PREFIX = "spring:session:%s:sessions:expires:%s";

	private static final String LAST_ACCESSED_TIME = "lastAccessedTime";

	private static final String SESSION_INDEX = "sessionAttr:session_index";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * addApplicationSession(long, java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addApplicationSession(String userId, String sessionId) {
		if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(sessionId)) {
			LOGGER.warn("The parameters are missing call addApplicationSession, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}
		try {
			String key = String.format(APP_SESSIONIDS_KEY, userId);
			LOGGER.info("Add app session id to redis server with key " + key
					+ ".");
			BoundSetOperations boundSetOps = redisTemplate.boundSetOps(key);
			String redisDKey = RedisDKey;
			String encryptSessionId = "";
			try {
				encryptSessionId = cipher.encrypt(sessionId, redisDKey);
			} catch (Exception e) {
				LOGGER.error("encryt failed", e);
			}
			boundSetOps.add(encryptSessionId);
			boundSetOps.persist();
		} catch (Exception ex) {
			LOGGER.error("Add application session to redis server failure.", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * getApplicationSession(java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<String> getApplicationSessions(String userId) {
		List<String> plainSessionIds = new ArrayList<String>();
		if (StringUtils.isEmpty(userId)) {
			LOGGER.warn("The parameters are missing call getApplicationSessions, it did nothing.");
			return plainSessionIds;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return plainSessionIds;
		}

		try {
			String key = String.format(APP_SESSIONIDS_KEY, userId);
			BoundSetOperations boundSetOps = redisTemplate.boundSetOps(key);
			Set<String> keys = boundSetOps.members();
			String redisDKey = RedisDKey;
			try {
				for (String cipherSessionId : keys) {
					String plaintext = cipher.decrypt(cipherSessionId,
							redisDKey);
					plainSessionIds.add(plaintext);
				}
			} catch (Exception e) {
				LOGGER.error("encryt failed", e);
			}
		} catch (Exception ex) {
			LOGGER.error("Get application sessions from redis server failure.",
					ex);
		}
		return plainSessionIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * deleteApplicationSession(java.lang.String, java.lang.String)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteApplicationSession(String userId, String sessionId) {
		if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(sessionId)) {
			LOGGER.warn("The parameters are missing call deleteApplicationSession, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}

		try {
			String key = String.format(APP_SESSIONIDS_KEY, userId);
			LOGGER.info("Delete app session id from redis server with key "
					+ key + ".");
			BoundSetOperations boundSetOps = redisTemplate.boundSetOps(key);
			String redisDKey = RedisDKey;
			String encryptSessionId = "";
			try {
				encryptSessionId = cipher.encrypt(sessionId, redisDKey);
			} catch (Exception e) {
				LOGGER.error("encryt failed", e);
			}
			boundSetOps.remove(encryptSessionId);
			boundSetOps.persist();
		} catch (Exception ex) {
			LOGGER.error(
					"Delete application session from redis server failure.", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * addIDPSession(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addIDPSession(String userId, String sessionId) {
		if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(sessionId)) {
			LOGGER.warn("The parameters are missing call addIDPSession, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}

		try {
			String key = String.format(IDP_SESSIONIDS_KEY, userId);
			LOGGER.info("Add idp session id to redis server with key " + key
					+ ".");
			BoundSetOperations boundSetOps = redisTemplate.boundSetOps(key);
			String redisDKey = RedisDKey;
			String encryptSessionId = "";
			try {
				encryptSessionId = cipher.encrypt(sessionId, redisDKey);
			} catch (Exception e) {
				LOGGER.error("encryt failed", e);
			}
			boundSetOps.add(encryptSessionId);
			boundSetOps.persist();
		} catch (Exception ex) {
			LOGGER.error("Add IDP session to redis server failure.", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * getIDPSessions(java.lang.String)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<String> getIDPSessions(String userId) {
		List<String> plainSessionIds = new ArrayList<String>();
		if (StringUtils.isEmpty(userId)) {
			LOGGER.warn("The parameters are missing call getIDPSessions, it did nothing.");
			return plainSessionIds;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return plainSessionIds;
		}

		try {
			String key = String.format(IDP_SESSIONIDS_KEY, userId);
			BoundSetOperations boundSetOps = redisTemplate.boundSetOps(key);
			Set<String> keys = boundSetOps.members();
			String redisDKey = RedisDKey;
			try {
				for (String cipherSessionId : keys) {
					String plaintext = cipher.decrypt(cipherSessionId,
							redisDKey);
					plainSessionIds.add(plaintext);
				}
			} catch (Exception e) {
				LOGGER.error("encryt failed", e);
			}
		} catch (Exception ex) {
			LOGGER.error("Get IDP sessions from redis server failure.", ex);
		}
		return plainSessionIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * deleteIDPSession(java.lang.String, java.lang.String)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void deleteIDPSession(String userId, String sessionId) {
		if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(sessionId)) {
			LOGGER.warn("The parameters are missing call deleteIDPSession, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}

		try {
			String key = String.format(IDP_SESSIONIDS_KEY, userId);
			LOGGER.info("Delete idp session id from redis server with key "
					+ key + ".");
			BoundSetOperations boundSetOps = redisTemplate.boundSetOps(key);
			String redisDKey = RedisDKey;
			String encryptSessionId = "";
			try {
				encryptSessionId = cipher.encrypt(sessionId, redisDKey);
			} catch (Exception e) {
				LOGGER.error("encryt failed", e);
			}
			boundSetOps.remove(encryptSessionId);
			boundSetOps.persist();
		} catch (Exception ex) {
			LOGGER.error("Delete IDP session from redis server failure.", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#getAttribute
	 * (java.lang.String, java.lang.String)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getRedisSessionAttribute(String sessionId,
			String redisNamespace, String attributeName) {
		if (StringUtils.isEmpty(sessionId)
				|| StringUtils.isEmpty(redisNamespace)
				|| StringUtils.isEmpty(attributeName)) {
			LOGGER.warn("The parameters are missing call getRedisSessionAttribute, it did nothing.");
			return null;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return null;
		}
		LOGGER.info("Get spring session attribute with " + attributeName + ".");
		Object result = null;
		try {
			String key = String.format(SPRING_SESSION_PREFIX, redisNamespace,
					sessionId);
			BoundHashOperations boundHashOps = redisTemplate.boundHashOps(key);
			result = boundHashOps.get(attributeName);
		} catch (Exception ex) {
			LOGGER.error(
					"Get redis session attribute from redis server failure.",
					ex);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setRedisSessionAttribute(String sessionId,
			String redisNamespace, String attributeName, Object value) {
		if (StringUtils.isEmpty(sessionId)
				|| StringUtils.isEmpty(redisNamespace)
				|| StringUtils.isEmpty(attributeName)) {
			LOGGER.warn("The parameters are missing call setRedisSessionAttribute, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}
		LOGGER.info("Session spring session attribute with " + attributeName
				+ ".");
		try {
			String key = String.format(SPRING_SESSION_PREFIX, redisNamespace,
					sessionId);
			BoundHashOperations boundHashOps = redisTemplate.boundHashOps(key);
			boundHashOps.put(attributeName, value);
			boundHashOps.persist();
		} catch (Exception ex) {
			LOGGER.error(
					"Set redis session attribute to redis server failure.", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * expireSession(java.lang.String, java.lang.String)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public void expireSession(String sessionId, String redisNamespace) {
		if (StringUtils.isEmpty(sessionId)
				|| StringUtils.isEmpty(redisNamespace)) {
			LOGGER.warn("The parameters are missing call expireSession, it did nothing.");
			return;
		}
		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}

		try {
			String key = String.format(SPRING_SESSION_EXPIRES_PREFIX,
					redisNamespace, sessionId);
			redisTemplate.delete(key);
			key = String.format(SPRING_SESSION_PREFIX, redisNamespace,
					sessionId);
			redisTemplate.delete(key);
		} catch (Exception ex) {
			LOGGER.error("Expire redis session from redis server failure.", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#isExpired
	 * (java.lang.String)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public boolean isExpired(String sessionId, String redisNamespace) {
		if (StringUtils.isEmpty(sessionId)
				|| StringUtils.isEmpty(redisNamespace)) {
			LOGGER.warn("The parameters are missing call isExpired, it did nothing.");
			return false;
		}
		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return false;
		}

		String key = String.format(SPRING_SESSION_EXPIRES_PREFIX,
				redisNamespace, sessionId);
		Boolean hasKey = false;
		try {
			hasKey = !redisTemplate.hasKey(key);
		} catch (Exception ex) {
			LOGGER.error("Check redis session whether expire failure.", ex);
		}
		return hasKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * invalidateAllSessions(java.lang.String)
	 */
	@Override
	public void invalidateAllSessions(String userId) {
		LOGGER.info("Invalidate user's all sessions start...");
		if (StringUtils.isEmpty(userId)) {
			LOGGER.warn("The parameters are missing call invalidateAllSessions, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}

		try {
			List<String> idpSessions = this.getIDPSessions(userId);
			for (String idpSessionId : idpSessions) {
				this.expireSession(idpSessionId, IDP_REDIS_NAMESPACE);
				this.deleteIDPSession(userId, idpSessionId);
			}

			List<String> applicationSessions = this
					.getApplicationSessions(userId);
			for (String appSessionId : applicationSessions) {
				this.expireSession(appSessionId, APPS_REDIS_NAMESPACE);
				this.deleteApplicationSession(userId, appSessionId);
			}
			LOGGER.info("Invalidate user's all sessions end.");
		} catch (Exception ex) {
			LOGGER.error("Invalidate user's all session failure.", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * invalidateAllSessions(java.lang.String, java.lang.String)
	 */
	@Override
	public void invalidateAllSessions(String userId, String exceptedSessionIndex) {
		LOGGER.info("Invalidate user's all sessions start...");
		if (StringUtils.isEmpty(userId)
				|| StringUtils.isEmpty(exceptedSessionIndex)) {
			LOGGER.warn("The parameters are missing call invalidateAllSessions, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}

		try {
			List<String> idpSessions = this.getIDPSessions(userId);
			for (String idpSessionId : idpSessions) {
				Object sessionIndex = this.getRedisSessionAttribute(
						idpSessionId, IDP_REDIS_NAMESPACE, SESSION_INDEX);
				if (null != sessionIndex
						&& sessionIndex.toString().equals(exceptedSessionIndex)) {
					continue;
				}
				this.expireSession(idpSessionId, IDP_REDIS_NAMESPACE);
				this.deleteIDPSession(userId, idpSessionId);
			}

			List<String> applicationSessions = this
					.getApplicationSessions(userId);
			for (String appSessionId : applicationSessions) {
				Object sessionIndex = this.getRedisSessionAttribute(
						appSessionId, APPS_REDIS_NAMESPACE, SESSION_INDEX);
				if (null != sessionIndex
						&& sessionIndex.toString().equals(exceptedSessionIndex)) {
					continue;
				}
				this.expireSession(appSessionId, APPS_REDIS_NAMESPACE);
				this.deleteApplicationSession(userId, appSessionId);
			}
			LOGGER.info("Invalidate user's all sessions end.");
		} catch (Exception ex) {
			LOGGER.error("Invalidate user's all session failure.", ex);
		}
	}

	/**
	 * Invalid Application Session by user id and session id
	 * 
	 * @param userId
	 *            user id
	 * @param sessionId
	 *            session id
	 */
	@Override
	public void invalidateApplicationSession(String userId, String sessionId) {
		LOGGER.info(
				"Invalidate user's sessions start..., user id: {}, session id: {}",
				userId, sessionId);
		if (StringUtils.isEmpty(userId)) {
			LOGGER.warn("The userId are missing call invalidateAllSessions, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}

		try {
			this.expireSession(sessionId, APPS_REDIS_NAMESPACE);
			this.deleteApplicationSession(userId, sessionId);
			LOGGER.info(
					"Invalidate user's sessions end..., user id: {}, session id: {}",
					userId, sessionId);
		} catch (Exception ex) {
			LOGGER.error("Invalidate user's all session failure. user id: "
					+ userId + ", session id: " + sessionId, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.sbo.occ.sessionmgr.redis.session.mgr.RedisSessionManager#
	 * checkConcurrentSessionCount(java.lang.String)
	 */
	@Override
	public void checkConcurrentSessionCount(String userId) {
		LOGGER.info("Check concurrent session count start...");
		if (StringUtils.isEmpty(userId)) {
			LOGGER.warn("The parameters are missing call checkConcurrentSessionCount, it did nothing.");
			return;
		}

		RedisSessionContext redisSessionContext = RedisSessionContext
				.getInstance();
		if (!redisSessionContext.isEnableRedisSession()
				|| redisSessionContext.isRedisConnectionFailure()) {
			LOGGER.info("Disable redis session management or redis server connection failure.");
			return;
		}

		try {
			List<String> idpSessionIds = this.getIDPSessions(userId);

			// Because the application session is not added yet when check
			// concurrent session count in IDP, so need to
			// increase 1 to current apps session count.
			int currentSessionCount = idpSessionIds.size() + 1;
			if (currentSessionCount > ConcurrentSessionCount) {
				int invalidateSessionCount = currentSessionCount
						- ConcurrentSessionCount;
				LOGGER.info(
						"The current session count is {} and the concurrent session count is {}, so will invalidate {} earliest sessions of the user {}.",
						currentSessionCount, ConcurrentSessionCount,
						invalidateSessionCount, userId);
				invalidateExcessiveIdpSessions(userId, invalidateSessionCount,
						idpSessionIds);
			}
			LOGGER.info("Check concurrent session count end.");
		} catch (Exception ex) {
			LOGGER.error("Check concurrent session count failure.", ex);
		}
	}

	@SuppressWarnings("unchecked")
	private void invalidateExcessiveIdpSessions(String userId,
			int excessiveSessionNumber, List<String> idpSessionIds) {
		try {

			List<Boolean> isExpiredList = redisTemplate
					.executePipelined(new RedisCallback<Object>() {

						@SuppressWarnings("rawtypes")
						@Override
						public Object doInRedis(RedisConnection connection)
								throws DataAccessException {

							RedisSerializer keySerializer = redisTemplate
									.getKeySerializer();
							for (String idpSessionId : idpSessionIds) {
								connection.exists(keySerializer
										.serialize(String.format(
												SPRING_SESSION_EXPIRES_PREFIX,
												IDP_REDIS_NAMESPACE,
												idpSessionId)));
							}

							return null;
						}

					});

			List<Long> lastAccessedTimeList = redisTemplate
					.executePipelined(new RedisCallback<Object>() {

						@SuppressWarnings("rawtypes")
						@Override
						public Object doInRedis(RedisConnection connection)
								throws DataAccessException {

							RedisSerializer keySerializer = redisTemplate
									.getKeySerializer();
							RedisSerializer hashKeySerializer = redisTemplate
									.getHashKeySerializer();
							for (String idpSessionId : idpSessionIds) {
								connection.hGet(keySerializer.serialize(String
										.format(SPRING_SESSION_PREFIX,
												IDP_REDIS_NAMESPACE,
												idpSessionId)),
										hashKeySerializer
												.serialize(LAST_ACCESSED_TIME));
							}

							return null;
						}
					});

			List<SessionInfo> idpSessionInfos = new ArrayList<SessionInfo>();
			List<String> expiredAppsSessionIds = new ArrayList<String>();
			for (int i = 0; i < isExpiredList.size(); i++) {
				if (!isExpiredList.get(i)) {
					expiredAppsSessionIds.add(idpSessionIds.get(i));
				} else {
					idpSessionInfos.add(new SessionInfo(lastAccessedTimeList
							.get(i), idpSessionIds.get(i)));
				}
			}

			if (!expiredAppsSessionIds.isEmpty()) {
				LOGGER.info(
						"There are {} expired idp sessions of the user {}, so will delete them from user session list.",
						expiredAppsSessionIds.size(), userId);
				for (String expiredSessionId : expiredAppsSessionIds) {
					this.deleteIDPSession(userId, expiredSessionId);
				}
			}

			if (expiredAppsSessionIds.size() < excessiveSessionNumber) {
				Collections.sort(idpSessionInfos);
				int realInvalidateSessionCount = excessiveSessionNumber
						- expiredAppsSessionIds.size();
				for (int i = 0; i < realInvalidateSessionCount; i++) {
					SessionInfo sessionInfo = idpSessionInfos.get(i);
					String invalidatedAppsSessionId = sessionInfo
							.getSessionId();
					this.expireSession(invalidatedAppsSessionId,
							IDP_REDIS_NAMESPACE);
					this.deleteIDPSession(userId, invalidatedAppsSessionId);
				}
			}
		} catch (Exception e) {
			LOGGER.warn("invalidating excessive idp sessions failed.", e);
		}
	}

	public static final class SessionInfo implements Comparable<SessionInfo> {
		private long lassAccessedTime;
		private String sessionId;

		public SessionInfo(long lassAccessedTime, String sessionId) {
			this.lassAccessedTime = lassAccessedTime;
			this.sessionId = sessionId;
		}

		/**
		 * @return the lassAccessedTime
		 */
		public long getLassAccessedTime() {
			return lassAccessedTime;
		}

		/**
		 * @param lassAccessedTime
		 *            the lassAccessedTime to set
		 */
		public void setLassAccessedTime(long lassAccessedTime) {
			this.lassAccessedTime = lassAccessedTime;
		}

		/**
		 * @return the sessionId
		 */
		public String getSessionId() {
			return sessionId;
		}

		/**
		 * @param sessionId
		 *            the sessionId to set
		 */
		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		@Override
		public int compareTo(SessionInfo o) {
			if (o != null) {
				if (this.getLassAccessedTime() > o.getLassAccessedTime()) {
					return 1;
				} else if (this.getLassAccessedTime() == o
						.getLassAccessedTime()) {
					return 0;
				}
			}
			return -1;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}

			if (obj == null || !(obj instanceof SessionInfo)) {
				return false;
			}

			SessionInfo other = (SessionInfo) obj;
			return sessionId.equals(other.sessionId);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((sessionId == null) ? 0 : sessionId.hashCode());
			return result;
		}
	}
}
