package com.huoyun.idp.tenant.impl;

import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.huoyun.idp.admin.tenant.CreateTenantParam;
import com.huoyun.idp.common.Facade;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.servicetoken.ServiceToken;
import com.huoyun.idp.servicetoken.ServiceTokenService;
import com.huoyun.idp.tenant.Tenant;
import com.huoyun.idp.tenant.TenantErrorCodes;
import com.huoyun.idp.tenant.TenantService;
import com.huoyun.idp.tenant.repository.TenantRepo;
import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.User;

public class TenantServiceImpl implements TenantService {

	private static Logger LOGGER = LoggerFactory
			.getLogger(TenantServiceImpl.class);

	private final static int MAX = 9999;
	private final static int MIN = 1000;
	private Facade facade;

	public TenantServiceImpl(Facade facade) {
		this.facade = facade;
	}

	@Transactional(rollbackFor = BusinessException.class)
	@Override
	public void createTenant(CreateTenantParam tenantParam)
			throws BusinessException {
		Tenant tenant = new Tenant();
		tenant.setTenantCode(this.generateTenantCode());
		tenant.setName(tenantParam.getCompanyName());
		tenant.setEmail(tenantParam.getEmail());
		this.facade.getService(TenantRepo.class).save(tenant);
		User user = this.facade.getService(UserService.class).createUser(
				tenant, tenantParam);
		ServiceToken serviceToken = this.facade.getService(
				ServiceTokenService.class).getServiceToken("CRM");
		try {
			this.provisionCRM(serviceToken.getDbName(), user);
		} catch (Exception ex) {
			LOGGER.error("Create tenant failed", ex);
			throw new BusinessException(TenantErrorCodes.Create_Tenant_Failed);
		}
		
		this.facade.getService(UserService.class).sendUserInitPasswordMail(user);
	}

	@Override
	public Tenant getTenantByTenantCode(String tenantCode) {
		return this.facade.getService(TenantRepo.class).getTenantByTenantCode(
				tenantCode);
	}

	private String generateTenantCode() {
		Random random = new Random();
		int randomNum = random.nextInt(MAX) % (MAX - MIN + 1) + MIN;
		return DateTime.now().toString("yyyyMMddHHmmssSSS") + randomNum;
	}

	private void provisionCRM(String dbName, User user) {
		EntityManager entityManager = this.facade
				.getService(EntityManager.class);
		final String companySql = "INSERT INTO `%s`.`company` (`CREATETIME`,`UPDATETIME`, `COMPANYNAME` , `VERSION` , `TENANT_CODE` ) "
				+ "VALUES( NOW(), NOW(), :companyName, 1, :tenantCode)";
		Query companyQuery = entityManager.createNativeQuery(String.format(
				companySql, dbName));
		companyQuery.setParameter("companyName", user.getTenant().getName());
		companyQuery.setParameter("tenantCode", user.getTenant()
				.getTenantCode());
		companyQuery.executeUpdate();

		final String employeeSql = "INSERT INTO `%s`.`employee` (`CREATETIME`,`UPDATETIME`, `EMAIL` , `USERID` ,`USERNAME`, `VERSION` , `TENANT_CODE` ) "
				+ "VALUES( NOW(), NOW(), :email, :userId, :userName, 1, :tenantCode)";
		Query employeeQuery = entityManager.createNativeQuery(String.format(
				employeeSql, dbName));
		employeeQuery.setParameter("email", user.getEmail());
		employeeQuery.setParameter("userId", user.getId());
		employeeQuery.setParameter("userName", user.getUserName());
		employeeQuery.setParameter("tenantCode", user.getTenant()
				.getTenantCode());
		employeeQuery.executeUpdate();
	}

}
