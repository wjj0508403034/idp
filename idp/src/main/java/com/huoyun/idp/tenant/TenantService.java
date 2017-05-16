package com.huoyun.idp.tenant;

import com.huoyun.idp.admin.tenant.CreateTenantParam;
import com.huoyun.idp.exception.BusinessException;

public interface TenantService {

	void createTenant(CreateTenantParam tenantParam) throws BusinessException;

	Tenant getTenantByTenantCode(String tenantCode);
}
