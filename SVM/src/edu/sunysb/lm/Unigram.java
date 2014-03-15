package edu.sunysb.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.sunysb.svm.svm_predict;
import edu.sunysb.svm.svm_train;

public class Unigram {

	ArrayList<HashMap<String, Integer>> positiveTrainingSet = new ArrayList<HashMap<String, Integer>>();
	ArrayList<HashMap<String, Integer>> negativeTrainingSet = new ArrayList<HashMap<String, Integer>>();
	ArrayList<HashMap<String, Integer>> positiveTestingSet = new ArrayList<HashMap<String, Integer>>();
	ArrayList<HashMap<String, Integer>> negativeTestingSet = new ArrayList<HashMap<String, Integer>>();
	int totalPosWords = 0;
	int totalNegWords = 0;
	HashMap<String, Count> fullMap = new HashMap<String, Count>();
	HashMap<String, Count> fullMapWithUnknown = new HashMap<String, Count>();
	HashMap<String, Probability> probWithSmoothing = new HashMap<String, Probability>();

	public static void main(String[] args) throws IOException {
		String[] folders = { "txt_sentoken\\pos", "txt_sentoken\\neg" };
		File dir = new File("outputs");
		String costs[] = {"1", "10", "20", "30", "50", "100"};
		for(String cost:costs) {
			System.out.println("------------------------------------------------");
			System.out.println("Cost: " + cost);
			for (File file : dir.listFiles())
				file.delete();
			for (int i = 0; i < 5; i++) {
				Unigram unigram = new Unigram();
				int start = i * 200;
				int end = start + 199;
				System.out.println("\nTest Data from: " + start + " - " + end);
				unigram.directoryReader(folders[0], true, start, end);
				unigram.directoryReader(folders[1], false, start, end);

				unigram.createTestData(folders[0], start, end,
						unigram.positiveTestingSet);
				unigram.createTestData(folders[1], start, end,
						unigram.negativeTestingSet);

				System.out.println("Calculating with Presence");
				Helper.createOutput(unigram.positiveTrainingSet, unigram.fullMap,
						"outputs/train_" + i, "+1", true);
				Helper.createOutput(unigram.negativeTrainingSet, unigram.fullMap,
						"outputs/train_" + i, "-1", true);

				Helper.createOutput(unigram.positiveTestingSet, unigram.fullMap,
						"outputs/test_" + i + ".t", "+1", true);
				Helper.createOutput(unigram.negativeTestingSet, unigram.fullMap,
						"outputs/test_" + i + ".t", "-1", true);

				svm_train svm_train = new svm_train();
				svm_train = new svm_train();
				svm_train.run(new String[] { "-q", "-t", "0", "-c", cost , "outputs/train_" + i,
						"outputs/train_" + i + ".model" });
				svm_predict.main(new String[] { "outputs/test_" + i + ".t",
						"outputs/train_" + i + ".model", "outputs/testout_" + i });


				for (File file : dir.listFiles())
					file.delete();
				System.out.println("\nCalculating with Frequency");
				Helper.createOutput(unigram.positiveTrainingSet, unigram.fullMap,
						"outputs/train_" + i, "+1", false);
				Helper.createOutput(unigram.negativeTrainingSet, unigram.fullMap,
						"outputs/train_" + i, "-1", false);

				Helper.createOutput(unigram.positiveTestingSet, unigram.fullMap,
						"outputs/test_" + i + ".t", "+1", false);
				Helper.createOutput(unigram.negativeTestingSet, unigram.fullMap,
						"outputs/test_" + i + ".t", "-1", false);

				svm_train = new svm_train();
				svm_train.run(new String[] {"-q", "-t", "0", "-c", cost , "outputs/train_" + i,
						"outputs/train_" + i + ".model" });
				svm_predict.main(new String[] { "outputs/test_" + i + ".t",
						"outputs/train_" + i + ".model", "outputs/testout_" + i });
			}
			System.out.println("------------------------------------------------");
		}
	}

	private void createTestData(String dirName, int start, int end,
			ArrayList<HashMap<String, Integer>> testingSet) {
		File dirPath = new File(dirName);
		if (dirPath.isDirectory()) {
			File[] fileList = dirPath.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (i >= start && i <= end) {
					File child = fileList[i];
					HashMap<String, Integer> features = new HashMap<String, Integer>();
					String fileContents = Helper.fileReader(child);
					String[] wordList = fileContents.split(" ");
					for (String word : wordList)
						if (features.containsKey(word))
							features.put(word, 1 + features.get(word));
						else
							features.put(word, 1);
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

	public void fileReader(File file,
			ArrayList<HashMap<String, Integer>> trainingSet) {
		BufferedReader br = null;
		try {
			String line = null;
			HashMap<String, Integer> words = new HashMap<String, Integer>();
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				String parsedLine = Helper.cleanLine(line);
				String[] wordList = parsedLine.split(" ");
				for (int i = 0; i < wordList.length; i++) {
					if (words.containsKey(wordList[i]))
						words.put(wordList[i], words.get(wordList[i]) + 1);
					else
						words.put(wordList[i], 1);
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
