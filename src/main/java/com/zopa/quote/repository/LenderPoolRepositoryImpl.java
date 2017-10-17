package com.zopa.quote.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.zopa.quote.domain.Lender;

@Repository
public class LenderPoolRepositoryImpl implements LenderPoolRepository {
	
	//Mocking a persisting repository
	private Map<Integer, Lender> poolLenderRepo;
	
	
	public LenderPoolRepositoryImpl() {
		//Initializing persisting repository
		poolLenderRepo = new HashMap<Integer, Lender>();
	}
	
	
	/**
	 * Returns total number of lenders.  
	 *
	 * @return      total number of lenders in repository.
	 */
	@Override
	public int total() {
		return poolLenderRepo.size();
	}
	
	
	/**
	 * Method inserts a new Lender domain object to persistence repository.  
	 *
	 */
	@Override
	public void save(Lender lender) {
		poolLenderRepo.put(lender.getId(), lender);
	}
	
	
	/**
	 * Method returns a Lender domain object from persistence repository based on ID provided.  
	 *
	 * @return      lender with ID provided.
	 */
	@Override
	public Lender find(int id) {
		return poolLenderRepo.get(id);
	}

	
	/**
	 * Method returns a list of Lender domain objects from persistence repository.  
	 *
	 * @return      full lender list found in repository.
	 */
	@Override
	public List<Lender> findAll() {
		return new ArrayList<Lender>(poolLenderRepo.values());
	}
	
	
	/**
	 * Method returns a sorted list of Lender domain objects from persistence repository, order based on rate property.  
	 *
	 * @return      full sorted lender list by rate found in repository.
	 */
	@Override
	public List<Lender> findAllSorted() {
		List<Lender> poolLenderList = new ArrayList<Lender>(poolLenderRepo.values());
		Collections.sort(poolLenderList);
		return poolLenderList;
	}
	
	
	/**
	 * Method removes all objects from persistence repository.  
	 *
	 */
	@Override
	public void flush() {
		poolLenderRepo.clear();
	}
	
}
