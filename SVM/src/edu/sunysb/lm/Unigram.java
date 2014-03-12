package edu.sunysb.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.sunysb.svm.svm_predict;
import edu.sunysb.svm.svm_train;

public class Unigram {

	ArrayList<HashSet<String>> positiveTrainingSet = new ArrayList<HashSet<String>>();
	ArrayList<HashSet<String>> negativeTrainingSet = new ArrayList<HashSet<String>>();
	ArrayList<HashSet<String>> positiveTestingSet = new ArrayList<HashSet<String>>();
	ArrayList<HashSet<String>> negativeTestingSet = new ArrayList<HashSet<String>>();
	int totalPosWords = 0;
	int totalNegWords = 0;
	HashMap<String, Count> fullMap = new HashMap<String, Count>();
	HashMap<String, Count> fullMapWithUnknown = new HashMap<String, Count>();
	HashMap<String, Probability> probWithSmoothing = new HashMap<String, Probability>();

	public static void main(String[] args) throws IOException {
		String[] folders = { "txt_sentoken\\pos", "txt_sentoken\\neg" };
		File dir = new File("outputs");
		for(File file: dir.listFiles()) file.delete();
		for (int i = 0; i < 5; i++) {
			Unigram unigram = new Unigram();
			int start = i * 200;
			int end = start + 199;
			System.out.println("\nTest Data from: " + start + " - " + end);
			unigram.directoryReader(folders[0], true, start, end);
			unigram.directoryReader(folders[1], false, start, end);



			Helper.createOutput(unigram.positiveTrainingSet, unigram.fullMap,
					"outputs/train_" + i , "+1");
			Helper.createOutput(unigram.negativeTrainingSet, unigram.fullMap,
					"outputs/train_" + i , "-1");

			unigram.createTestData(folders[0], start, end, unigram.positiveTestingSet);
			unigram.createTestData(folders[1], start, end, unigram.negativeTestingSet);

			Helper.createOutput(unigram.positiveTestingSet, unigram.fullMap,
					"outputs/test_" + i  + ".t", "+1");
			Helper.createOutput(unigram.negativeTestingSet, unigram.fullMap,
					"outputs/test_" + i + ".t", "-1");

			svm_train svm_train = new svm_train();
			svm_train.run(new String[]{"-t", "0" ,"outputs/train_" + i, "outputs/train_" + i + ".model"});
			svm_predict.main(new String[]{"outputs/test_" + i + ".t", "outputs/train_" + i + ".model", "outputs/testout_" + i});

			svm_train.run(new String[]{"-s", "1", "-t", "0" ,"outputs/train_" + i, "outputs/train_" + i + ".model"});
			svm_predict.main(new String[]{"outputs/test_" + i + ".t", "outputs/train_" + i + ".model", "outputs/testout_" + i});

		}
	}

	private void createTestData(String dirName, int start, int end,
			ArrayList<HashSet<String>> testingSet) {
		File dirPath = new File(dirName);
		if (dirPath.isDirectory()) {
			File[] fileList = dirPath.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (i >= start && i <= end) {
					File child = fileList[i];
					HashSet<String> features = new HashSet<String>();
					String fileContents = Helper.fileReader(child);
					String[] wordList = fileContents.split(" ");
					for(String word:wordList)
						features.add(word);
					testingSet.add(features);
				}
			}
		}
	}



	public void directoryReader(String dirPath, boolean isPositiveDir,
			int start, int end) {
		File dir = new File(dirPath);
		if (dir.isDirectory()) {
			File[] fileList = dir.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (i >= start && i <= end) {
					continue;
				}
				File child = fileList[i];
				if (isPositiveDir) {
					fileReader(child, positiveTrainingSet);
				} else {
					fileReader(child, negativeTrainingSet);
				}

			}
		}
	}

	public void fileReader(File file, ArrayList<HashSet<String>> trainingSet) {
		BufferedReader br = null;
		try {
			String line = null;
			HashSet<String> words = new HashSet<String>();
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				String parsedLine = Helper.cleanLine(line);
				String[] wordList = parsedLine.split(" ");
				for (int i = 0; i < wordList.length; i++) {
					words.add(wordList[i]);
					if (fullMap.containsKey(wordList[i]))
						fullMap.get(wordList[i]).increamentCount();
					else {
						Count c = new Count(fullMap.size(), 1);
						fullMap.put(wordList[i], c);
					}
				}
			}
			trainingSet.add(words);
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
