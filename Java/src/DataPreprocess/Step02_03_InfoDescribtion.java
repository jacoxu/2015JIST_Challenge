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
import CBrain.config.SmsBase;

public class Step02_03_InfoDescribtion {
	/**
	 * @param args
	 * @author jacoxu.com-2015/10/24
	 * @param refineTestData 
	 * @param refineTrainData 
	 * @param rawInfoBox 
	 * @param rawAbstract 
	 * @param hasTestData 
	 * @param rawTestData 
	 * @param testDataRefine 
	 * @param task1DataTermSet 
	 */
	//rawTrainData, dataRefine, tagInfo
	private static void dataAnalysis(String rawTrainData, String rawTestData
			, String rawAbstract, String rawInfoBox, String refineTrainData, String refineTestData) {
		//URL, Info
		HashMap<String, EntityInfo> urlInfoBoxMap = new HashMap<String, EntityInfo>();
		try {
			String encoding = "UTF-8";
			File rawTrainDataFile = new File(rawTrainData);
			File rawTestDataFile = new File(rawTestData);
			File rawAbstractFile = new File(rawAbstract);
			File rawInfoBoxFile = new File(rawInfoBox);

			if (rawTrainDataFile.exists()) {
				BufferedReader rawTrainDataReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(rawTrainDataFile), encoding));
				BufferedReader rawTestDataReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(rawTestDataFile), encoding));
				BufferedReader rawAbstractReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(rawAbstractFile), encoding));
				BufferedReader rawInfoBoxReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(rawInfoBoxFile), encoding));
				
				//开始读取摘要数据
				System.out.println("Start to process abstract...");
				String tmpURL;
				String tmpAbstract;
				EntityInfo tmpEntityInfo;
				int lineNum=0;
				String tmpLineStr = null;
				while ((tmpLineStr = rawAbstractReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					tmpEntityInfo = new EntityInfo();
					String[] urlInfo = tmpLineStr.split("\\s+");
					tmpURL = urlInfo[0].trim();
					tmpURL = tmpURL.substring(1, tmpURL.length()-1);
					
					tmpAbstract = tmpLineStr.substring(tmpLineStr.indexOf(SmsBase.abstractPrefix)+SmsBase.abstractPrefix.length()
							,tmpLineStr.length()-SmsBase.abstractProfix.length());
					tmpAbstract = ConvertUnicode.convertUnicode(tmpAbstract);
					
					//如果是初次遇到此URL
					if (urlInfoBoxMap.containsKey(tmpURL)) {
						System.err.println("Has encountered the url:"+ tmpURL +" in lineNum:"+lineNum);
					}
					tmpEntityInfo.abstractStr = "摘要\t"+tmpAbstract;
					tmpEntityInfo.hasAbstractStr = true;
					urlInfoBoxMap.put(tmpURL, tmpEntityInfo);
										
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed abstractInfo numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed abstractInfo numbers:" + lineNum);
				rawAbstractReader.close();
								
				//开始读取属性数据
				System.out.println("Start to process infoBox...");
				tmpURL = "";
				String tmpInfoBox;
				String tmpInfoBoxValue;
				tmpEntityInfo = null;
				lineNum=0;
				tmpLineStr = null;
				while ((tmpLineStr = rawInfoBoxReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] urlInfo = tmpLineStr.split("\\s+");
					tmpURL = urlInfo[0].trim();
					tmpURL = tmpURL.substring(1, tmpURL.length()-1);
					
					String[] infoBoxArray = tmpLineStr.split("> ");
					tmpInfoBox = infoBoxArray[1];
					tmpInfoBox = tmpInfoBox.substring(tmpInfoBox.lastIndexOf("/")+1);
					tmpInfoBox = URLDecoder.decode(tmpInfoBox, SmsBase.encode);
					
					String[] infoBoxValueArray = tmpLineStr.split("> \"");
					if (infoBoxValueArray.length<2) {
						System.err.print("Error: infoBoxArray.length is "+ infoBoxValueArray.length 
								+ " in lineNum" + lineNum);
						System.exit(0);
					}
					tmpInfoBoxValue = infoBoxValueArray[1].trim();
					tmpInfoBoxValue = tmpInfoBoxValue.substring(0,tmpInfoBoxValue.length()-SmsBase.infoBoxProfix.length());
					tmpInfoBoxValue = ConvertUnicode.convertUnicode(tmpInfoBoxValue);
					
					//如果是初次遇到此URL
					if (urlInfoBoxMap.containsKey(tmpURL)) {
						tmpEntityInfo = urlInfoBoxMap.get(tmpURL);
					}else {
						tmpEntityInfo = new EntityInfo();
					}
					tmpEntityInfo.hasInfoBoxList = true;
					tmpEntityInfo.infoBoxList.add(tmpInfoBox+"\t"+tmpInfoBoxValue);
					
					urlInfoBoxMap.put(tmpURL, tmpEntityInfo);
										
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed abstractInfo numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed abstractInfo numbers:" + lineNum);
				rawInfoBoxReader.close();
				
				System.out.println("Start to refine train data ...");
				tmpURL = "";
				tmpEntityInfo = null;
				lineNum=0;
				tmpLineStr = null;
				int trainHasAbstract = 0;
				int trainHasInfoBox = 0;
				while ((tmpLineStr = rawTrainDataReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] urlInfo = tmpLineStr.split("\t");
					tmpURL = urlInfo[0].trim();

					if (urlInfoBoxMap.containsKey(tmpURL)) {
						tmpEntityInfo = urlInfoBoxMap.get(tmpURL);
					}else {
						System.err.println("Non any InfoBox for URL"+tmpURL+" in lineNum:"+lineNum);
					}
					if (tmpEntityInfo.hasAbstractStr) {
						trainHasAbstract++;			
					}
					if (tmpEntityInfo.hasInfoBoxList) {
						trainHasInfoBox++;
					}
					
					tmpLineStr = tmpLineStr + tmpEntityInfo.getInfoStr();
					Result2Txt(refineTrainData, tmpLineStr);
					
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed train numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed train data numbers:" + lineNum);
				System.out.println("trainHasAbstract:" + trainHasAbstract+" trainHasInfoBox:"+trainHasInfoBox);
				rawTrainDataReader.close();
				
				System.out.println("Start to refine test data ...");
				tmpURL = "";
				tmpEntityInfo = null;
				lineNum=0;
				tmpLineStr = null;
				int testHasAbstract = 0;
				int testHasInfoBox = 0;
				while ((tmpLineStr = rawTestDataReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] urlInfo = tmpLineStr.split("\t");
					tmpURL = urlInfo[0].trim();

					if (urlInfoBoxMap.containsKey(tmpURL)) {
						tmpEntityInfo = urlInfoBoxMap.get(tmpURL);
					}else {
						System.err.println("Non any InfoBox for URL"+tmpURL+" in lineNum:"+lineNum);
					}
					if (tmpEntityInfo.hasAbstractStr) {
						testHasAbstract++;			
					}
					if (tmpEntityInfo.hasInfoBoxList) {
						testHasInfoBox++;
					}
					tmpLineStr = tmpLineStr + tmpEntityInfo.getInfoStr();
					Result2Txt(refineTestData, tmpLineStr);
					
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed test numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed test data numbers:" + lineNum);
				System.out.println("testHasAbstract:" + testHasAbstract+" testHasInfoBox:"+testHasInfoBox);
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
		String testC = "%FF%FE%1C%4E%9E%83%02%5E%2D%4E%C3%5F%7F%5E%3A%57";
		String testB;
		try {
			testB = URLDecoder.decode(testC, "UTF-8");
			testB = URLDecoder.decode(testC, "ASCII");
			testB = URLDecoder.decode(testC, "GBK");
			testB = URLDecoder.decode(testC, "GB18030");
			testB = URLDecoder.decode(testC);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		testA = ConvertUnicode.convertUnicode(testA);
		testA = testA.substring(1,testA.length()-1);
		testA = testA.substring(testA.lastIndexOf("/")+1);
		//***********测试区域************
		
		String dataPathStr="./../Data/";
		String resultsPathStr="./../RefineData/";
		File resultsPathFile = new File(resultsPathStr);
		if (!resultsPathFile.exists()) resultsPathFile.mkdir();

		//分析数据集
	    String rawTrainData=dataPathStr+"train.dat";
	    String rawTestData=dataPathStr+"test.dat";
	    String rawAbstract=dataPathStr+"zhishime_abstracts_zh_enc.nt";
	    String rawInfoBox=dataPathStr+"zhishime_infobox_properties_zh_enc.nt";

	    //分析的结果输出
	    String refineTrainData = resultsPathStr +"Step02_03_Train_desc.dat";
	    String refineTestData = resultsPathStr +"Step02_03_Test_desc.dat";
		
		long readstart=System.currentTimeMillis();
		dataAnalysis(rawTrainData, rawTestData, rawAbstract, rawInfoBox, refineTrainData, refineTestData);
        long readend=System.currentTimeMillis();
        System.out.println((readend-readstart)/1000.0+"s had been consumed to process the raw training data");
	}

}
