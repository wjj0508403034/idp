package com.huoyun.idp.redis;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisSessionContext {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionContext.class);
    private static final String ENABLE_REDIS_SESSION_MANAGEMENT = "enable.redis.session.management";

    private static final String TRY_CONNECT_INTERVAL_WITH_MINUTES = "try.connect.interval.with.minutes";

    private static class RedisSessionContextHolder {
        private final static RedisSessionContext INSTANCE = new RedisSessionContext();
    }

    private RedisSessionContext() {
//        String sldServiceToken = SldSettings.getProperty(SldSettings.PROP_SLD_SERVICE_TOKEN);
//        String enableRedisSessionManagement = System.getProperty(ENABLE_REDIS_SESSION_MANAGEMENT);

//        if (StringUtils.isNotEmpty(sldServiceToken)) {
//            if ("false".equalsIgnoreCase(enableRedisSessionManagement)) {
//                LOGGER.info("Disable redis session management.");
//                enableRedisSession = false;
//            } else {
//                LOGGER.info("Enable redis session management.");
//                enableRedisSession = true;
//            }
//        } else {
//            if ("true".equalsIgnoreCase(enableRedisSessionManagement)) {
//                LOGGER.info("Enable redis session management.");
//                enableRedisSession = true;
//            } else {
//                LOGGER.info("Disable redis session management.");
//                enableRedisSession = false;
//            }
//        }
    	
    	String enableRedisSessionManagement = null;
        try {
        	enableRedisSessionManagement = System.getProperty(ENABLE_REDIS_SESSION_MANAGEMENT);
        }
        catch (Exception e) {
        	LOGGER.error("get JVM argument " + 
        			ENABLE_REDIS_SESSION_MANAGEMENT + 
        			"failed, this setting will be set to true by default.", e);
        	enableRedisSessionManagement = "true";
        }
        
        if ("false".equalsIgnoreCase(enableRedisSessionManagement)) {
            LOGGER.info("Disable redis session management.");
            enableRedisSession = false;
        } else {
            LOGGER.info("Enable redis session management.");
            enableRedisSession = true;
        }

        
    	String tryConnectIntervalWithMinutes = null;
        try {
        	tryConnectIntervalWithMinutes = System.getProperty(TRY_CONNECT_INTERVAL_WITH_MINUTES);
        }
        catch (Exception e) {
        	LOGGER.error("get JVM argument " + 
        			TRY_CONNECT_INTERVAL_WITH_MINUTES + 
        			"failed, this setting will be set to 2 by default.", e);
        }
        if (StringUtils.isNotEmpty(tryConnectIntervalWithMinutes)) {
            this.tryConnectionInterval = Integer.valueOf(tryConnectIntervalWithMinutes);
        } else {
            this.tryConnectionInterval = 2;
        }
    }

    public static RedisSessionContext getInstance() {
        return RedisSessionContextHolder.INSTANCE;
    }

    private volatile boolean enableRedisSession = true;

    private volatile int tryConnectionInterval = 0;

    private volatile boolean redisConnectionFailure = false;

    private volatile DateTime redisConnectionFailureTime;

    private volatile int tryConnectionTimes = 0;

    /**
     * @return the enableRedisSession
     */
    public boolean isEnableRedisSession() {
        return enableRedisSession;
    }

    /**
     * @return the redisConnectionFailure
     */
    public boolean isRedisConnectionFailure() {
        return redisConnectionFailure;
    }

    /**
     * @param redisConnectionFailure
     *            the redisConnectionFailure to set
     */
    public void setRedisConnectionFailure(boolean redisConnectionFailure) {
        this.redisConnectionFailure = redisConnectionFailure;
    }

    /**
     * @return the redisConnectionFailureTime
     */
    public DateTime getRedisConnectionFailureTime() {
        return redisConnectionFailureTime;
    }

    /**
     * @param redisConnectionFailureTime
     *            the redisConnectionFailureTime to set
     */
    public void setRedisConnectionFailureTime(DateTime redisConnectionFailureTime) {
        this.redisConnectionFailureTime = redisConnectionFailureTime;
    }

    /**
     * @return the tryConnectionTimes
     */
    public int getTryConnectionTimes() {
        return tryConnectionTimes;
    }

    /**
     * @param tryConnectionTimes
     *            the tryConnectionTimes to set
     */
    public void setTryConnectionTimes(int tryConnectionTimes) {
        this.tryConnectionTimes = tryConnectionTimes;
    }

    /**
     * @return the tryConnectionInterval
     */
    public int getTryConnectionInterval() {
        return tryConnectionInterval;
    }
}
