package org.nt.maxmind.test.lookup;

import java.io.File;
import java.util.TreeMap;

import org.junit.Test;
import org.nt.maxmind.lookup.IPUtils;

public class IPUtilsTest {

	@Test
	public void testTreeMap() throws Throwable{
		
		File file = new File("src/main/resources/data/GeoIPCity-134-Blocks.csv.bz2");
		
		TreeMap<Integer, Integer> map = IPUtils.loadTreeMap(file);
		
		System.out.println(map.size());
	}
	
}
