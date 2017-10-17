package com.zopa.quote.repository;

import java.util.List;

import com.zopa.quote.domain.Lender;

public interface LenderPoolRepository {
	
	int total();
	void save(Lender lender);
	Lender find(int id);
	List<Lender> findAll();
	List<Lender> findAllSorted();
	void flush();

}
