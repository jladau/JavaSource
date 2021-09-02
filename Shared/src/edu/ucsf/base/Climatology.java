package edu.ucsf.base;

import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Climatology object.
 * @author jladau
 */

public class Climatology implements Comparable<Climatology>{

	/**Start date**/
	public LocalDate timLB;
	
	/**End date**/
	public LocalDate timUB;
	
	/**Identifying date**/
	public LocalDate timValue;

	public Climatology(String sTime, String sTimeLB, String sTimeUB){
		this.timLB = new LocalDate(sTimeLB);
		this.timUB = new LocalDate(sTimeUB);
		this.timValue = new LocalDate(sTime);
	}

	public Climatology(LocalDate timValue, LocalDate timLB, LocalDate timUB){
		this.timLB = timLB;
		this.timUB = timUB;
		this.timValue = timValue;
	}
	
	public int compareTo(Climatology clm1){
		if(timValue.isBefore(clm1.timValue)){
			return -1;
		}else if(timValue.isAfter(clm1.timValue)){
			return 1;
		}else{
			return 0;
		}
	}
	
	public boolean equals(Object obj1){
		
		//clm1 = climatology object
		
		Climatology clm1;
		
		if(!(obj1 instanceof Climatology)){
			return false;
		}else{
			clm1 = (Climatology) obj1;
			if(!clm1.timLB.equals(timLB)){
				return false;
			}
			if(!clm1.timUB.equals(timUB)){
				return false;
			}
			if(!clm1.timValue.equals(timValue)){
				return false;
			}
			return true;
		}
	}
	
	public boolean isBefore(Climatology clm1){
		if(this.timValue.isBefore(clm1.timValue)){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isAfter(Climatology clm1){
		if(this.timValue.isAfter(clm1.timValue)){
			return true;
		}else{
			return false;
		}
	}
	
	public static int daysBetween(Climatology clm1, Climatology clm2){
		return Days.daysBetween(clm1.timValue, clm2.timValue).getDays();
	}
	
	public String toString(){
		return timValue + " [" + timLB + ", " + timUB + "]";
	}
}
