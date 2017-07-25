package com.huoyun.idp.trial.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.huoyun.idp.trial.Trial;

@Repository
public interface TrialRepo extends PagingAndSortingRepository<Trial, Long>  {

}
