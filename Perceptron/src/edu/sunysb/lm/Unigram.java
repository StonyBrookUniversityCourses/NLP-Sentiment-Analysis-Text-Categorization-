package edu.sunysb.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



public class Unigram {

	ArrayList<HashMap<String,Integer>> positiveTrainingSet = new ArrayList<HashMap<String,Integer>>();
	ArrayList<HashMap<String,Integer>> negativeTrainingSet = new ArrayList<HashMap<String,Integer>>();
	ArrayList<HashMap<String,Integer>> positiveTestingSet = new ArrayList<HashMap<String,Integer>>();
	ArrayList<HashMap<String,Integer>> negativeTestingSet = new ArrayList<HashMap<String,Integer>>();
	int totalPosWords = 0;
	int totalNegWords = 0;
	HashMap<String, Count> fullMap = new HashMap<String, Count>();
	HashMap<String, Count> fullMapWithUnknown = new HashMap<String, Count>();
	HashMap<String, Probability> probWithSmoothing = new HashMap<String, Probability>();
	HashMap<String,Integer> weightMap=new HashMap<String, Integer>();
	public final boolean COUNTBASED=false;
	public static void main(String[] args) throws IOException {
		String[] folders = { "txt_sentoken\\pos", "txt_sentoken\\neg" };
		//File dir = new File("outputs");
		//for(File file: dir.listFiles()) file.delete();
		for (int i = 0; i < 5; i++) {
			Unigram unigram = new Unigram();
			int start = i * 200;
			int end = start + 199;
			System.out.println("\nTest Data from: " + start + " - " + end);
			unigram.directoryReader(folders[1], false, start, end);
			unigram.directoryReader(folders[0], true, start, end);
			
			Helper.printMap(unigram.weightMap);
			//int posAccuracy=unigram.doClassify(folders[0], true, start, end);//, unigram.positiveTestingSet);
			//int negAccuracy=unigram.doClassify(folders[1], false, start, end);//, unigram.negativeTestingSet);

			int positiveSuccess = unigram.doClassify(folders[0], true, start, end);
			int negSuccess = unigram.doClassify(folders[1], false,start, end);
			int totalTest = end - start + 1;
			
			
			
			System.out.println("Positives=" + positiveSuccess
					+ ", percent success: " + (positiveSuccess * 100.0)
					/ totalTest);
			System.out.println("Negatives=" + negSuccess
					+ ", percent success: " + (negSuccess * 100.0) / totalTest);
			System.out.println("Total Success=" + (negSuccess + positiveSuccess)
					+ ", percent success: " + ((negSuccess + positiveSuccess) * 100.0) / (totalTest*2));
			
			
//			Helper.createOutput(unigram.positiveTrainingSet, unigram.fullMap,
//					"outputs/train_" + i , "+1", true);
//			Helper.createOutput(unigram.negativeTrainingSet, unigram.fullMap,
//					"outputs/train_" + i , "-1", true);
//
//			Helper.createOutput(unigram.positiveTestingSet, unigram.fullMap,
//					"outputs/test_" + i  + ".t", "+1", true);
//			Helper.createOutput(unigram.negativeTestingSet, unigram.fullMap,
//					"outputs/test_" + i + ".t", "-1", true);

			
			//for(File file: dir.listFiles()) file.delete();
			//System.out.println("\nCalculating with Frequency");
//			Helper.createOutput(unigram.positiveTrainingSet, unigram.fullMap,
//					"outputs/train_" + i , "+1", false);
//			Helper.createOutput(unigram.negativeTrainingSet, unigram.fullMap,
//					"outputs/train_" + i , "-1", false);
//
//			Helper.createOutput(unigram.positiveTestingSet, unigram.fullMap,
//					"outputs/test_" + i  + ".t", "+1", false);
//			Helper.createOutput(unigram.negativeTestingSet, unigram.fullMap,
//					"outputs/test_" + i + ".t", "-1", false);

		}
	}

	public int doClassify(String dirName, boolean isPositive, int start, int end){//,
		//ArrayList<HashMap<String, Integer>> testingSet) {
		File dirPath = new File(dirName);
		int numPos=0;
		int numNeg=0;
		//if (dirPath.isDirectory()) {
		File[] fileList = dirPath.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (i >= start && i <= end) {
				File child = fileList[i];
				HashMap<String, Integer> features = new HashMap<String, Integer>();
				String fileContents = Helper.fileReader(child);
				String[] wordList = fileContents.split(" ");
				for(String word:wordList){
					if(COUNTBASED){
						if(features.containsKey(word)){
							features.put(word,features.get(word)+1);
						}else{
							features.put(word, 1);
						}
					}else{
						features.put(word, 1);
					}
				}
				boolean predictedClass=classifyFile(features);
				if(predictedClass==true){
					numPos++;
				}else{
					numNeg++;
				}
				//testingSet.add(features);
			}
		}
		if(isPositive){
			return numPos;
		}else{
			return numNeg;
		}
		//}
	}



	public boolean classifyFile(HashMap<String, Integer> featureMap) {
		int categoryNum=0;
		Set<String> keySet = featureMap.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			int val = featureMap.get(key);
			if(weightMap.containsKey(key)){
				categoryNum+=weightMap.get(key)*val;
			}
			//else{
			//	categoryNum+=weightMap.get(key)*0;
			//}
		}	
		if(categoryNum>=0)
			return true;
		else
			return false;
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
					fileReader(child, positiveTrainingSet,isPositiveDir);
				} else {
					fileReader(child, negativeTrainingSet,isPositiveDir);
				}

			}
		}
	}

	public void fileReader(File file, ArrayList<HashMap<String,Integer>> trainingSet, boolean isPositive) {
		BufferedReader br = null;
		try {
			String line = null;
			HashMap<String,Integer> words = new HashMap<String,Integer>();
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			int categoryNum=0;
			while ((line = br.readLine()) != null) {
				String parsedLine = Helper.cleanLine(line);
				String[] wordList = parsedLine.split(" ");
				for (int i = 0; i < wordList.length; i++) {
					if(COUNTBASED){
						if(words.containsKey(wordList[i])){
							words.put(wordList[i],words.get(wordList[i])+1);
						}else{
							words.put(wordList[i], 1);
						}
					}else{
						words.put(wordList[i], 1);
					}
				}
			}
			Set<String> keySet = words.keySet();
			Iterator<String> iter = keySet.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				int val = words.get(key);
				if(weightMap.containsKey(key)){
					categoryNum+=weightMap.get(key)*val;
				}
				//else{
				//	categoryNum+=weightMap.get(key)*0;
				//}
			}	
			trainingSet.add(words);
			boolean isCorrect=true;
			if(categoryNum>=0){
				isCorrect=isCorrectlyClassified(true,isPositive);
				if(isCorrect==false){
					//subtract
					keySet = words.keySet();
					iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						int val = words.get(key);
						if(weightMap.containsKey(key)){
							weightMap.put(key, weightMap.get(key)-val);
						}else{
							weightMap.put(key, 0-val);
						}
					}	
				}
			}else{
				isCorrect=isCorrectlyClassified(false,isPositive);
				if(isCorrect==false){
					//add
					keySet = words.keySet();
					iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						int val = words.get(key);
						if(weightMap.containsKey(key)){
							weightMap.put(key, weightMap.get(key)+val);
						}else{
							weightMap.put(key, 0+val);
						}
					}	
				}
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean isCorrectlyClassified(boolean predicted, boolean actual) {
		// TODO Auto-generated method stub
		if(predicted==actual)
			return true;
		else
			return false;
	}

}
