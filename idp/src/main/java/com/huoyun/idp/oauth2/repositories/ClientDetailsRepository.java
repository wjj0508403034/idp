package com.huoyun.idp.oauth2.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.oauth2.entities.ClientDetailsEntity;

@Repository
public interface ClientDetailsRepository extends
		PagingAndSortingRepository<ClientDetailsEntity, Long> {
	
	@Query("select count(t) > 0 from ClientDetailsEntity t where t.name = ?1")
	boolean existsCheckBeforeCreate(String name);
	
	@Query("select t from ClientDetailsEntity t where t.clientId = ?1")
	ClientDetailsEntity findByClientId(String clientId);

}
