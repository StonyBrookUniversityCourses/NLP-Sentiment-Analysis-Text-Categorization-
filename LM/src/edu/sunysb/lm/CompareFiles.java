package edu.sunysb.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Compare files wrongly classified
 * @author ankur
 *
 */
public class CompareFiles {

	static String [] pos = {
			"txt_sentoken\\pos\\cv050_11175.txt",
			"txt_sentoken\\pos\\cv082_11080.txt",
			"txt_sentoken\\pos\\cv185_28654.txt",
			"txt_sentoken\\pos\\cv214_12294.txt",
			"txt_sentoken\\pos\\cv242_10638.txt",
			"txt_sentoken\\pos\\cv282_6653.txt",
			"txt_sentoken\\pos\\cv420_28795.txt",
			"txt_sentoken\\pos\\cv555_23922.txt",
			"txt_sentoken\\pos\\cv603_17694.txt",
			"txt_sentoken\\pos\\cv842_5866.txt",
			"txt_sentoken\\pos\\cv876_9390.txt",
			"txt_sentoken\\pos\\cv932_13401.txt",
			"txt_sentoken\\pos\\cv952_25240.txt"
			};

	String[] neg = {
			"txt_sentoken\\neg\\cv835_20531.txt",
			"txt_sentoken\\neg\\cv838_25886.txt",
			"txt_sentoken\\neg\\cv845_15886.txt",
			"txt_sentoken\\neg\\cv851_21895.txt",
			"txt_sentoken\\neg\\cv888_25678.txt",
			"txt_sentoken\\neg\\cv941_10718.txt",
			"txt_sentoken\\neg\\cv972_26837.txt",
			"txt_sentoken\\neg\\cv010_29063.txt",
			"txt_sentoken\\neg\\cv104_19176.txt",
			"txt_sentoken\\neg\\cv106_18379.txt",
			"txt_sentoken\\neg\\cv142_23657.txt",
			"txt_sentoken\\neg\\cv237_20635.txt",
			"txt_sentoken\\neg\\cv262_13812.txt",
			"txt_sentoken\\neg\\cv273_28961.txt",
			"txt_sentoken\\neg\\cv333_9443.txt",
			"txt_sentoken\\neg\\cv381_21673.txt",
			"txt_sentoken\\neg\\cv392_12238.txt",
			"txt_sentoken\\neg\\cv427_11693.txt",
			"txt_sentoken\\neg\\cv466_20092.txt",
			"txt_sentoken\\neg\\cv475_22978.txt",
			"txt_sentoken\\neg\\cv519_16239.txt",
			"txt_sentoken\\neg\\cv524_24885.txt",
			"txt_sentoken\\neg\\cv571_29292.txt",
			"txt_sentoken\\neg\\cv697_12106.txt",
			"txt_sentoken\\neg\\cv708_28539.txt",
			"txt_sentoken\\neg\\cv746_10471.txt",
			"txt_sentoken\\neg\\cv761_13769.txt"
			};

	public static void main(String[] args) {
		try {
			compareFiles(pos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void compareFiles(String[] path) throws IOException {
		String first = path[0];
		HashSet<String> words = new HashSet<String>();
		getStringsUnigrams(first, words);
		for(int i=1; i< path.length;i++){
			compareStringsUnigrams(first, words);
		}
		//System.err.println(words);
		System.out.println(compareWithTrainingUnigram("txt_sentoken\\neg", words));

		words = new HashSet<String>();
		getStringsBigrams(first, words);
		for(int i=1; i< path.length;i++){
			compareStringsBigrams(first, words);
		}
		System.err.println();
		//System.err.println(words);
		System.out.println(compareWithTrainingBigram("txt_sentoken\\neg", words));

	}

	private static HashSet<String> compareWithTrainingUnigram(String dirPath, HashSet<String> words) throws IOException{
		File dir = new File(dirPath);
		HashSet<String> tempwords = new HashSet<String>();
		if (dir.isDirectory()) {
			File[] fileList = dir.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				FileReader fr = new FileReader(fileList[i]);
				BufferedReader br = new BufferedReader(fr);

				String line = null;
				while ((line = br.readLine()) != null) {
					String parsedLine = Helper.cleanLine(line);
					String[] wordList = parsedLine.split("\\s");
					for (int j = 0; j < wordList.length; j++) {
						if(words.contains(wordList[j]))
							tempwords.add(wordList[j]);
					}
				}
			}
		}
		return tempwords;
	}

	private static HashSet<String> compareWithTrainingBigram(String dirPath, HashSet<String> words) throws IOException{
		File dir = new File(dirPath);
		HashSet<String> tempwords = new HashSet<String>();
		if (dir.isDirectory()) {
			File[] fileList = dir.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				FileReader fr = new FileReader(fileList[i]);
				BufferedReader br = new BufferedReader(fr);

				String line = null;
				while ((line = br.readLine()) != null) {
					String parsedLine = Helper.cleanLine(line);
					String[] wordList = parsedLine.split("\\s");
					for (int j = 0; j < wordList.length - 1; j++) {
						String word = wordList[j] + " " + wordList[j + 1];
						if(words.contains(word))
							tempwords.add(word);
					}
				}
			}
		}
		return tempwords;
	}

	private static void compareStringsUnigrams(String file,
			HashSet<String> words) throws IOException {
		String line = null;
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		HashSet<String> tempwords = new HashSet<String>();
		while ((line = br.readLine()) != null) {
			String parsedLine = Helper.cleanLine(line);
			String[] wordList = parsedLine.split("\\s");
			for (int i = 0; i < wordList.length; i++) {
					tempwords.add(wordList[i]);
			}
		}
		words.retainAll(tempwords);
		br.close();
		fr.close();

	}

	private static void getStringsUnigrams(String file, HashSet<String> words) throws IOException {
		String line = null;
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		while ((line = br.readLine()) != null) {
			String parsedLine = Helper.cleanLine(line);
			String[] wordList = parsedLine.split("\\s");
			for (int i = 0; i < wordList.length; i++) {
					words.add(wordList[i]);
			}
		}
		br.close();
		fr.close();

	}

	private static void compareStringsBigrams(String file,
			HashSet<String> words) throws IOException {
		String line = null;
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		HashSet<String> tempwords = new HashSet<String>();
		while ((line = br.readLine()) != null) {
			String parsedLine = Helper.cleanLine(line);
			String[] wordList = parsedLine.split("\\s");
			for (int i = 0; i < wordList.length - 1; i++) {
					tempwords.add(wordList[i] + " " + wordList[i+1]);
			}
		}
		words.retainAll(tempwords);
		br.close();
		fr.close();

	}

	private static void getStringsBigrams(String file, HashSet<String> words) throws IOException {
		String line = null;
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		while ((line = br.readLine()) != null) {
			String parsedLine = Helper.cleanLine(line);
			String[] wordList = parsedLine.split("\\s");
			for (int i = 0; i < wordList.length - 1; i++) {
					words.add(wordList[i] + " " + wordList[i+1]);
			}
		}
		br.close();
		fr.close();

	}
}

