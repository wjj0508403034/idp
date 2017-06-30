package com.huoyun.idp.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.huoyun.idp.common.AbstractBusinessObject;
import com.huoyun.idp.tenant.Tenant;

@Entity
@Table
public class User extends AbstractBusinessObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String phone;

	@Column
	private String userName;

	@Column
	private boolean locked;

	@Column
	private String password;

	@Column
	private boolean active;

	@Column
	private String activeCode;

	@Column
	private DateTime activeDate;

	@Column
	private Role role;

	@ManyToOne
	@JoinColumn
	private Tenant tenant;

	@Override
	public Long getId() {
		return this.id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean isLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getActiveCode() {
		return activeCode;
	}

	public void setActiveCode(String activeCode) {
		this.activeCode = activeCode;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public boolean getLocked() {
		return locked;
	}

	public boolean getActive() {
		return active;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public DateTime getActiveDate() {
		return activeDate;
	}

	public void setActiveDate(DateTime activeDate) {
		this.activeDate = activeDate;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
