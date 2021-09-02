package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.HashSet;

public class County extends Region{

	/**Alpha value**/
	public double dAlphaCounty;
	
	public County(String sFips, int iStartTime, int iEndTime, ArrayList<Integer> lstTimes){
		super(sFips, iStartTime, iEndTime, lstTimes);
	}
	
	public County(String sFips, int iStartTime, int iEndTime, HashSet<Integer> setTimes){
		super(sFips, iStartTime, iEndTime, setTimes);
	}
}