package edu.sunysb.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
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

	public static int countWords(HashMap<String, Count> map, boolean isPositive) {
		int numWords = 0;
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Count countObj = map.get(key);
			if (isPositive)
				numWords += countObj.posCount;
			else
				numWords += countObj.negCount;
		}
		return numWords;
	}

	public static <T> int countUniqueWords(HashMap<String, T> map) {
		return map.size();
	}

	public static HashMap<String, Probability> calcProbWithSmoothing(
			HashMap<String, Count> map) {
		HashMap<String, Probability> probWithSmoothing = new HashMap<String, Probability>();
		int posWords = Helper.countWords(map, true);
		int negWords = Helper.countWords(map, false);
		int totalVocab = Helper.countUniqueWords(map);
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Count countObj = map.get(key);
			
			//double posProb = Math.log((countObj.posCount + 1) * 1.0
				//	/ (posWords + totalVocab));
			//double negProb = Math.log((countObj.negCount + 1) * 1.0
				//	/ (negWords + totalVocab));
			double posProb = Math.log((countObj.posCount) * 1.0
					/ (posWords));
			double negProb = Math.log((countObj.negCount) * 1.0
					/ (negWords));
			
			Probability probObj = new Probability(posProb, negProb);
			probWithSmoothing.put(key, probObj);
		}
		return probWithSmoothing;

	}

	public static HashMap<String, Probability> calcProbWithSmoothing(
			HashMap<String, Count> bimap, HashMap<String, Count> unimap) {
		HashMap<String, Probability> probWithSmoothing = new HashMap<String, Probability>();
		int totalVocab = Helper.countUniqueWords(bimap);
		//System.out.println(totalVocab);
		Set<String> keySet = bimap.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			String firstWord=key.split(" ")[0];
			Count countObj = bimap.get(key);
			Count uniCountObj = unimap.get(firstWord);

//			double posProb = Math.log((countObj.posCount + 1) * 1.0
//					/ (uniCountObj.posCount + totalVocab));
//			double negProb = Math.log((countObj.negCount + 1) * 1.0
//					/ (uniCountObj.negCount + totalVocab));
			
			double posProb = Math.log((countObj.posCount) * 1.0
					/ (uniCountObj.posCount));
			double negProb = Math.log((countObj.negCount) * 1.0
					/ (uniCountObj.negCount));
			
			Probability probObj = new Probability(posProb, negProb);
			probWithSmoothing.put(key, probObj);
		}
		return probWithSmoothing;

	}


	public static HashMap<String, Count> buildCumulativeMap(
			HashMap<String, Integer> posMap, HashMap<String, Integer> negMap) {
		HashMap<String, Count> fullMap = new HashMap<String, Count>();
		// iterate the positive Map
		Set<String> keySet = posMap.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			int val = posMap.get(key);
			fullMap.put(key, new Count(val, 0));
		}

		// iterate the negative map
		keySet = negMap.keySet();
		iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			int val = negMap.get(key);
			if (fullMap.containsKey(key)) {
				Count countObj = fullMap.get(key);
				countObj.negCount = val;
				fullMap.put(key, countObj);
			} else
				fullMap.put(key, new Count(0, val));
		}
		return fullMap;
	}

	public static HashMap<String, Count> buildUnknownMap(
			HashMap<String, Count> map) {
		HashMap<String, Count> unknownMap = new HashMap<String, Count>();
		int posUnknownCount = 0;
		int negUnknownCount = 0;

		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Count countObj = map.get(key);
			if (countObj.isUnknown()) {
				posUnknownCount += countObj.posCount;
				negUnknownCount += countObj.negCount;
			} else {
				unknownMap.put(key, countObj);
			}
		}
		Count countObj = new Count(posUnknownCount, negUnknownCount);
		unknownMap.put(Helper.UNKNOWN, countObj);
		return unknownMap;
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

}
