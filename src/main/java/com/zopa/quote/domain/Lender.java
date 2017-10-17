package com.zopa.quote.domain;

public class Lender implements Comparable<Lender>{
	
	private int id;
	private String name;
	private double rate;
	private double available;
	
	
	public Lender() {}
	public Lender(int id, String name, double rate, double available) {
		super();
		this.id = id;
		this.name = name;
		this.rate = rate;
		this.available = available;
	}

	
	public int getId() {
		return id;
	}
	
	
	public void setId(int id) {
		this.id = id;
	}
	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public double getRate() {
		return rate;
	}


	public void setRate(double rate) {
		this.rate = rate;
	}


	public double getAvailable() {
		return available;
	}


	public void setAvailable(double available) {
		this.available = available;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) return true;
		if ( !(obj instanceof Lender) ) return false;
		
		Lender o = (Lender) obj;
		return (id == o.getId()
				&& name.equals(o.getName())
				&& rate == o.getRate()
				&& available == o.getAvailable());
	}
	
	
	@Override
	public String toString() {
		return "[ ID: " + id + ", NAME: " + name + ", RATE: " + rate + ", AVAILABLE: " + available + " ]";
	}
	
	
	@Override
	public int compareTo(Lender other) {
		if(other == null)
			return 1;
		
		return this.rate > other.rate ? 1 : this.rate < other.rate ? -1 : 0;
	}
	
}

