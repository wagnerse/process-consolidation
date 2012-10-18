package org.bpel4chor.mergechoreography.test;

import java.util.ArrayList;
import java.util.List;

public class UtilTester {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("Eins");
		list.add("Zwei");
		list.add("Drei");
		list.add("Vier");
		list.add("Fünf");
		System.out.println(list.indexOf("Eins"));
		System.out.println(list.indexOf("Zwei"));
		System.out.println(list.indexOf("Drei"));
		System.out.println(list.indexOf("Vier"));
		System.out.println(list.indexOf("Fünf"));
		
	}
	
}
