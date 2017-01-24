package com.huoyun.idp.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.user.entity.User;

@Repository
public interface UserRepo extends PagingAndSortingRepository<User,Long> {

	@Query("select t from User t where t.email = ?1")
	User getUserByEmail(String email);
	
	@Query("select t from User t where t.phone = ?1")
	User getUserByPhone(String phone);
}
