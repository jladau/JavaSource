package edu.ucsf.base;

import java.util.Iterator;

/**
 * Iterator for running process in parallel
 * @author jladau
 */

public class ClusterIterator implements Iterator<Boolean>{

	//iIteration = internal iteration counter
	//bInclude = true if iteration should be included; false otherwise
	//iTaskID = task ID
	//iTotalTasks = total tasks
	
	public boolean bInclude;
	public int iIteration;
	private int iTotalTasks;
	private int iTaskID;

	public ClusterIterator(int iTaskID, int iTotalTasks){
		iIteration=0;
		this.iTaskID = iTaskID;
		this.iTotalTasks = iTotalTasks;
	}
	
	public boolean hasNext(){
		return true;
	}
	
	public Boolean next(){
		iIteration++;
		
		if(iTaskID==-9999 || iTotalTasks==-9999){
			bInclude=true;
		}else{
			if((iIteration % iTotalTasks) == (iTaskID-1)){
				bInclude=true;
			}else{
				bInclude=false;
			}
		}
		return bInclude;
	}
	
	public void remove(){
	}
}
