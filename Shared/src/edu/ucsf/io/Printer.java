package edu.ucsf.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Printer{

	public static void printStringArray(ArrayList<String> lst1) {
		System.out.println("---------------------");
		for(String s:lst1) {
			System.out.println(s);
		}
		System.out.println("---------------------");
	}
	
	public static void print(HashMap<String,String> map1) {
		System.out.println("---------------------");
		for(String s:map1.keySet()) {
			System.out.println(s + "," + map1.get(s));
		}
		System.out.println("---------------------");
	}
		
	public static void printStringSet(HashSet<String> set1) {
		System.out.println("---------------------");
		for(String s:set1) {
			System.out.println(s);
		}
		System.out.println("---------------------");
	}

	public static void printDoubleArray(ArrayList<Double> lst1) {
		System.out.println("---------------------");
		for(Double d:lst1) {
			System.out.println(d);
		}
		System.out.println("---------------------");
	}
}
