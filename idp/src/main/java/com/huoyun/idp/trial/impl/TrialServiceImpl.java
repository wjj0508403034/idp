package com.huoyun.idp.trial.impl;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.controller.TrialParam;
import com.huoyun.idp.trial.Trial;
import com.huoyun.idp.trial.TrialService;
import com.huoyun.idp.trial.repository.TrialRepo;

public class TrialServiceImpl implements TrialService {

	private Facade facade;
	
	public TrialServiceImpl(Facade facade) {
		this.facade = facade;
	}

	@Override
	public void create(TrialParam trialParam) {
		Trial trial = new Trial();
		trial.setCompanyName(trialParam.getCompanyName());
		trial.setContactPerson(trialParam.getContactPerson());
		trial.setEmail(trialParam.getEmail());
		trial.setPhone(trialParam.getPhone());
		this.facade.getService(TrialRepo.class).save(trial);
	}

}
