package com.huoyun.idp.oauth2.repositories;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.oauth2.entities.AuthorizeCodeEntity;

@Repository
public interface AuthorizeCodeRepository extends
		PagingAndSortingRepository<AuthorizeCodeEntity, Long> {

	@Transactional
	@Modifying
	@Query("delete from AuthorizeCodeEntity t where t.code = ?1")
	void remove(String code);

	@Query("select t from AuthorizeCodeEntity t where t.code = ?1")
	AuthorizeCodeEntity findByCode(String code);
}
