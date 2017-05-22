package com.huoyun.idp.user.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.tenant.Tenant;
import com.huoyun.idp.user.entity.User;

@Repository
public interface UserRepo extends PagingAndSortingRepository<User,Long> {

	@Query("select t from User t where t.email = ?1")
	User getUserByEmail(String email);
	
	@Query("select t from User t where t.id = ?1")
	User getUserById(Long userId);
	
	@Query("select t from User t where t.phone = ?1")
	User getUserByPhone(String phone);
	
	@Query("select count(t) > 0 from User t where t.email = ?1")
	boolean exists(String email);
	
	@Query("select t from User t where t.activeCode = ?1")
	User getUserByActiveCode(String activeCode);
	
	@Modifying
	@Query("delete from User t where t.email = ?1 and t.tenant = ?2")
	void deleteUserByEmailAndTenant(String email,Tenant tenant);
}
