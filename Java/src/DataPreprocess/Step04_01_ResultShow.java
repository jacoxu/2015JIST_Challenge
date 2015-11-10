package DataPreprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import CBrain.config.SmsBase;

public class Step04_01_ResultShow {
	/**
	 * @param args
	 * @author jacoxu.com-2015/10/24
	 * @param hasTestData 
	 * @param predictTestTag 
	 * @param resultShowTest 
	 * @param task1DataTermSet 
	 */
	//refinedTestData, predictTestTag, resultShowTest
	private static void dataAnalysis(String refinedTestData, String predictTestTag
			, String tagMapStr, String resultShowTest) {
		//
		HashMap<Integer, String> tagInfoMap = new HashMap<Integer, String>();
		try {
			String encoding = "UTF-8";
			File refinedTestDatafile = new File(refinedTestData);
			File predictTestTagfile = new File(predictTestTag);
			File tagMapfile = new File(tagMapStr);
			
			if (refinedTestDatafile.exists()) {
				BufferedReader refinedTestDataReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(refinedTestDatafile), encoding));
				BufferedReader predictTestTagReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(predictTestTagfile), encoding));
				BufferedReader tagMapReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(tagMapfile), encoding));
				
				//首先读取标签Map
				System.out.println("Start to process tagMap info...");
				String tmpTag;
				String tmpTagNum;
				
				int lineNum=0;
				String tmpLineStr = null;
				while ((tmpLineStr = tagMapReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] trainSentInfo = tmpLineStr.split("\t");
					if (trainSentInfo.length<2) {
						System.err.print("Error: trainSentInfo.length is "+ trainSentInfo.length 
								+ " in lineNum" + lineNum);
						System.exit(0);
					}
					tmpTag = trainSentInfo[0].trim();
					tmpTagNum = trainSentInfo[1].trim();
				
					tagInfoMap.put(Integer.valueOf(tmpTagNum), tmpTag);
										
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed tagMap data numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed tagMap data numbers:" + lineNum);
				tagMapReader.close();
				
				//首先读取测试文本数据和预测的测试标签
				System.out.println("Start to process test data and test tag info...");
				String testPrefixStr;
				String testPostfixStr;
				lineNum=0;
				while ((tmpLineStr = refinedTestDataReader.readLine()) != null) {
					tmpTagNum = predictTestTagReader.readLine();
					lineNum++;
					if (lineNum<=0) continue;
//					testPrefixStr = tmpLineStr.substring(0,tmpLineStr.indexOf(SmsBase.abstractTagStr));
//					testPostfixStr = tmpLineStr.substring(tmpLineStr.indexOf(SmsBase.abstractTagStr));
					double tmpTagNum_d = Double.valueOf(tmpTagNum);
//					tmpLineStr = testPrefixStr+"\t"+tagInfoMap.get((int)tmpTagNum_d)+testPostfixStr;
					tmpLineStr = tmpLineStr+"\t"+tagInfoMap.get((int)tmpTagNum_d);
					Result2Txt(resultShowTest, tmpLineStr);
					
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed test data and test tag info numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed test data and test tag info numbers:" + lineNum);
				predictTestTagReader.close();
				refinedTestDataReader.close();
				
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
		String test = "   8.0000000e+00";
		double test1 = Double.valueOf(test);
		System.out.println(test1);
		System.out.println((int)test1);

		//***********测试区域************
		
		String dataPathStr="./../Data/";
		String resultsPathStr="./../RefineData/";
		File resultsPathFile = new File(resultsPathStr);
		if (!resultsPathFile.exists()) resultsPathFile.mkdir();

		//进行测试集结果展示
	    String refinedTestData=dataPathStr+"test.dat";//resultsPathStr+"Step02_03_Test_desc.dat";
	    String predictTestTag = "./../predictSVM_label.txt";
	    String tagMapStr = resultsPathStr +"Step02_01_tag_info.lst";

	    //分析的结果输出
	    String resultShowTest = resultsPathStr +"result.dat";
		
		long readstart=System.currentTimeMillis();
		dataAnalysis(refinedTestData, predictTestTag, tagMapStr, resultShowTest);
        long readend=System.currentTimeMillis();
        System.out.println((readend-readstart)/1000.0+"s had been consumed to process the raw training data");
	}

}
