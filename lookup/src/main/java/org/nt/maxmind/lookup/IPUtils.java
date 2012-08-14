package org.nt.maxmind.lookup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * http://dev.maxmind.com/geoip/csv
 * 
 * 
 */
public class IPUtils {

	public static final int convert(String ip) {
		/**
		 * Written after http://dev.maxmind.com/geoip/csv<br/>
		 * ( o1, o2, o3, o4 ) = <br/>
		 * address.split('.')<br/>
		 * <p/>
		 * integer_ip = ( 16777216 * o1 ) + ( 65536 * o2 ) + ( 256 * o3 ) + o4
		 * <br/>
		 */

		final String[] split = StringUtils.split(ip, '.');

		return (int) ((16777216 * intValueOf(split[0]))
				+ (65536 * intValueOf(split[1])) + (256 * intValueOf(split[2])) + intValueOf(split[3]));

	}

	public static final TreeMap<Integer, Integer> loadTreeMap(
			File compressedFile) throws IOException {

		final FileInputStream fin = new FileInputStream(compressedFile);
		final BufferedInputStream in = new BufferedInputStream(fin);

		final String fileName = compressedFile.getName();
		final CompressorInputStream cin;

		final BufferedReader reader;

		try {
			if (fileName.endsWith(".bz2")) {
				cin = new BZip2CompressorInputStream(in);
			} else if (fileName.endsWith(".gz")) {
				cin = new GzipCompressorInputStream(in);
			} else {
				throw new RuntimeException(
						fileName
								+ " compression format not supported, note that the file must be compressed");
			}

			reader = new BufferedReader(new InputStreamReader(cin));
			try {
				return loadTreeMap(reader);
			} finally {
				reader.close();
				cin.close();
			}

		} finally {
			in.close();
			fin.close();
		}

	}

	/**
	 * Line separated file. <br/>
	 * Line must be startIpNum,endIpNum,locId<br/>
	 * "7602176","7864319","16" <br/>
	 * "16777216","16777471","17" <br/>
	 * "16777472","16777727","24328" <br/>
	 * <p/>
	 * A typical maxmind file with location ids contains about 6 million
	 * entries.<br/>
	 * If we have per entry: key = integer, value = integer, overhead=
	 * 4bytes</br> Then for 6 millions entries we need 68 megabytes of memory to
	 * store the whole map.<br/>
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static final TreeMap<Integer, Integer> loadTreeMap(
			BufferedReader reader) throws IOException {

		TreeMap<Integer, Integer> treeMap = new TreeMap<Integer, Integer>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] split = StringUtils.split(line, ',');
			if (split != null && split.length == 3) {

				final int startIpNum = intValueOf(getNumber(split[0]));
				final int endIpNum = intValueOf(getNumber(split[1]));
				final int locId = intValueOf(getNumber(split[2]));

				// add start and end ip numbers with value locId
				treeMap.put(startIpNum, locId);
				treeMap.put(endIpNum, locId);

			}
		}

		return treeMap;

	}

	private static final String getNumber(String val) {
		return (val.startsWith("\"") || val.startsWith("'")) ? val.substring(1,
				val.length() - 1) : val;
	}

	/**
	 * Please see:
	 * http://stackoverflow.com/questions/1030479/most-efficient-way-
	 * of-converting-string-to-integer-in-java<br/>
	 * This method is the fastest String to int conversion method tested so far.
	 * 
	 * @param str
	 * @return
	 */
	private static final int intValueOf(String str) {
		int ival = 0, idx = 0, end;
		boolean sign = false;
		char ch;

		if (str == null
				|| (end = str.length()) == 0
				|| ((ch = str.charAt(0)) < '0' || ch > '9')
				&& (!(sign = ch == '-') || ++idx == end || ((ch = str
						.charAt(idx)) < '0' || ch > '9')))
			throw new NumberFormatException(str);

		for (;; ival *= 10) {
			ival += '0' - ch;
			if (++idx == end)
				return sign ? ival : -ival;
			if ((ch = str.charAt(idx)) < '0' || ch > '9')
				throw new NumberFormatException(str);
		}
	}

}
