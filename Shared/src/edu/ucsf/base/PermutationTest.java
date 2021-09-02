package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.*;
import org.junit.Test;

import edu.ucsf.base.Permutation;

/**
 * Unit test for Permutation class
 * @author jladau
 */

public class PermutationTest{

	/**Domain in ArrayList format.**/
	private ArrayList<String> lstDomain;
	
	/**Domain in array format.**/
	private String[] rgsDomain;
	
	/**Permutation object.**/
	private Permutation<String> per1;
	
	public PermutationTest(){
		rgsDomain = new String[]{"a","b","c","d"};
		lstDomain = new ArrayList<String>();
		for(int i=0;i<rgsDomain.length;i++){
			lstDomain.add(rgsDomain[i]);
		}
		initialize();
	}
	
	private void initialize(){
		per1 = new Permutation<String>(lstDomain);
	}
	
	public void loadRandomPermutation(){
		
	}
	
	@Test
	public void nextPermutation_AllPermutationsGenerated_PermutationsUnique(){
		
		//rgl2 = all permutations
		
		String rgs2[][];
		
		rgs2 = new String[24][4];
		for(int i=0;i<24;i++){
			per1.nextPermutation();
			for(int j=0;j<4;j++){
				rgs2[i][j]=per1.getImage(rgsDomain[j]);
			}
		}
		for(int i=1;i<24;i++){
			for(int k=0;k<i;k++){
				assertFalse(Arrays.equals(rgs2[i], rgs2[k]));
			}
		}
		
	}
	
	@Test
	public void hasNext_NextChecked_CorrectAnswer(){
		for(int i=0;i<23;i++){
			per1.nextPermutation();
			assertTrue(per1.hasNext());
		}
		per1.nextPermutation();
		assertFalse(per1.hasNext());
		initialize();
	}
	
	@Test
	public void loadPermutation_PermutationLoads_PermutationCorrect(){
		
		//per2 = permutation of indices
		//lst1 = list of integers
		//rgi1 = current image
		
		Permutation<Integer> per2;
		ArrayList<Integer> lst1;
		int rgi1[];
		
		lst1 = new ArrayList<Integer>();
		for(int i=0;i<4;i++){
			lst1.add(i);
		}
		per2 = new Permutation<Integer>(lst1);
		
		while(per2.hasNext()){
			per2.nextPermutation();
			rgi1 = new int[4];
			for(int i=0;i<4;i++){
				rgi1[i]=per2.getImage(i);
			}
			per1.loadPermutation(rgi1);
			for(int i=0;i<4;i++){
				assertEquals(rgsDomain[rgi1[i]],per1.getImage(rgsDomain[i]));
			}
		}
		initialize();
	}
	
	@Test
	public void getImage_ImageGotten_APermutation(){
		
		//set1 = set of image values for permutation
		
		HashSet<String> set1;
		
		for(int i=0;i<24;i++){
			per1.nextPermutation();
			set1 = new HashSet<String>();
			for(int k=0;k<4;k++){
				set1.add(per1.getImage(rgsDomain[k]));
			}
			assertEquals(4,set1.size());
		}
		initialize();
	}
	
	@Test
	public void toString_ConvertedToString_StringCorrect(){
		
		per1.loadPermutation(new int[]{3,2,1,0});
		assertEquals("a-->d,b-->c,c-->b,d-->a",per1.toString());
	}
}
