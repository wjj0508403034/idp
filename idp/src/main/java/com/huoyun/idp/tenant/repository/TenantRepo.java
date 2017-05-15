package com.huoyun.idp.tenant.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.tenant.Tenant;

@Repository
public interface TenantRepo extends PagingAndSortingRepository<Tenant, Long> {

	@Query("select t from Tenant t where t.tenantCode = ?1")
	Tenant getTenantByTenantCode(String tenantCode);
}
