package com.huoyun.idp.email;

import com.huoyun.idp.exception.BusinessException;

public interface EmailService {

	void send(String to, EmailTemplate template) throws BusinessException;
}
