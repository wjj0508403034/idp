package com.huoyun.idp.tenant.impl;

import java.util.Random;

import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

import com.huoyun.idp.admin.tenant.CreateTenantParam;
import com.huoyun.idp.common.Facade;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.tenant.Tenant;
import com.huoyun.idp.tenant.TenantService;
import com.huoyun.idp.tenant.repository.TenantRepo;
import com.huoyun.idp.user.UserService;

public class TenantServiceImpl implements TenantService {

	private final static int MAX = 9999;
	private final static int MIN = 1000;
	private Facade facade;

	public TenantServiceImpl(Facade facade) {
		this.facade = facade;
	}

	@Transactional(rollbackFor = BusinessException.class)
	@Override
	public void createTenant(CreateTenantParam tenantParam) throws BusinessException {
		Tenant tenant = new Tenant();
		tenant.setTenantCode(this.generateTenantCode());
		tenant.setName(tenantParam.getCompanyName());
		tenant.setEmail(tenantParam.getEmail());
		this.facade.getService(TenantRepo.class).save(tenant);
		this.facade.getService(UserService.class).createUser(tenant, tenantParam);
	}

	@Override
	public Tenant getTenantByTenantCode(String tenantCode) {
		return this.facade.getService(TenantRepo.class).getTenantByTenantCode(tenantCode);
	}

	private String generateTenantCode() {
		Random random = new Random();
		int randomNum = random.nextInt(MAX) % (MAX - MIN + 1) + MIN;
		return DateTime.now().toString("yyyyMMddHHmmssSSS") + randomNum;
	}

}
