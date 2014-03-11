package edu.sunysb.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.sun.xml.internal.bind.v2.runtime.RuntimeUtil.ToStringAdapter;


public class Unigram {
	
	HashMap<String, Integer> posMap=new HashMap<String, Integer>();
	HashMap<String, Integer> negMap=new HashMap<String, Integer>();
	HashMap<String,Double> posProb=new HashMap<String,Double>();
	HashMap<String,Double> negProb=new HashMap<String,Double>();
	int totalPosWords=0;
	int totalNegWords=0;
	HashMap<String,CountObj> fullMap=new HashMap<String,CountObj>();
	HashMap<String,CountObj> fullMapWithUnknown=new HashMap<String,CountObj>();
	HashMap<String,ProbObj> probWithSmoothing=new HashMap<String,ProbObj>();
	
	public static final String UNKNOWN="$$$$";
	//args[0] is positive review directory
	//args[1] is negative review directory
	public static void main(String[] args) {
		Unigram unigram=new Unigram();
		int start=80;
		int end=99;
		
		unigram.directoryReader(args[0], true, start,end);
		unigram.directoryReader(args[1], false,start,end);
		unigram.fullMap=unigram.buildCumulativeMap(unigram.posMap, unigram.negMap);
		unigram.fullMapWithUnknown=unigram.buildUnknownMap(unigram.fullMap);
		unigram.probWithSmoothing=unigram.calcProbWithSmoothing(unigram.fullMapWithUnknown);
		
		
		int numPos=unigram.doClassify(args[0], start,end,true);
		int numNeg=unigram.doClassify(args[1], start,end, false);
		System.out.println("Positives="+numPos);
		System.out.println("Negatives="+numNeg);
	}
	

	public boolean classifyFile(File file){
		String fileContents=fileReader(file);
		String[] wordList=fileContents.split(" ");
		double posProb=0;
		double negProb=0;
		for(int i=0;i<wordList.length;i++){
			if(probWithSmoothing.containsKey(wordList[i])){
				ProbObj probObj=probWithSmoothing.get(wordList[i]);
				negProb+=probObj.negProb;
				posProb+=probObj.posProb;
			}
			else{
				ProbObj probObj=probWithSmoothing.get(UNKNOWN);
				negProb+=probObj.negProb;
				posProb+=probObj.posProb;
			}
		}
		if(posProb<negProb){
			return true;
		}else{
			return false;
		}
	}
	
