package com.huoyun.idp.oauth2.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.oauth2.entities.AccessTokenEntity;

@Repository
public interface AccessTokenRepository extends
		PagingAndSortingRepository<AccessTokenEntity, Long> {

	@Query("select t from AccessTokenEntity t where t.authenticationId = ?1")
	AccessTokenEntity findByAuthenticationId(String authenticationId);
	
	@Query("select t from AccessTokenEntity t where t.tokenId = ?1")
	AccessTokenEntity findByTokenId(String tokenId);
	
	@Query("select t from AccessTokenEntity t where t.clientId = ?1 and t.userName = ?2")
	List<AccessTokenEntity> findByClientIdAndUserName(String clientId,String userName);
	
	@Query("select t from AccessTokenEntity t where t.clientId = ?1")
	List<AccessTokenEntity> findByClientId(String clientId);

	@Transactional
	@Modifying
	@Query("delete from AccessTokenEntity t where t.tokenId = ?1")
	void removeByTokenId(String tokenId);
	
	@Transactional
	@Modifying
	@Query("delete from AccessTokenEntity t where t.refreshToken = ?1")
	void removeByRefreshToken(String refreshToken);
}
