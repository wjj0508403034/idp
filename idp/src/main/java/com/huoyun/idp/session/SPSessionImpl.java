package com.huoyun.idp.session;

import java.util.Date;

import com.sap.security.saml2.idp.session.ModifiableSPSession;
import com.sap.security.saml2.idp.session.SPSession;

public class SPSessionImpl implements SPSession, java.io.Serializable {

	private static final long serialVersionUID = 2824655611330461508L;
	private String sessionIndex = null;
	private String spName = null;
	private String subjectNameId = null;
	private String subjectNameIdFormat = null;
	private String subjectSPProvidedId = null;
	private Date creationDate = null;
	private Date modificationDate = null;
	private int status;
	private String statusDetails = null;
	private String sloRequestId = null;

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public ModifiableSPSession getModifiableSPSession() {
		return null;
	}

	@Override
	public Date getModificationDate() {
		return modificationDate;
	}

	@Override
	public String getSLORequestId() {
		return sloRequestId;
	}

	@Override
	public String getSPName() {
		return spName;
	}

	@Override
	public String getSessionIndex() {
		return sessionIndex;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public String getStatusDetails() {
		return statusDetails;
	}

	@Override
	public String getSubjectNameId() {
		return subjectNameId;
	}

	@Override
	public String getSubjectNameIdFormat() {
		return subjectNameIdFormat;
	}

	@Override
	public String getSubjectSPProvidedId() {
		return subjectSPProvidedId;
	}

	// hidden methods for updating SpSession in Modifiable IdPSession
	public void setCreationDate(Date date) {
		if (date != null) {
			this.creationDate = date;
		}
	}

	public void setSPName(String name) {
		if (name != null) {
			this.spName = name;
		}
	}

	public void setSessionIndex(String index) {
		if (index != null) {

			this.sessionIndex = index;
		}
	}

	public void setModificationDate(Date date) {
		if (date != null) {
			this.modificationDate = date;
		}
	}

	public void setSLORequestId(String id) {
		if (id != null) {

			this.sloRequestId = id;
		}
	}

	public void setStatus(int status) {
		if (status > 0) {
			this.status = status;
		}
	}

	public void setStatusDetails(String details) {
		if (details != null) {
			this.statusDetails = details;
		}
	}

	public void setSubjectNameId(String nameId) {
		if (nameId != null) {
			this.subjectNameId = nameId;
		}
	}

	public void setSubjectNameIdFormat(String format) {
		if (format != null) {
			this.subjectNameIdFormat = format;
		}
	}

	public void setSubjectSPProvidedId(String id) {
		if (id != null) {
			this.subjectSPProvidedId = id;
		}
	}

	public String getSpName() {
		return spName;
	}

	public void setSpName(String spName) {
		this.spName = spName;
	}

	public String getSloRequestId() {
		return sloRequestId;
	}

	public void setSloRequestId(String sloRequestId) {
		this.sloRequestId = sloRequestId;
	}
}
