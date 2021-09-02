package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * This class implements a permutation function
 * @author jladau
 */

public class Permutation<T> {

	//lstDomain = domain of permutation
	//lstOrderedRange = range of permutation (ordered)
	//rnd1 = random number generator
	//a = internal ordering for iterating through all permutations (other single letter variables from NEXPER in Nijehuis and Wilf)
	//b1 = flag for whether iterator has been initialized
	//iTotalPer = total number of perumtations
	//b2 = flag for whether last permutation has been reached
	
	public ArrayList<T> lstDomain;
	private ArrayList<T> lstOrderedRange;
	private Random rnd1;
	private int a[];
	private boolean b1=false;
	private boolean b2=false;
	private int m;
	private int m1;
	private int n1;
	private int s;
	private int n;
	private int q;
	private int t;
	private int iTotalPer;
	
	/**
	 * Constructor
	 * @param lstDomain Domain for permutation
	 */
	public Permutation(ArrayList<T> lstDomain){
		this.lstDomain=lstDomain;
		rnd1 = new Random();
	}
	
	/**
	 * Loads a random permutation
	 */
	public void loadRandomPermutation(){
		lstOrderedRange=new ArrayList<T>(lstDomain);
		Collections.shuffle(lstOrderedRange);
	}
	
	/**
	 * Loads next permutation
	 */
	public void nextPermutation(){
		nextPer();
		if(a==null){
			lstOrderedRange=null;
		}else{
			lstOrderedRange=new ArrayList<T>(lstDomain.size());
			for(int i=0;i<lstDomain.size();i++){
				lstOrderedRange.add(lstDomain.get(a[i+1]-1));
			}
		}
	}
	
	/**
	 * Checks if another permutation is available
	 * @returns True if another permutation is available; false otherwise
	 */
	public boolean hasNext(){
		if(b2==true){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Loads next permutation sequence for iterating through all permutations (from Nijenhuis and Wilf, 1978, Combinatorial Algorithms)
	 */
	private void nextPer(){
		
		//i1 = value being swapped
		
		int i1;
		
		//checking if done
		if(b2==true){
			a=null;
			return;
		}
		
		//initializing
		if(b1==false){
			n=lstDomain.size();
			iTotalPer=1;
			for(int i=1;i<=n;i++){
				iTotalPer*=i;
			}
			a = new int[n+1];
			for(int i=1;i<=n;i++){
				a[i]=i;
			}
			m=1;
			b1=true;
			return;
		}else{
			n1=n;
			m1=m;
			s=n;
			do{
				q=m1%n1;
				t=m1%(2*n1);
				if(q!=0){
					break;
				}
				if(t==0){
					s--;
				}
				m1=m1/n1;
				n1--;
			}while(n>0);
			if(q==t){
				s-=q;
			}else{
				s+=q-n1;
			}
			i1=a[s];
			a[s]=a[s+1];
			a[s+1]=i1;
			m++;
			if(m==iTotalPer){
				b2=true;
			}	
		}
	}
	
	/**
	 * Loads permutation using specified order
	 * @param rgiOrder Ordering to use
	 */
	public void loadPermutation(int rgiOrder[]){
		lstOrderedRange=new ArrayList<T>(lstDomain.size());
		for(int i=0;i<lstDomain.size();i++){
			lstOrderedRange.add(lstDomain.get(rgiOrder[i]));
		}
	}
	
	/**
	 * Swaps a random pair of elements
	 * @return Pair elements that were swapped.
	 */
	public ArrayList<T> swapRandomElements(){
		
		//lstOut = pair of elements that were swapped
		//rgi1 = indices of elements that were swapped
		
		ArrayList<T> lstOut;
		int rgi1[];

		//loading indices of elements to be swapped
		rgi1 = new int[2];
		rgi1[0]=rnd1.nextInt(lstOrderedRange.size());
		rgi1[1]=rnd1.nextInt(lstOrderedRange.size());

		//loading values to be swapped
		lstOut = new ArrayList<T>(2);
		lstOut.add(lstDomain.get(rgi1[0]));
		lstOut.add(lstDomain.get(rgi1[1]));
		
		//swapping elements
		Collections.swap(lstOrderedRange, rgi1[0], rgi1[1]);
		
		//returning value
		return lstOut;
	}
	
	
	/**
	 * Applies permutation to get value
	 * @param dValue Value being permuted
	 * @result Image of value
	 */
	public T getImage(T Value){
		return lstOrderedRange.get(lstDomain.indexOf(Value));
	}
	
	/**
	 * Prints permutation
	 * @returns permutation
	 */
	public String toString(){
		
		//sblOut = output
		//b1 = flag for first entry
		
		StringBuilder sblOut;
		boolean b1=true;
		
		sblOut = new StringBuilder();
		for(T t:lstDomain){
			if(b1==false){
				sblOut.append(",");
			}else{
				b1=false;
			}
			sblOut.append(t + "-->" + getImage(t));
		}
		return sblOut.toString();
	}
}
