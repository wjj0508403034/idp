package com.huoyun.idp.domains.impl;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.domains.Domain;
import com.huoyun.idp.domains.DomainService;
import com.huoyun.idp.domains.repository.DomainRepo;

public class DomainServiceImpl implements DomainService {

	private Facade facade;

	public DomainServiceImpl(Facade facade) {
		this.facade = facade;
	}

	@Override
	public Domain getDomainByName(String name) {
		return this.facade.getService(DomainRepo.class).getDomainByName(name);
	}

}
