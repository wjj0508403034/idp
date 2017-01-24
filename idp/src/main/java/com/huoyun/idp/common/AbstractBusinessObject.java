package com.huoyun.idp.common;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.joda.time.DateTime;

@MappedSuperclass
public abstract class AbstractBusinessObject implements BusinessObject,
		Serializable {

	private static final long serialVersionUID = -8045295128580323209L;

	@Version
	private Integer version;

	@Column(updatable = false)
	private DateTime createTime;

	@Column
	private DateTime updateTime;

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public DateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(DateTime createTime) {
		this.createTime = createTime;
	}

	public DateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(DateTime updateTime) {
		this.updateTime = updateTime;
	}
}
