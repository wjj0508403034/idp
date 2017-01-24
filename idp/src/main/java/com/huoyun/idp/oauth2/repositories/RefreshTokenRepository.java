package com.huoyun.idp.oauth2.repositories;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.oauth2.entities.RefreshTokenEntity;

@Repository
public interface RefreshTokenRepository extends
		PagingAndSortingRepository<RefreshTokenEntity, Long> {

	@Query("select t from RefreshTokenEntity t where t.tokenId = ?1")
	RefreshTokenEntity findByTokenId(String tokenId);
	
	@Transactional
	@Modifying
	@Query("delete from RefreshTokenEntity t where t.tokenId = ?1")
	void removeByTokenId(String tokenId);
}
