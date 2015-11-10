package DataPreprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Tools.ConvertUnicode;
import Tools.EntityInfo;
import Tools.PreProcessText;
import Tools.SplitNGram;
import Tools.StringAnalyzer;
import Tools.TagInfo;
import CBrain.config.SmsBase;

public class Step04_03_ExtractTextFea_Plus {
	/**
	 * @param args
	 * @author jacoxu.com-2015/10/24
	 * @param feaTestData_full_plus 
	 * @param feaTrainData_full_plus 
	 * @param feaTestData_keyword 
	 * @param feaTrainData_keyword 
	 * @param hasTestData 
	 * @param feaTestData_full_plus 
	 * @param feaTestData_1gram_plus 
	 * @param feaTrainData_1gram_plus 
	 * @param feaTestData_firstSent_plus 
	 * @param feaTrainData_firstSent_plus 
	 * @param feaTestData_full 
	 * @param feaTrainData_full 
	 * @param feaTestData_secKeyword 
	 * @param feaTrainData_secKeyword 
	 * @param feaTestData_abstract 
	 * @param feaTrainData_abstract 
	 * @param feaTestData_firstSubSent_plus 
	 * @param feaTrainData_firstSubSent_plus 
	 * @param feaTestData_sub1gram_plus 
	 * @param feaTrainData_sub1gram_plus 
	 * @param feaTestData_area 
	 * @param feaTrainData_area 
	 * @param testDataRefine 
	 * @param task1DataTermSet 
	 */
	//refineTrainData, refineTestData, feaTrainData_keyword, feaTestData_keyword
//	, feaTrainData_full, feaTestData_full
	private static void dataAnalysis(String refineTrainData, String refineTestData
			, String feaTrainData_keyword, String feaTestData_keyword
			, String feaTrainData_full_plus, String feaTestData_full_plus
			, String feaTrainData_firstSent_plus, String feaTestData_firstSent_plus
			, String feaTrainData_1gram_plus, String feaTestData_1gram_plus
			, String feaTrainData_full, String feaTestData_full
			, String feaTrainData_secKeyword, String feaTestData_secKeyword
			, String feaTrainData_abstract, String feaTestData_abstract
			, String feaTrainData_firstSubSent_plus, String feaTestData_firstSubSent_plus
			, String feaTrainData_sub1gram_plus, String feaTestData_sub1gram_plus
			, String feaTrainData_area, String feaTestData_area) {
		try {
			String encoding = "UTF-8";
			File refineTrainDataFile = new File(refineTrainData);
			File refineTestDataFile = new File(refineTestData);

			if (refineTrainDataFile.exists()) {
				BufferedReader rawTrainDataReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(refineTrainDataFile), encoding));
				BufferedReader rawTestDataReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(refineTestDataFile), encoding));
				
				//开始读取训练数据
				System.out.println("Start to process train data...");
				String tmpFeaFull;
				String tmpFeaAbstract;
				int lineNum=0;
				String tmpLineStr = null;
				while ((tmpLineStr = rawTrainDataReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					tmpFeaFull = tmpLineStr.substring(tmpLineStr.indexOf(SmsBase.abstractTagStr)+SmsBase.abstractTagStr.length());
					String[] entityInfo = tmpLineStr.split("\t");
					tmpFeaAbstract = entityInfo[3].trim();
					String[] tmpSentArray = tmpFeaAbstract.split("。|！");
					String tmpFirstSentStr = tmpSentArray[0].trim();
					
					tmpFeaFull = PreProcessText.preProcess4Task2(tmpFeaFull);
					tmpFeaAbstract = PreProcessText.preProcess4Task2(tmpFeaAbstract);
					tmpSentArray = tmpFirstSentStr.toLowerCase().split("，");
					String tmpFirstSubSentStr = tmpSentArray[0].trim();
					tmpFirstSubSentStr = PreProcessText.preProcess4Task2(tmpFirstSubSentStr);
					String tmpSub1GramStr = SplitNGram.split2NGram(
							StringAnalyzer.extractChineseCharacter(tmpFirstSubSentStr), 1, false);
					
					tmpFirstSentStr = PreProcessText.preProcess4Task2(tmpFirstSentStr);
					String tmp1GramStr = SplitNGram.split2NGram(
							StringAnalyzer.extractChineseCharacter(tmpFirstSentStr), 1, false);
					
					double[] tmpClassPros = new double[SmsBase.tagInfoMap.size()];
					for (int i = 0; i < tmpClassPros.length; i++) {
						tmpClassPros[i] = 0.0;
					}
					double[] tmpSecClassPros = new double[SmsBase.tagInfoMap.size()];
					for (int i = 0; i < tmpSecClassPros.length; i++) {
						tmpSecClassPros[i] = 0.0;
					}
					double[] tmpAreaPros = new double[SmsBase.tagInfoMap.size()];
					for (int i = 0; i < tmpAreaPros.length; i++) {
						tmpAreaPros[i] = 0.0;
					}
					int tmpLeastSpan = Integer.MAX_VALUE;
					int tmpKeywordClass = -1;
					int tmpSecKeywordClass = -1;
					int tmpKeywordIndx = -1;
					for (int i = 0; (i < tmpSentArray.length)&&(i < 3); i++) {
						String tmpSubSentStr = tmpSentArray[i];
						if (tmpSubSentStr.contains("是")) {
							tmpSubSentStr = tmpSubSentStr.substring(tmpSubSentStr.indexOf("是")+1);
							for (int j = 0; j < SmsBase.tagInfoMap.size(); j++) {
								TagInfo tmpTagInfo = SmsBase.tagInfoMap.get(j);
								for (int k = 0; k < tmpTagInfo.keywordList.size(); k++) {
									tmpKeywordIndx = tmpSubSentStr.lastIndexOf(tmpTagInfo.keywordList.get(k));
									if (tmpKeywordIndx>0) {
										int tmpSpan = tmpSubSentStr.length()-tmpKeywordIndx-tmpTagInfo.keywordList.get(k).length();
										if ((tmpSpan<tmpLeastSpan)&&(tmpSpan<=3)) {
											tmpKeywordClass = j;					
										}
									}
								}
							}
						}
						if ((i==tmpSentArray.length-1)&&(tmpKeywordClass>0)) {
							for (int j = 0; j < SmsBase.tagInfoMap.size(); j++) {
								TagInfo tmpTagInfo = SmsBase.tagInfoMap.get(j);
								for (int k = 0; k < tmpTagInfo.keywordList.size(); k++) {
									if (tmpSubSentStr.endsWith(tmpTagInfo.keywordList.get(k))) {
										tmpSecKeywordClass = j;
									}
								}
							}									
						}
					}
					if (tmpKeywordClass>0) {
						tmpClassPros[tmpKeywordClass]+=3;
					}
					if (tmpSecKeywordClass>0) {
						tmpSecClassPros[tmpSecKeywordClass]+=1;
					}
					if (tmpLineStr.contains("平方米")) {
						tmpAreaPros[4] = -0.1;
					}
					StringBuffer tmpBuffer = new StringBuffer();
					for (int i = 0; i < tmpClassPros.length; i++) {
						tmpBuffer.append(" ");
						tmpBuffer.append(String.valueOf(tmpClassPros[i]));
					}
					StringBuffer tmpSecBuffer = new StringBuffer();
					for (int i = 0; i < tmpSecClassPros.length; i++) {
						tmpSecBuffer.append(" ");
						tmpSecBuffer.append(String.valueOf(tmpSecClassPros[i]));
					}
					StringBuffer tmpAreaBuffer = new StringBuffer();
					for (int i = 0; i < tmpSecClassPros.length; i++) {
						tmpAreaBuffer.append(" ");
						tmpAreaBuffer.append(String.valueOf(tmpAreaPros[i]));
					}
					
//					Result2Txt(feaTrainData_keyword, tmpBuffer.toString().trim());
//					Result2Txt(feaTrainData_secKeyword, tmpSecBuffer.toString().trim());
//					Result2Txt(feaTrainData_full_plus, tmp1GramStr+"\t"+tmpFirstSentStr+"\t"+tmpFeaFull);
//					Result2Txt(feaTrainData_firstSent_plus, tmpFirstSentStr);
//					Result2Txt(feaTrainData_1gram_plus, tmp1GramStr);
//					Result2Txt(feaTrainData_full, tmpFeaFull);
//					Result2Txt(feaTrainData_abstract, tmpFeaAbstract);
//					Result2Txt(feaTrainData_firstSubSent_plus, tmpFirstSubSentStr);
//					Result2Txt(feaTrainData_sub1gram_plus, tmpSub1GramStr);
					Result2Txt(feaTrainData_area, tmpAreaBuffer.toString().trim());
					tmpAreaBuffer.delete(0, tmpAreaBuffer.length());
					tmpBuffer.delete(0, tmpBuffer.length());
					tmpSecBuffer.delete(0, tmpSecBuffer.length());
					if (lineNum%1000 ==0) {						
						System.out.println("hasProcessed refined Train numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed refined Train numbers:" + lineNum);
				rawTrainDataReader.close();
				
				//开始读取测试数据
				System.out.println("Start to process test data...");
				lineNum=0;
				while ((tmpLineStr = rawTestDataReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					tmpFeaFull = tmpLineStr.substring(tmpLineStr.indexOf(SmsBase.abstractTagStr)+SmsBase.abstractTagStr.length());
					String[] entityInfo = tmpLineStr.split("\t");
					tmpFeaAbstract = entityInfo[2].trim();
					String[] tmpSentArray = tmpFeaAbstract.split("。|！");
					String tmpFirstSentStr = tmpSentArray[0].trim();
					
					tmpFeaFull = PreProcessText.preProcess4Task2(tmpFeaFull);
					tmpFeaAbstract = PreProcessText.preProcess4Task2(tmpFeaAbstract);
					tmpSentArray = tmpFirstSentStr.toLowerCase().split("，");
					String tmpFirstSubSentStr = tmpSentArray[0].trim();
					tmpFirstSubSentStr = PreProcessText.preProcess4Task2(tmpFirstSubSentStr);
					String tmpSub1GramStr = SplitNGram.split2NGram(
							StringAnalyzer.extractChineseCharacter(tmpFirstSubSentStr), 1, false);
					
					tmpFirstSentStr = PreProcessText.preProcess4Task2(tmpFirstSentStr);
					String tmp1GramStr = SplitNGram.split2NGram(
							StringAnalyzer.extractChineseCharacter(tmpFirstSentStr), 1, false);
					
					double[] tmpClassPros = new double[SmsBase.tagInfoMap.size()];
					for (int i = 0; i < tmpClassPros.length; i++) {
						tmpClassPros[i] = 0.0;
					}
					double[] tmpSecClassPros = new double[SmsBase.tagInfoMap.size()];
					for (int i = 0; i < tmpSecClassPros.length; i++) {
						tmpSecClassPros[i] = 0.0;
					}
					double[] tmpAreaPros = new double[SmsBase.tagInfoMap.size()];
					for (int i = 0; i < tmpAreaPros.length; i++) {
						tmpAreaPros[i] = 0.0;
					}
					int tmpLeastSpan = Integer.MAX_VALUE;
					int tmpKeywordClass = -1;
					int tmpSecKeywordClass = -1;
					int tmpKeywordIndx = -1;
					for (int i = 0; (i < tmpSentArray.length)&&(i < 3); i++) {
						String tmpSubSentStr = tmpSentArray[i];
						if (tmpSubSentStr.contains("是")) {
							tmpSubSentStr = tmpSubSentStr.substring(tmpSubSentStr.indexOf("是")+1);
							for (int j = 0; j < SmsBase.tagInfoMap.size(); j++) {
								TagInfo tmpTagInfo = SmsBase.tagInfoMap.get(j);
								for (int k = 0; k < tmpTagInfo.keywordList.size(); k++) {
									tmpKeywordIndx = tmpSubSentStr.lastIndexOf(tmpTagInfo.keywordList.get(k));
									if (tmpKeywordIndx>0) {
										int tmpSpan = tmpSubSentStr.length()-tmpKeywordIndx-tmpTagInfo.keywordList.get(k).length();
										if ((tmpSpan<tmpLeastSpan)&&(tmpSpan<=3)) {
											tmpKeywordClass = j;										
										}
									}
								}
							}
						}
						if ((i==tmpSentArray.length-1)&&(tmpKeywordClass>0)) {
							for (int j = 0; j < SmsBase.tagInfoMap.size(); j++) {
								TagInfo tmpTagInfo = SmsBase.tagInfoMap.get(j);
								for (int k = 0; k < tmpTagInfo.keywordList.size(); k++) {
									if (tmpSubSentStr.endsWith(tmpTagInfo.keywordList.get(k))) {
										tmpSecKeywordClass = j;
									}
								}
							}
						}
					}
					if (tmpKeywordClass>0) {
						tmpClassPros[tmpKeywordClass]+=3;	
					}
					if (tmpSecKeywordClass>0) {
						tmpSecClassPros[tmpSecKeywordClass]+=1;			
					}
					if (tmpLineStr.contains("平方米")) {
						tmpAreaPros[4] = -0.1;
					}
					StringBuffer tmpBuffer = new StringBuffer();
					for (int i = 0; i < tmpClassPros.length; i++) {
						tmpBuffer.append(" ");
						tmpBuffer.append(String.valueOf(tmpClassPros[i]));
					}
					StringBuffer tmpSecBuffer = new StringBuffer();
					for (int i = 0; i < tmpSecClassPros.length; i++) {
						tmpSecBuffer.append(" ");
						tmpSecBuffer.append(String.valueOf(tmpSecClassPros[i]));
					}
					StringBuffer tmpAreaBuffer = new StringBuffer();
					for (int i = 0; i < tmpSecClassPros.length; i++) {
						tmpAreaBuffer.append(" ");
						tmpAreaBuffer.append(String.valueOf(tmpAreaPros[i]));
					}
					
//					Result2Txt(feaTestData_keyword, tmpBuffer.toString().trim());
//					Result2Txt(feaTestData_secKeyword, tmpSecBuffer.toString().trim());
//					Result2Txt(feaTestData_full_plus, tmp1GramStr+"\t"+tmpFirstSentStr+"\t"+tmpFeaFull);
//					Result2Txt(feaTestData_firstSent_plus, tmpFirstSentStr);
//					Result2Txt(feaTestData_1gram_plus, tmp1GramStr);
//					Result2Txt(feaTestData_full, tmpFeaFull);
//					Result2Txt(feaTestData_abstract, tmpFeaAbstract);
//					Result2Txt(feaTestData_firstSubSent_plus, tmpFirstSubSentStr);
//					Result2Txt(feaTestData_sub1gram_plus, tmpSub1GramStr);
					Result2Txt(feaTestData_area, tmpAreaBuffer.toString().trim());
					tmpAreaBuffer.delete(0, tmpAreaBuffer.length());
					tmpBuffer.delete(0, tmpBuffer.length());
					tmpSecBuffer.delete(0, tmpSecBuffer.length());
					if (lineNum%1000 ==0) {					
						System.out.println("hasProcessed refined Test numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed refined Test numbers:" + lineNum);
				rawTestDataReader.close();
				
				
			} else {
				System.out.println("can't find the file");
			}
		} catch (Exception e) {
			System.out.println("something error when reading the content of the file");
			e.printStackTrace();
		}
		return;
		
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
	
	public static void main(String[] args) {
		//***********测试区域************
		System.out.println("test");
		String testA = "<http://zhishi.me/baidubaike/resource/%FF%FE%70%51%7F%89%BF%53> <http://zhishi.me/ontology/abstract> \"\u5170\u897F\u4F4D\u4E8E\u677E\u5AE9\u5E73\u539F\u4E1C\u90E8\uFF0C\u547C\u5170\u6CB3\u4E0B\u6E38\uFF0C\u5904\u4E8E\u54C8\u5C14\u6EE8\u5E02\"\u4E00\u5C0F\u65F6\u7ECF\u6D4E\u5708\"\u5185\uFF0C\u5357\u4E0E\u54C8\u5E02\u547C\u5170\u533A\u63A5\u58E4\uFF0C\u4E1C\u754C\u5317\u6797\u533A,\u897F\u8FDE\u5927\u5E86\u5E02\u3001\u5B89\u8FBE\u5E02\u3001\u8087\u4E1C\u5E02\uFF0C\u8DDD\u54C8\u5E02\u533A67\u516C\u7406\u3002\u5170\u897F\u662F\u56FD\u5BB6\u5546\u54C1\u7CAE\u98DF\u91CD\u8981\u57FA\u5730\uFF0C\u4E5F\u662F\u5168\u56FD\u7CAE\u98DF\u4EA7\u91CF\u767E\u5F3A\u53BF\uFF0C\u7D20\u6709\u201C\u4E2D\u56FD\u4E9A\u9EBB\u4E4B\u4E61\u201D\u548C\u201C\u4E2D\u56FD\u4E1C\u5317\u6C11\u4E4B\u4E61\u201D\u4E4B\u79F0\uFF0C\u88AB\u56FD\u5BB6\u547D\u540D\u4E3A\u201C\u4E2D\u56FD\u4E9A\u9EBB\u7F16\u7EC7\u540D\u57CE\u201D\u3002\"@zh .";
		testA = testA.substring(testA.indexOf(SmsBase.abstractPrefix)+SmsBase.abstractPrefix.length()
								,testA.length()-SmsBase.abstractProfix.length());
		testA = ConvertUnicode.convertUnicode(testA);
		testA = testA.substring(1,testA.length()-1);
		testA = testA.substring(testA.lastIndexOf("/")+1);
		double[] testD = new double[5];
		testD[0] = 0.1;
		testD[2] = 0.2;
		testD[3] = 0.3;
		testD[4] = 0.4;
		
		System.out.println("testD:"+testD);
		System.out.println(testA.indexOf("许家铭"));
		//***********测试区域************
		try {
			if (!SmsBase.hasLoadConfig) {
				SmsBase.SmsBaseLoadConfig();			
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String dataPathStr="./../Data/";
		String resultsPathStr="./../RefineData/";
		File resultsPathFile = new File(resultsPathStr);
		if (!resultsPathFile.exists()) resultsPathFile.mkdir();

		//分析数据集
	    String refineTrainData = resultsPathStr +"Step02_03_Train_desc.dat";
	    String refineTestData = resultsPathStr +"Step02_03_Test_desc.dat";

	    //分析的结果输出
	    String feaTrainData_keyword = resultsPathStr +"Step04_03_Train_fea_keyword_plus.dat";
	    String feaTestData_keyword = resultsPathStr +"Step04_03_Test_fea_keyword_plus.dat";
	    String feaTrainData_secKeyword = resultsPathStr +"Step04_03_Train_fea_secKeyword_plus.dat";
	    String feaTestData_secKeyword = resultsPathStr +"Step04_03_Test_fea_secKeyword_plus.dat";	    
	    String feaTrainData_full_plus = resultsPathStr +"Step04_03_Train_fea_full_plus.dat";
	    String feaTestData_full_plus = resultsPathStr +"Step04_03_Test_fea_full_plus.dat";
	    String feaTrainData_firstSent_plus = resultsPathStr +"Step04_03_Train_fea_firstSent_plus.dat";
	    String feaTestData_firstSent_plus = resultsPathStr +"Step04_03_Test_fea_firstSent_plus.dat";
	    String feaTrainData_1gram_plus = resultsPathStr +"Step04_03_Train_fea_1gram_plus.dat";
	    String feaTestData_1gram_plus = resultsPathStr +"Step04_03_Test_fea_1gram_plus.dat";	
	    String feaTrainData_full = resultsPathStr +"Step04_03_Train_fea_full.dat";
	    String feaTestData_full = resultsPathStr +"Step04_03_Test_fea_full.dat";
	    String feaTrainData_abstract = resultsPathStr +"Step04_03_Train_fea_abstract.dat";
	    String feaTestData_abstract = resultsPathStr +"Step04_03_Test_fea_abstract.dat";	
	    String feaTrainData_firstSubSent_plus = resultsPathStr +"Step04_03_Train_fea_firstSubSent_plus.dat";
	    String feaTestData_firstSubSent_plus = resultsPathStr +"Step04_03_Test_fea_firstSubSent_plus.dat";
	    String feaTrainData_sub1gram_plus = resultsPathStr +"Step04_03_Train_fea_Sub1gram_plus.dat";
	    String feaTestData_sub1gram_plus = resultsPathStr +"Step04_03_Test_fea_Sub1gram_plus.dat";
	    String feaTrainData_area = resultsPathStr +"Step04_03_Train_fea_area_plus.dat";
	    String feaTestData_area = resultsPathStr +"Step04_03_Test_fea_area_plus.dat";	
		
		long readstart=System.currentTimeMillis();
		dataAnalysis(refineTrainData, refineTestData, feaTrainData_keyword, feaTestData_keyword
				, feaTrainData_full_plus, feaTestData_full_plus,feaTrainData_firstSent_plus, feaTestData_firstSent_plus
				, feaTrainData_1gram_plus, feaTestData_1gram_plus,feaTrainData_full,feaTestData_full
				, feaTrainData_secKeyword, feaTestData_secKeyword
				, feaTrainData_abstract, feaTestData_abstract
				, feaTrainData_firstSubSent_plus, feaTestData_firstSubSent_plus
				, feaTrainData_sub1gram_plus, feaTestData_sub1gram_plus
				, feaTrainData_area, feaTestData_area);
        long readend=System.currentTimeMillis();
        System.out.println((readend-readstart)/1000.0+"s had been consumed to process the raw training data");
	}

}
