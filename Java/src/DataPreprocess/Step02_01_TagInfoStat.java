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

public class Step02_01_TagInfoStat {
	/**
	 * @param args
	 * @author jacoxu.com-2015/10/24
	 * @param hasTestData 
	 * @param rawTestData 
	 * @param testDataRefine 
	 * @param task1DataTermSet 
	 */
	//rawTrainData, dataRefine, tagInfo
	private static void dataAnalysis(String rawTrainData, String trainDataRefine, String tagInfo
			, String rawTestData, boolean hasTestData, String testDataRefine) {
		//
		HashMap<String, Integer> tagInfoMap = new HashMap<String, Integer>();
		HashMap<String, Integer> tagCountInfoMap = new HashMap<String, Integer>();
		try {
			String encoding = "UTF-8";
			File rawTrainDatafile = new File(rawTrainData);
			File rawTestDatafile = new File(rawTestData);
			if (rawTrainDatafile.exists()) {
				BufferedReader rawTrainDataReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(rawTrainDatafile), encoding));
				
				//开始读取训练数据
				System.out.println("Start to process train data info...");
				String tmpURL;
				String tmpTag;
				
				int lineNum=0;
				String tmpLineStr = null;
				while ((tmpLineStr = rawTrainDataReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] trainSentInfo = tmpLineStr.split("\t");
					if (trainSentInfo.length!=2) {
						System.err.print("Error: trainSentInfo.length is "+ trainSentInfo.length 
								+ " in lineNum" + lineNum);
						System.exit(0);
					}
					tmpURL = trainSentInfo[0].trim();
					tmpTag = trainSentInfo[1].trim();
				
					//如果是初次遇到此标签
					int tmpCount = 0;
					int tmpTagNum = 0;
					if (tagCountInfoMap.containsKey(tmpTag)) {
						tmpCount = tagCountInfoMap.get(tmpTag);
					}else {
						tmpTagNum = tagInfoMap.size();
						tagInfoMap.put(tmpTag, tmpTagNum);
						tmpTagNum++;
					}
					tmpCount++;
					tagCountInfoMap.put(tmpTag, tmpCount);
					Result2Txt(trainDataRefine, String.valueOf(tagInfoMap.get(tmpTag)));
										
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed train data numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed train data numbers:" + lineNum);
				rawTrainDataReader.close();
				lineNum = 0;
				if (hasTestData) {
					//开始读取测试数据
					BufferedReader rawTestDataReader = new BufferedReader(new InputStreamReader(
							new FileInputStream(rawTestDatafile), encoding));
					System.out.println("Start to process test data info...");
					while ((tmpLineStr = rawTestDataReader.readLine()) != null) {
						lineNum++;
						if (lineNum<=0) continue;
						String[] testSentInfo = tmpLineStr.split("\t");
						if (testSentInfo.length<2) {
							System.err.print("Error: testSentInfo.length is "+ testSentInfo.length 
									+ " in lineNum" + lineNum);
							System.exit(0);
						}
						tmpURL = testSentInfo[0].trim();
						tmpTag = testSentInfo[1].trim();

						Result2Txt(testDataRefine, String.valueOf(tagInfoMap.get(tmpTag)));
										
						if (lineNum%1000 ==0) {
							System.out.println("hasProcessed test data numbers:" + lineNum);
						}
					}
					System.out.println("Totally processed test data numbers:" + lineNum);
					rawTestDataReader.close();
				}
				
				System.out.println("Start to write all tagInfo into file...");
				Iterator iter = tagInfoMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					tmpTag = (String) entry.getKey();
					tmpTag = tmpTag+"\t"+tagInfoMap.get(tmpTag)+"\t"+tagCountInfoMap.get(tmpTag);
					Result2Txt(tagInfo, tmpTag);
				}
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

		//***********测试区域************
		
		String dataPathStr="./../Data/";
		String resultsPathStr="./../RefineData/";
		File resultsPathFile = new File(resultsPathStr);
		if (!resultsPathFile.exists()) resultsPathFile.mkdir();

		//分析训练数据集
	    String rawTrainData=dataPathStr+"train.dat";
	    String rawTestData=resultsPathStr+"Step04_01_test_resultShow.dat";
	    boolean hasTestData = true;

	    //分析的结果输出
	    String trainDataRefine = resultsPathStr +"Step02_01_train_label.dat";
	    String testDataRefine = resultsPathStr +"Step02_01_test_label.dat";
	    String tagInfo = resultsPathStr +"Step02_01_tag_info.lst";
		
		long readstart=System.currentTimeMillis();
		dataAnalysis(rawTrainData, trainDataRefine, tagInfo, rawTestData, hasTestData, testDataRefine);
        long readend=System.currentTimeMillis();
        System.out.println((readend-readstart)/1000.0+"s had been consumed to process the raw training data");
	}

}
