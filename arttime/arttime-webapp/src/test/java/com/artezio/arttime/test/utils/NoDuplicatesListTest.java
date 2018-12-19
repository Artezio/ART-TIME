package com.artezio.arttime.test.utils;

import static org.junit.Assert.*;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import junitx.framework.ListAssert;

import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.utils.NoDuplicatesList;

public class NoDuplicatesListTest {
	private NoDuplicatesList<String> list;
	
	@Before
	public void setUp() {
		list = new NoDuplicatesList<String>();
	}

	@Test
	public void test() {
		for (int i = 0; i < 2; i++)
			System.out.println(Arrays.toString(closest("456899 50 11992 176 272293 163 389128 96 290193 85 52")[i]));
	}

	public int[][] closest(String string) {
		List<Integer> weights = Arrays.stream(string.split(" "))
				.map(str -> str.chars().map(integer -> integer - 48).sum())
				.collect(Collectors.toList());
		int minWeight1 = weights.get(0) + 0;
		int minWeight2 = weights.get(1) + 1;
		int minIndex1 = 0;
		int minIndex2 = 1;
		int minSum = Math.abs(minWeight1 - minWeight2) + minWeight1 + minWeight2;
		for (int i = 0; i < weights.size(); i++) {
			int weightI = weights.get(i);
			for (int j = 0; j < weights.size(); j++) {
				int weightJ = weights.get(j);
				int sum = weightJ + j + weightI + i + Math.abs(weightI - weightJ);
				if (sum < minSum) {
					minIndex1 = i;
					minIndex2 = j;
					minSum = sum;
				}
			}
		}
		return new int[][] {{minWeight1, minIndex1, string.indexOf(weights.indexOf(minWeight1))}, {}};
	}

	@Test
	public void testConstructor() {
		List<String> collection = Arrays.asList("a", "b", "a", "c");
		List<String> expected = Arrays.asList("a", "b", "c");
		
		NoDuplicatesList<String> actual = new NoDuplicatesList<>(collection);
		
		ListAssert.assertEquals(expected, actual);
	}
	
	@Test
	public void testAdd() {
		assertTrue(list.add("a"));
		assertFalse(list.add("a"));
		assertTrue(list.add("b"));
	}
	
	@Test
	public void testAddByIndex() {
		List<String> collection = Arrays.asList("a", "b", "c");
		list = new NoDuplicatesList<>(collection);
		List<String> expected = Arrays.asList("a", "b", "d", "c");
		
		list.add(2, "a");
		list.add(3, "d");
		
		ListAssert.assertEquals(expected, list);
	}
	
	@Test
	public void testAddAll() {		
		List<String> collection = Arrays.asList("a", "b", "c");
		list = new NoDuplicatesList<>(collection);
		List<String> expected = Arrays.asList("a", "b", "c", "d");
		
		boolean actual = list.addAll(Arrays.asList("a", "d"));
		
		assertTrue(actual);
		ListAssert.assertEquals(expected, list);
	}
	
	@Test
	public void testAddAll_ifFalse() {		
		List<String> collection = Arrays.asList("a", "b", "c");
		list = new NoDuplicatesList<>(collection);
		List<String> expected = Arrays.asList("a", "b", "c");
		
		boolean actual = list.addAll(Arrays.asList("a", "c"));
		
		assertFalse(actual);
		ListAssert.assertEquals(expected, list);
	}
	
	@Test
	public void testAddAllByIndex() {		
		List<String> collection = Arrays.asList("a", "b", "c");
		list = new NoDuplicatesList<>(collection);
		List<String> expected = Arrays.asList("a", "d", "b", "c");
		
		boolean actual = list.addAll(1, Arrays.asList("a", "d"));
		
		assertTrue(actual);
		ListAssert.assertEquals(expected, list);
	}
	
	@Test
	public void testAddAllByIndex_ifFalse() {		
		List<String> collection = Arrays.asList("a", "b", "c");
		list = new NoDuplicatesList<>(collection);
		List<String> expected = Arrays.asList("a", "b", "c");
		
		boolean actual = list.addAll(1, Arrays.asList("a", "c"));
		
		assertFalse(actual);
		ListAssert.assertEquals(expected, list);
	}
}
