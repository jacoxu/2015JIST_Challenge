package DataPreprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Step02_05_GenVSM_plus {
	public static void main(String[] args) throws Exception {
		//Test
//		String tempLine ="宸濄伄娴併倢銇倛銇?28521"; 
//		String[] tokensStr  = tempLine.split("\t");
		//利用纯文本和wordmap构建基于词频的向量空间模型，用于STH预处理
		//all = [test_data;train_data]!
		String srcDataPathStr = "./../RefineData/";
		String tardataPathStr="./../RefineData/";
		String wordMapStr = tardataPathStr+"/Step04_03_usrWordDict_plus.lst";
		
		String vsmBW_TrainFullStr = tardataPathStr+"/Step04_03_Train_fea_full.VSM";
		String vsmBW_TestFullStr = tardataPathStr+"/Step04_03_Test_fea_full.VSM";
		String vsmBW_TrainFirstSentStr = tardataPathStr+"/Step04_03_Train_fea_firstSent_plus.VSM";
		String vsmBW_TestFirstSentStr = tardataPathStr+"/Step04_03_Test_fea_firstSent_plus.VSM";
		String vsmBW_Train1GramStr = tardataPathStr+"/Step04_03_Train_fea_1gram_plus.VSM";
		String vsmBW_Test1GramStr = tardataPathStr+"/Step04_03_Test_fea_1gram_plus.VSM";

		BufferedReader dataTrainFullFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(srcDataPathStr+
						"Step04_03_Train_fea_full.dat")), "UTF-8"));
		BufferedReader dataTestFullFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(srcDataPathStr+
						"Step04_03_Test_fea_full.dat")), "UTF-8"));
		BufferedReader dataTrainFirstSentFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(srcDataPathStr+
						"Step04_03_Train_fea_firstSent_plus.dat")), "UTF-8"));
		BufferedReader dataTestFirstSentFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(srcDataPathStr+
						"Step04_03_Test_fea_firstSent_plus.dat")), "UTF-8"));
		BufferedReader dataTrain1gramFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(srcDataPathStr+
						"Step04_03_Train_fea_1gram_plus.dat")), "UTF-8"));
		BufferedReader dataTest1gramFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(srcDataPathStr+
						"Step04_03_Test_fea_1gram_plus.dat")), "UTF-8"));		
		
		//构造训练VSM词频向量空间模型
		BufferedReader wordMapRD = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(wordMapStr)), "UTF-8"));
		creatVSMText(dataTrainFullFile, wordMapRD,vsmBW_TrainFullStr);
		wordMapRD.close();
		dataTrainFullFile.close();
		
		wordMapRD = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(wordMapStr)), "UTF-8"));
		creatVSMText(dataTestFullFile, wordMapRD,vsmBW_TestFullStr);
		wordMapRD.close();
		dataTestFullFile.close();
		
		wordMapRD = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(wordMapStr)), "UTF-8"));
		creatVSMText(dataTrainFirstSentFile, wordMapRD,vsmBW_TrainFirstSentStr);
		wordMapRD.close();
		dataTrainFirstSentFile.close();
		
		wordMapRD = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(wordMapStr)), "UTF-8"));
		creatVSMText(dataTestFirstSentFile, wordMapRD,vsmBW_TestFirstSentStr);
		wordMapRD.close();
		dataTestFirstSentFile.close();
		
		wordMapRD = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(wordMapStr)), "UTF-8"));
		creatVSMText(dataTrain1gramFile, wordMapRD,vsmBW_Train1GramStr);
		wordMapRD.close();
		dataTrain1gramFile.close();
		
		wordMapRD = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(wordMapStr)), "UTF-8"));
		creatVSMText(dataTest1gramFile, wordMapRD,vsmBW_Test1GramStr);
		wordMapRD.close();
		dataTest1gramFile.close();

		System.out.println("It is done, ok!");
	}
	
	public static void creatVSMText(BufferedReader sourceTextRD,
			BufferedReader wordMapRD, String vsmBW_Str) throws IOException, Exception {
		System.out.println("Start to create VSM ...!");
		String tempLine;
		//先读入词典
		int wordIdxNum = 1;
		HashMap<String, Integer> wordMap = new HashMap<String,Integer>();

		while ((tempLine = wordMapRD.readLine()) != null) {
			//词典中放着词和索引号，索引号
			if (wordMap.containsKey(tempLine.trim())) {
				System.out.println("Test, the word is replicate:"+tempLine.trim()
						+", in wordIdxNum:"+wordIdxNum);
			}
			if (tempLine.trim().length()==0) continue;
			//wordMap.put(tempLine.trim(), wordIdxNum);
			wordMap.put(tempLine.split("\\s+")[0].trim(), Integer.valueOf(tempLine.split("\\s+")[1]));	
			wordIdxNum =wordIdxNum+1;
		}
		//定义了这个数据集的特征维数
		int dimVector = wordIdxNum-1;
		System.out.println("Has read the dictionary, the size is:"+wordMap.size());
		ArrayList<Integer> wordFreqList = new ArrayList<Integer>();
		int lineNum = 1;
		boolean hasWordFeature = false;
		StringBuffer tmpVSMBuffer = new StringBuffer();
		while ((tempLine = sourceTextRD.readLine()) != null) {
			hasWordFeature = false;
			//读入一行，即一个文档；
			wordFreqList.clear();
			for (int i = 0; i < dimVector; i++) {
				wordFreqList.add(0);
			}

			String[] tokensStr;
			boolean isvalid = true;
			
			tokensStr = tempLine.trim().split("\\s+");

			if (!(tokensStr.length<1)) {
				for (int j = 0; j < tokensStr.length; j++) {
					String tempToken = tokensStr[j];
					if (wordMap.containsKey(tempToken.trim())) {
						hasWordFeature = true;
						int index = wordMap.get(tempToken.trim());
						if (index>dimVector) {
							System.out.print("Error, and the word is: "+tempToken.trim());
						}
						wordFreqList.set(index-1, wordFreqList.get(index-1)+1);
					}else {
						System.out.println("error: the map has not contain the word:"
								+tempToken+" in Line:"+lineNum);
					}
				}
			}else {
				isvalid = false;
			}
			
			if (!isvalid) {
				System.out.println("warning: the string has lacked contents:"
						+tempLine.trim()+" in Line:"+lineNum);
			}
			for (int tempFreq:wordFreqList) {
				tmpVSMBuffer.append(String.valueOf(tempFreq)+" ");
			}
			//把处理好的文本写入到新的文本文件中
			Result2Txt(vsmBW_Str,tmpVSMBuffer.toString().trim());
			tmpVSMBuffer.delete(0, tmpVSMBuffer.length());
			
			if (!hasWordFeature) {
				System.out.println("++++++++++"+"has no word in Line:"+lineNum+"++++++++++");
			}
			lineNum++;
			if (lineNum%1000 ==0) {
				System.out.println("hasProcessed text numbers:" + lineNum);
			}
		}
	}
	public static void Result2Txt(String file, String txt) {
		  try {
			   BufferedWriter os = new BufferedWriter(new OutputStreamWriter(   
		                new FileOutputStream(new File(file),true), "UTF-8")); 
			   os.write(txt + "\n");
			   os.close();
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	 }
}
