package com.huoyun.idp.admin.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.tenant.TenantService;

@Controller
@RequestMapping("/admin/tenants")
public class TenantController {

	@Autowired
	private TenantService tenantService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void createTenant(@RequestBody CreateTenantParam createTenantParam) throws BusinessException {
		this.tenantService.createTenant(createTenantParam);
	}
}
