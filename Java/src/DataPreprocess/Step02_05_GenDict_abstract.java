package DataPreprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.LSTORE;

import CBrain.config.SmsBase;

public class Step02_05_GenDict_abstract {
	public static void main(String[] args) throws Exception {

		//读取训练数据文件
		String trainFeaSplitWords = "./../RefineData/Step02_04_Train_fea_abstract.dat";
		FileReader fr = new FileReader(trainFeaSplitWords);
		BufferedReader br = new BufferedReader(fr);
		
		String usrWordDict = "./../RefineData/Step02_05_usrWordDict_abstract.lst";
		BufferedWriter wordDictFileW = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(usrWordDict)), "UTF-8"));
		
		//先读入处理好的训练语料
		int wordReadNum = 0;
		int wordWriteNum = 0;

		HashMap<String, Long> wordMap = new HashMap<String, Long>();
		System.out.println("Start to read wordSet ...");
		int lineNum = 0;
		String tempLine;
		while ((tempLine = br.readLine()) != null) {
			lineNum++;
			String[] wordArraysStr = tempLine.trim().split("\\s+");
			for (int i = 0; i < wordArraysStr.length; i++) {
				String tmpWord = wordArraysStr[i].trim();
				if (tmpWord.length()<1) continue;
				if (wordMap.containsKey(tmpWord)) {
					wordMap.put(tmpWord, wordMap.get(tmpWord)+1);
				}else {
					wordMap.put(tmpWord,(long) 1);
					wordReadNum++;
				}
			}
			if (lineNum%1000 ==0) {
				System.out.println("hasProcessed train data numbers:" + lineNum);
			}
		}
		System.out.println("Total lineNum:"+lineNum+", and wordSet.size():"+wordMap.size());
		br.close();
		
		Iterator iter=wordMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Long> entry = (Map.Entry) iter.next();
			String tmpWord = entry.getKey();

			wordWriteNum++;
			wordDictFileW.write(tmpWord+"\t"+wordWriteNum+"\n");
		}
		System.out.println("wordReadNum:" +wordReadNum);
		System.out.println("wordWriteNum:" +wordWriteNum);
		if (wordMap.size()!=wordReadNum) {
			System.out.println("Error! wordReadNum is diffent with wordMap.size()");
		}	
		wordDictFileW.close();
	}
}