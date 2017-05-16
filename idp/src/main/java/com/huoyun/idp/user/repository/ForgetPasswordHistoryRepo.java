package com.huoyun.idp.user.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.user.entity.ForgetPasswordHistory;
import com.huoyun.idp.user.entity.User;

@Repository
public interface ForgetPasswordHistoryRepo extends PagingAndSortingRepository<ForgetPasswordHistory, Long> {

	@Query("select t from ForgetPasswordHistory t where t.requestCode = ?1")
	ForgetPasswordHistory getHistoryByRequestCode(String requestCode);

	@Modifying
	@Query("update from ForgetPasswordHistory t set t.active = false where t.user = ?1")
	void deactiveRequests(User user);
}
