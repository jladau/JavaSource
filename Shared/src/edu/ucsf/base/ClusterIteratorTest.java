package edu.ucsf.base;

import static org.junit.Assert.*;
import org.junit.Test;

import edu.ucsf.base.ClusterIterator;

/**
 * Iterator for running process in parallel
 * @author jladau
 */

public class ClusterIteratorTest{

	/**Cluster iterator object.**/
	private ClusterIterator itr1;
	
	public ClusterIteratorTest(){
		initialize();
	}
	
	private void initialize(){
		itr1 = new ClusterIterator(2,4);	
	}
	
	@Test
	public void next_IsRun_HasCorrectInclusion(){
		
		for(int i=1;i<=20;i++){
			itr1.next();
			if(i==1 || i==5 || i==9|| i==13 || i==17){
				assertTrue(itr1.bInclude);
			}else{
				assertFalse(itr1.bInclude);
			}
		}
		initialize();
	}
}
