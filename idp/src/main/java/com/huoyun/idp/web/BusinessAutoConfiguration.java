package com.huoyun.idp.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.domains.DomainService;
import com.huoyun.idp.domains.impl.DomainServiceImpl;
import com.huoyun.idp.servicetoken.ServiceTokenService;
import com.huoyun.idp.servicetoken.impl.ServiceTokenServiceImpl;
import com.huoyun.idp.tenant.TenantService;
import com.huoyun.idp.tenant.impl.TenantServiceImpl;
import com.huoyun.idp.trial.TrialService;
import com.huoyun.idp.trial.impl.TrialServiceImpl;

@Configuration
public class BusinessAutoConfiguration {

	@Bean
	public ServiceTokenService serviceTokenService(Facade facade) {
		return new ServiceTokenServiceImpl(facade);
	}

	@Bean
	public TenantService tenantService(Facade facade) {
		return new TenantServiceImpl(facade);
	}

	@Bean
	public DomainService domianService(Facade facade) {
		return new DomainServiceImpl(facade);
	}
	
	@Bean
	public TrialService trialService(Facade facade){
		return new TrialServiceImpl(facade);
	}
}
