package edu.sunysb.perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Bigram {

	ArrayList<HashMap<String, Integer>> positiveTrainingSet = new ArrayList<HashMap<String, Integer>>();
	ArrayList<HashMap<String, Integer>> negativeTrainingSet = new ArrayList<HashMap<String, Integer>>();
	ArrayList<HashMap<String, Integer>> positiveTestingSet = new ArrayList<HashMap<String, Integer>>();
	ArrayList<HashMap<String, Integer>> negativeTestingSet = new ArrayList<HashMap<String, Integer>>();
	int totalPosWords = 0;
	int totalNegWords = 0;
	static double  learningRate = 1;
	static int MIN_ITR = 5;
	HashMap<String, Count> fullMap = new HashMap<String, Count>();
	HashMap<String, Count> fullMapWithUnknown = new HashMap<String, Count>();
	HashMap<String, Probability> probWithSmoothing = new HashMap<String, Probability>();
	HashMap<String, Double> weightMap = new HashMap<String, Double>();
	public final boolean COUNTBASED = false;

	public static void main(String[] args) throws IOException {

		double learningRates[] = {1,0.7};
		int min_itr[] = {1,5,10};
		for(double lr:learningRates) {
			learningRate = lr;
			for(int mi:min_itr) {
				MIN_ITR = mi;
				System.out.println("-------------------------------------------------------------------------");
				System.out.println("Perceptron with bigram(presence), learningRates: " + learningRate + ", iterations: " + MIN_ITR);
				do_work();
				System.out.println("-------------------------------------------------------------------------");
			}
		}
		// File dir = new File("outputs");
		// for(File file: dir.listFiles()) file.delete();

	}

	public static void do_work() {
		String[] folders = { "txt_sentoken\\pos", "txt_sentoken\\neg" };
		double avgSuccess = 0;
		for (int i = 0; i < 5; i++) {
			Bigram biigram = new Bigram();
			int start = i * 200;
			int end = start + 199;
			System.out.println("\nTest Data from: " + start + " - " + end);
			biigram.directoryReader(folders[0], folders[1], start, end);

			// int posAccuracy=unigram.doClassify(folders[0], true, start,
			// end);//, unigram.positiveTestingSet);
			// int negAccuracy=unigram.doClassify(folders[1], false, start,
			// end);//, unigram.negativeTestingSet);

			int positiveSuccess = biigram.doClassify(folders[0], true, start,
					end);
			int negSuccess = biigram.doClassify(folders[1], false, start, end);
			int totalTest = end - start + 1;
			avgSuccess += ((negSuccess + positiveSuccess) * 100.0) / (totalTest * 2);
			System.out.println("Positives=" + positiveSuccess
					+ ", percent success: " + (positiveSuccess * 100.0)
					/ totalTest);
			System.out.println("Negatives=" + negSuccess
					+ ", percent success: " + (negSuccess * 100.0) / totalTest);
			System.out.println("Total Success="
					+ (negSuccess + positiveSuccess) + ", percent success: "
					+ ((negSuccess + positiveSuccess) * 100.0)
					/ (totalTest * 2));


		}
		System.out.println("\nAverage Success Rate: " + avgSuccess/5);
	}

	public int doClassify(String dirName, boolean isPositive, int start, int end) {// ,
		// ArrayList<HashMap<String, Integer>> testingSet) {
		File dirPath = new File(dirName);
		int numPos = 0;
		int numNeg = 0;
		// if (dirPath.isDirectory()) {
		File[] fileList = dirPath.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (i >= start && i <= end) {
				File child = fileList[i];
				HashMap<String, Integer> features = new HashMap<String, Integer>();
				String fileContents = Helper.fileReader(child);
				String[] wordList = fileContents.split("\\s");
				for (int j = 0; j < wordList.length - 1; j++) {
					String word = wordList[j] + " " + wordList[j + 1];
					if (COUNTBASED) {
						if (features.containsKey(word)) {
							features.put(word, features.get(word) + 1);
						} else {
							features.put(word, 1);
						}
					} else {
						features.put(word, 1);
					}
				}
				boolean predictedClass = classifyFile(features);
				if (predictedClass == true) {
					numPos++;
				} else {
					numNeg++;
				}
				// testingSet.add(features);
			}
		}
		if (isPositive) {
			return numPos;
		} else {
			return numNeg;
		}
		// }
	}

	public boolean classifyFile(HashMap<String, Integer> featureMap) {
		int categoryNum = 0;
		Set<String> keySet = featureMap.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			int val = featureMap.get(key);
			if (weightMap.containsKey(key)) {
				categoryNum += weightMap.get(key) * val;
			}
		}
		if (categoryNum >= 0)
			return true;
		else
			return false;
	}

	public void directoryReader(String positiveFolder, String negativeFolder,
			int start, int end) {
		File posdir = new File(positiveFolder);
		File negdir = new File(negativeFolder);
		File[] posfileList = posdir.listFiles();
		File[] negfileList = negdir.listFiles();
		double err = 0;
		int itr = 0;
		do {
			err = 0;
			System.out.println("Iteration: " + itr++);
			for (int i = 0; i < posfileList.length; i++) {
				if (i >= start && i <= end) {
					continue;
				}
				File poschild = posfileList[i];
				File negchild = negfileList[i];
				err += Math
						.abs(fileReader(negchild, negativeTrainingSet, false));
				err += Math
						.abs(fileReader(poschild, positiveTrainingSet, true));

			}
		} while (err / (posfileList.length * 2 - 2 * (start - end + 1)) > 1 || itr < MIN_ITR);
	}

	public double fileReader(File file,
			ArrayList<HashMap<String, Integer>> trainingSet, boolean isPositive) {
		BufferedReader br = null;
		try {
			String line = null;
			HashMap<String, Integer> words = new HashMap<String, Integer>();
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			double categoryNum = 0;
			while ((line = br.readLine()) != null) {
				String parsedLine = Helper.cleanLine(line);
				String[] wordList = parsedLine.split("\\s");
				for (int i = 0; i < wordList.length - 1; i++) {
					String word = wordList[i] + " " + wordList[i + 1];
					if (COUNTBASED) {
						if (words.containsKey(word)) {
							words.put(word, words.get(word) + 1);
						} else {
							words.put(word, 1);
						}
					} else {
						words.put(word, 1);
					}
				}
			}
			Set<String> keySet = words.keySet();
			Iterator<String> iter = keySet.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				int val = words.get(key);
				if (weightMap.containsKey(key)) {
					categoryNum += weightMap.get(key) * val;
				}
			}
			double correction = learningRate
					* ((isPositive ? 1 : -1) - categoryNum / words.size());
			keySet = words.keySet();
			iter = keySet.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				int val = words.get(key);
				if (weightMap.containsKey(key)) {
					weightMap.put(key, weightMap.get(key) + correction * val);
				} else {
					weightMap.put(key, 0 + correction * val);
				}
			}
			br.close();
			fr.close();
			return (correction / learningRate);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean isCorrectlyClassified(boolean predicted, boolean actual) {
		// TODO Auto-generated method stub
		if (predicted == actual)
			return true;
		else
			return false;
	}

}
