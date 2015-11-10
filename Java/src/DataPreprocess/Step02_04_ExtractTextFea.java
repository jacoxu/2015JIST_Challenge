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
import CBrain.config.SmsBase;

public class Step02_04_ExtractTextFea {
	/**
	 * @param args
	 * @author jacoxu.com-2015/10/24
	 * @param feaTestData_full 
	 * @param feaTrainData_full 
	 * @param feaTestData_abstract 
	 * @param feaTrainData_abstract 
	 * @param hasTestData 
	 * @param feaTestData_full 
	 * @param testDataRefine 
	 * @param task1DataTermSet 
	 */
	//rawTrainData, dataRefine, tagInfo
//	refineTrainData, refineTestData, feaTrainData_abstract, feaTestData_abstract
//	, feaTrainData_full, feaTestData_full
	private static void dataAnalysis(String refineTrainData, String refineTestData
			, String feaTrainData_abstract, String feaTestData_abstract
			, String feaTrainData_full, String feaTestData_full) {
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
					
					tmpFeaFull = PreProcessText.preProcess4Task2(tmpFeaFull);
					tmpFeaAbstract = PreProcessText.preProcess4Task2(tmpFeaAbstract);
					
					Result2Txt(feaTrainData_abstract, tmpFeaAbstract);
					Result2Txt(feaTrainData_full, tmpFeaFull);
					
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
					
					tmpFeaFull = PreProcessText.preProcess4Task2(tmpFeaFull);
					tmpFeaAbstract = PreProcessText.preProcess4Task2(tmpFeaAbstract);
					
					Result2Txt(feaTestData_abstract, tmpFeaAbstract);
					Result2Txt(feaTestData_full, tmpFeaFull);
					
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
		//***********测试区域************
		
		String dataPathStr="./../Data/";
		String resultsPathStr="./../RefineData/";
		File resultsPathFile = new File(resultsPathStr);
		if (!resultsPathFile.exists()) resultsPathFile.mkdir();

		//分析数据集
	    String refineTrainData = resultsPathStr +"Step02_03_Train_desc.dat";
	    String refineTestData = resultsPathStr +"Step02_03_Test_desc.dat";

	    //分析的结果输出
	    String feaTrainData_abstract = resultsPathStr +"Step02_04_Train_fea_abstract.dat";
	    String feaTestData_abstract = resultsPathStr +"Step02_04_Test_fea_abstract.dat";
	    String feaTrainData_full = resultsPathStr +"Step02_04_Train_fea_full.dat";
	    String feaTestData_full = resultsPathStr +"Step02_04_Test_fea_full.dat";
		
		long readstart=System.currentTimeMillis();
		dataAnalysis(refineTrainData, refineTestData, feaTrainData_abstract, feaTestData_abstract
				, feaTrainData_full, feaTestData_full);
        long readend=System.currentTimeMillis();
        System.out.println((readend-readstart)/1000.0+"s had been consumed to process the raw training data");
	}

}
