package edu.sunysb.lm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Helper {

	public static final String UNKNOWN = "$$$$";

	public static String cleanLine(String line) {
		// System.out.println(line);
		String parsedLine = line.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll(
				" +", " ");
		// System.out.println(parsedLine);
		return parsedLine;
	}

	public static int countWords(HashMap<String, Integer> map) {
		int numWords = 0;
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			int val = map.get(key);
			numWords += val;
		}
		return numWords;
	}



	public static <T> int countUniqueWords(HashMap<String, T> map) {
		return map.size();
	}

	public static void createOutput(ArrayList<HashSet<String>> featureArray,
			HashMap<String, Count> featureMap,
			String outfile, String className, boolean presence) throws IOException {
		FileWriter fileWriter = new FileWriter(outfile, true);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		for(HashSet<String> featureSet:featureArray){
			bw.write(className + " ");
			ArrayList<Features> features = new ArrayList<Features>();
			for(String str : featureSet) {
				if(featureMap.containsKey(str)) {
					features.add(new Features(featureMap.get(str).index, presence?1:featureMap.get(str).count));

				}
			}
			Collections.sort(features);
			for(Features feature : features)
				bw.write(feature.index + ":" + feature.value + " ");
			bw.write("\n");
		}

		bw.flush();
		bw.close();
		fileWriter.close();

	}


	public static String fileReader(File file) {
		BufferedReader br = null;
		StringBuffer strBuf = new StringBuffer();
		try {
			String line = null;
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				String parsedLine = Helper.cleanLine(line);
				strBuf.append(parsedLine + " ");
			}
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}

	public static <T> void printMap(HashMap<String, T> map) {
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			T val = (T) map.get(key);
			System.out.println(key + " " + val);
		}
	}

	static class Features implements Comparable<Features>{
		Integer index;
		int value;
		@Override
		public int compareTo(Features o) {
			return index.compareTo(o.index);
		}
		public Features(Integer index, int value) {
			this.index = index;
			this.value = value;
		}

	}

}
