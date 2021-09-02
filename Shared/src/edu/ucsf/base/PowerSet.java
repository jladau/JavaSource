package edu.ucsf.base;

import java.util.ArrayList;

/**
 * Power set via Gray Code (Nijenhuis and Wilf 1978)
 * @author jladau
 *
 */

public class PowerSet{

	private int a[];
	private int k;
	private int t;
	private int j;
	private int n;
	private boolean bHasNext;
	private ArrayList<String> lst1;
	
	public PowerSet(ArrayList<String> lst1) {
		n = lst1.size();
		a = new int[n+1];
		k = 0;	
		bHasNext = true;
		this.lst1 = new ArrayList<String>(lst1);
	}
	
	public String[] next(){
		t = k % 2;
		j = 1;
		if(t!=0) {
			d();
		}else {
			c();
		}
		if(a[j]==0) {
			return new String[] {lst1.get(j-1),"remove"};
		}else {
			return new String[] {lst1.get(j-1),"add"};
		}
	}

	public ArrayList<String> currentSubset() {
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(n);
		for(int i=1;i<a.length;i++) {
			if(a[i]==1) {
				lstOut.add(lst1.get(i-1));
			}
		}
		return lstOut;
	}
	
	public boolean hasNext() {
		return bHasNext;
	}
	
	private void c(){
		a[j] = 1-a[j];
		k = k + 2*a[j] - 1;
		if(k==a[n]){
			bHasNext=false;
		}
	}
	
	private void d(){
		
		boolean bExit;
		
		bExit = false;
		do{
			j++;
			if(a[j-1]==1) {
				c();
				bExit = true;
			}
		}while(bExit==false);
	}
}