	public int doClassify(String dirName,int start,int end, boolean forPositive){
		File dirPath = new File(dirName);
		int numPos=0;
		int numNeg=0;
		if(dirPath.isDirectory()){	
			File[] fileList=dirPath.listFiles();
			for(int i=0;i<fileList.length;i++) {
				if(i>=start && i<=end){
					File child=fileList[i];
					if(classifyFile(child)){
						numPos++;
					}else{
						numNeg++;
					}
				}
			}
		}
		if(forPositive){
		return numPos;
		}else{
			return numNeg;
		}
	}
	
	
	public String fileReader(File file){
		BufferedReader br=null;
		StringBuffer strBuf=new StringBuffer();
		try {
			String line=null;
			br=new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				String parsedLine=cleanLine(line);
				strBuf.append(parsedLine+" ");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}
	
	public void directoryReader(String dirPath, boolean isPositiveDir, int start, int end){
		File dir = new File(dirPath);
		if(dir.isDirectory()){	
			File[] fileList=dir.listFiles();
			for(int i=0;i<fileList.length;i++) {
				if(i>=start && i<=end){
					continue;
				}
				File child=fileList[i];
				if(isPositiveDir){
					fileReader(child,posMap);
				}else{
					fileReader(child,negMap);
				}
				
			}
		}
	}
	
	public <T> void printMap(HashMap<String,T> map){
		Set<String> keySet=map.keySet();
		Iterator<String> iter=keySet.iterator();
		while(iter.hasNext()){
			String key=iter.next();
			T val=(T)map.get(key);
			System.out.println(key+" "+val);
		}
	}
	
	public void fileReader(File file, HashMap<String,Integer> map){
		
		BufferedReader br=null;
		try {
			String line=null;
			br=new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				String parsedLine=cleanLine(line);
				String[] wordList=parsedLine.split(" ");
				for(int i=0;i<wordList.length;i++){
					if(map.containsKey(wordList[i]))
						map.put(wordList[i],map.get(wordList[i])+1);
					else
						map.put(wordList[i], 1);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void calcProb(HashMap<String,Integer> map){
		int totalWords=countWords(map);
		Set<String> keySet=map.keySet();
		Iterator<String> iter=keySet.iterator();
		while(iter.hasNext()){
			String key=iter.next();
			int val=map.get(key);
			double prob=(val*1.0/totalWords);
			posProb.put(key, prob);
		}
	}
	
	public String cleanLine(String line){
		//System.out.println(line);
		String parsedLine=line.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll(" +", " ");
		//System.out.println(parsedLine);
		return parsedLine;
	}
	
	public int countWords(HashMap<String,Integer> map){
		int numWords=0;
		Set<String> keySet=map.keySet();
		Iterator<String> iter=keySet.iterator();
		while(iter.hasNext()){
			String key=iter.next();
			int val=map.get(key);
			numWords+=val;
		}
		return numWords;
	}
	
	public int countWords(HashMap<String,CountObj> map, boolean isPositive){
		int numWords=0;
		Set<String> keySet=map.keySet();
		Iterator<String> iter=keySet.iterator();
		while(iter.hasNext()){
			String key=iter.next();
			CountObj countObj=map.get(key);
			if(isPositive)
				numWords+=countObj.posCount;
			else
				numWords+=countObj.negCount;
		}
		return numWords;
	}
	
	public <T> int countUniqueWords(HashMap<String,T> map){
		return map.keySet().size();
	}
	
	public HashMap<String,CountObj> buildCumulativeMap(HashMap<String,Integer> posMap, HashMap<String,Integer> negMap){
		HashMap<String,CountObj> fullMap=new HashMap<String,CountObj>();
		//iterate the positive Map
		Set<String> keySet=posMap.keySet();
		Iterator<String> iter=keySet.iterator();
		while(iter.hasNext()){
			String key=iter.next();
			int val=posMap.get(key);
			fullMap.put(key, new CountObj(val,0));
		}
		
		//iterate the negative map
		keySet=negMap.keySet();
		iter=keySet.iterator();
		while(iter.hasNext()){
			String key=iter.next();
			int val=negMap.get(key);
			if(fullMap.containsKey(key)){
				CountObj countObj=fullMap.get(key);
				countObj.negCount=val;
				fullMap.put(key, countObj);
			}
			else
				fullMap.put(key, new CountObj(0,val));
		}
		return fullMap;
	}
	
	public HashMap<String, CountObj> buildUnknownMap(HashMap<String, CountObj> map) {
		HashMap<String,CountObj> unknownMap=new HashMap<String,CountObj>();
		int posUnknownCount=0;
		int negUnknownCount=0;
		
		Set<String> keySet=map.keySet();
		Iterator<String> iter=keySet.iterator();
		while(iter.hasNext()){
			String key=iter.next();
			CountObj countObj=map.get(key);
			if(countObj.isUnknown()){
				posUnknownCount+=countObj.posCount;
				negUnknownCount+=countObj.negCount;
			}else{
				unknownMap.put(key, countObj);
			}
		}
		CountObj countObj=new CountObj(posUnknownCount, negUnknownCount);
		unknownMap.put(UNKNOWN, countObj);
		return unknownMap;
	}
	
	public HashMap<String, ProbObj> calcProbWithSmoothing(HashMap<String, CountObj> map) {
		HashMap<String, ProbObj> probWithSmoothing=new HashMap<String,ProbObj>();
		int posWords=countWords(fullMapWithUnknown,true);
		int negWords=countWords(fullMapWithUnknown,false);
		int totalVocab=countUniqueWords(map);
		Set<String> keySet=map.keySet();
		Iterator<String> iter=keySet.iterator();
		while(iter.hasNext()){
			String key=iter.next();
			if(key.equals(UNKNOWN)){
				CountObj countObj=map.get(key);
				double posProb=Math.log((countObj.posCount+1)*1.0/(posWords+negWords+totalVocab));
				double negProb=Math.log((countObj.negCount+1)*1.0/(negWords+negWords+totalVocab));
				ProbObj probObj=new ProbObj(posProb, negProb);
				probWithSmoothing.put(key, probObj);
			}else{
				CountObj countObj=map.get(key);
				double posProb=Math.log((countObj.posCount+1)*1.0/(posWords+totalVocab));
				double negProb=Math.log((countObj.negCount+1)*1.0/(negWords+totalVocab));
				ProbObj probObj=new ProbObj(posProb, negProb);
				probWithSmoothing.put(key, probObj);
			}
		}
		return probWithSmoothing;
		
	}


}

class CountObj{
	int posCount;
	int negCount;
	CountObj(int posCount,int negCount){
		this.posCount=posCount;
		this.negCount=negCount;
	}
	
	@Override
	public String toString() {
		return posCount+" "+negCount;
		
	}
	
	public boolean isUnknown(){
		if((posCount+negCount)>1){
			return false;
		}else{
			return true;
		}
	}
}

class ProbObj{
	double posProb;
	double negProb;
	ProbObj(double posProb,double negProb){
		this.posProb=posProb;
		this.negProb=negProb;
	}
	
	@Override
	public String toString() {
		return posProb+" "+negProb;
		
	}
	
}