package com.huoyun.idp.domains.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.domains.Domain;

@Repository
public interface DomainRepo extends PagingAndSortingRepository<Domain, Long>{

	@Query("select t from Domain t where t.name = ?1")
	Domain getDomainByName(String name);
}
