package com.huoyun.idp.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.sap.security.saml2.idp.session.AuthenticationContext;
import com.sap.security.saml2.idp.session.IdPSession;
import com.sap.security.saml2.idp.session.ModifiableIdPSession;
import com.sap.security.saml2.idp.session.SPSession;

public class IdPSessionImpl implements IdPSession, Serializable {

	private static final long serialVersionUID = 7857041504925325815L;
	private String sessionId = null;
	private String subjectId = null;
	private String ipAddress = null;
	private Date creationDate = null;
	private Date modificationDate = null;
	private Date expirationDate = null;
	private int status;
	private String sloReason = null;
	private String sloInitiatorRequestId = null;
	private String sloInitiatorRequestIssuer = null;
	private String sloInitiatorRequestBinding = null;
	private String sloInitiatorRelayState = null;
	boolean isSLOPartial;

	HashMap<String, SPSession> spSessionsMap = null;
	HashMap<String, AuthenticationContext> authnContextsMap = null;

	@Override
	public String geSLOInitiatorRequestId() {
		return sloInitiatorRequestId;
	}

	@Override
	public AuthenticationContext getAuthenticationContext(String classRef) {
		if (authnContextsMap != null) {
			return authnContextsMap.get(classRef);
		}
		return null;
	}

	@Override
	public Collection<AuthenticationContext> getAuthenticationContexts() {
		if (authnContextsMap != null) {
			return authnContextsMap.values();
		}
		return new ArrayList<AuthenticationContext>();
	}

	@Override
	public String getClientIP() {
		return ipAddress;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}

	@Override
	public SPSession getFirstSPSession(int status) {
		if (spSessionsMap != null) {
			Iterator<SPSession> iter = spSessionsMap.values().iterator();
			while (iter.hasNext()) {
				SPSession session = iter.next();
				if (session.getStatus() == status) {
					return session;
				}
			}
		}
		return null;
	}

	@Override
	public ModifiableIdPSession getModifiableIdPSession() {
		return null;
	}

	@Override
	public Date getModificationDate() {
		return modificationDate;
	}

	@Override
	public String getSLOInitiatorRelayState() {
		return sloInitiatorRelayState;
	}

	@Override
	public String getSLOInitiatorRequestBinding() {
		return sloInitiatorRequestBinding;
	}

	@Override
	public String getSLOInitiatorRequestIssuer() {
		return sloInitiatorRequestIssuer;
	}

	@Override
	public String getSLOReason() {
		return sloReason;
	}

	@Override
	public SPSession getSPSession(String spName) {
		if (spSessionsMap != null) {
			return spSessionsMap.get(spName);
		}
		return null;
	}

	@Override
	public Collection<SPSession> getSPSessions() {
		if (spSessionsMap != null) {
			return spSessionsMap.values();
		}
		return new ArrayList<SPSession>();
	}

	@Override
	public Collection<SPSession> getSPSessions(int status) {
		Collection<SPSession> result = new ArrayList<SPSession>();

		if (spSessionsMap != null) {
			Iterator<SPSession> iter = spSessionsMap.values().iterator();
			while (iter.hasNext()) {
				SPSession session = iter.next();
				if (session.getStatus() == status) {
					result.add(session);
				}
			}
		}

		return result;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public String getSubjectId() {
		return subjectId;
	}

	@Override
	public boolean isSLOPartial() {
		return isSLOPartial;
	}

	// hidden methods for updating the IdPSession when persisting is done
	public void setCreationDate(Date date) {
		if (date != null) {
			this.creationDate = date;
		}
	}

	public void setExpirationDate(Date date) {
		if (date != null) {
			this.expirationDate = date;
		}
	}

	public void setModificationDate(Date date) {
		if (date != null) {
			this.modificationDate = date;
		}
	}

	public void setSessionId(String id) {
		if (id != null) {
			this.sessionId = id;
		}
	}

	public void setSubjectId(String subjectId) {
		if (subjectId != null) {
			this.subjectId = subjectId;
		}
	}

	public void setClientIP(String ipAddress) {
		if (ipAddress != null) {
			this.ipAddress = ipAddress;
		}
	}

	public void setSLOInitiatorRelayState(String relayState) {
		if (relayState != null) {
			this.sloInitiatorRelayState = relayState;
		}
	}

	public void setSLOInitiatorRequestBinding(String binding) {
		if (binding != null) {
			this.sloInitiatorRequestBinding = binding;
		}
	}

	public void setSLOInitiatorRequestId(String id) {
		if (id != null) {
			this.sloInitiatorRequestId = id;
		}
	}

	public void setSLOInitiatorRequestIssuer(String issuer) {
		if (issuer != null) {
			this.sloInitiatorRequestIssuer = issuer;
		}
	}

	public void setSLOPartial(boolean isPartial) {
		this.isSLOPartial = isPartial;
	}

	public void setSLOReason(String reason) {
		if (reason != null) {
			this.sloReason = reason;
		}
	}

	public void setStatus(int status) {
		if (status > 0) {
			this.status = status;
		}
	}

	public void setSPSession(SPSession spSession) {
		if (spSession != null) {
			if (spSessionsMap == null) {
				spSessionsMap = new HashMap<String, SPSession>();
			}
			spSessionsMap.put(spSession.getSPName(), spSession);
		}
	}

	public void setAuthenticationContext(AuthenticationContext authnContext) {
		if (authnContext != null) {
			if (authnContextsMap == null) {
				authnContextsMap = new HashMap<String, AuthenticationContext>();
			}
			authnContextsMap.put(authnContext.getClassRef(), authnContext);
		}
	}

}
