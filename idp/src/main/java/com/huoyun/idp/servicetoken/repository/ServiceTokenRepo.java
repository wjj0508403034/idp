package com.huoyun.idp.servicetoken.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.servicetoken.ServiceToken;

@Repository
public interface ServiceTokenRepo extends PagingAndSortingRepository<ServiceToken, Long> {

	@Query("select count(t) > 0 from ServiceToken t where t.name = ?1 and t.token = ?2")
	boolean isValid(String name, String token);
	
	@Query("select t from ServiceToken t where t.name = ?1")
	ServiceToken getServiceTokenByName(String name);
}
