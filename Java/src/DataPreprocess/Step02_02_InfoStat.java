package DataPreprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import CBrain.config.SmsBase;

public class Step02_02_InfoStat {
	/**
	 * @param args
	 * @author jacoxu.com-2015/10/24
	 * @param hasTestData 
	 * @param rawTestData 
	 * @param testDataRefine 
	 * @param task1DataTermSet 
	 */
	//rawTrainData, dataRefine, tagInfo
	private static void dataAnalysis(String rawInfoBox, String infoBoxList) {
		//
		HashMap<String, Integer> infoBoxCountMap = new HashMap<String, Integer>();
		try {
			String encoding = "UTF-8";
			File rawInfoBoxFile = new File(rawInfoBox);

			if (rawInfoBoxFile.exists()) {
				BufferedReader rawInfoBoxReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(rawInfoBoxFile), encoding));
				
				//开始读取训练数据
				System.out.println("Start to process infoBox...");
				String tmpInfoBox;
				
				int lineNum=0;
				String tmpLineStr = null;
				while ((tmpLineStr = rawInfoBoxReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] trainSentInfo = tmpLineStr.split("\\s+");
					if (trainSentInfo.length<2) {
						System.err.print("Error: trainSentInfo.length is "+ trainSentInfo.length 
								+ " in lineNum" + lineNum);
						System.exit(0);
					}
					tmpInfoBox = trainSentInfo[1].trim();
					tmpInfoBox = tmpInfoBox.substring(1, tmpInfoBox.length()-1);
					tmpInfoBox = tmpInfoBox.substring(tmpInfoBox.lastIndexOf("/")+1);
					tmpInfoBox = URLDecoder.decode(tmpInfoBox, SmsBase.encode);
					//如果是初次遇到此InfoBox
					int tmpCount = 0;
					if (infoBoxCountMap.containsKey(tmpInfoBox)) {
						tmpCount = infoBoxCountMap.get(tmpInfoBox);
					}
					tmpCount++;
					infoBoxCountMap.put(tmpInfoBox, tmpCount);
										
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed train data numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed train data numbers:" + lineNum);
				rawInfoBoxReader.close();
								
				System.out.println("Start to write all infoBox into file...");
				List<Map.Entry<String, Integer>> infoMapList =   
				        new ArrayList<Map.Entry<String, Integer>>(infoBoxCountMap.entrySet());   
				Collections.sort(infoMapList, new Comparator<Map.Entry<String, Integer>>() {      
				    @Override
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {         
				        return (o2.getValue() - o1.getValue());    
				        //return (o1.getKey()).toString().compareTo(o2.getKey());   
				    }   
				});    
				for (int i = 0; i < infoMapList.size(); i++) {   
					tmpInfoBox = infoMapList.get(i).getKey();   
				    int infoFrq = infoMapList.get(i).getValue();   
				    Result2Txt(infoBoxList, tmpInfoBox+"<->"+infoFrq);
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
		String testA = "<http://zhishi.me/baidubaike/property/%E5%9C%B0%E7%90%86%E4%BD%8D%E7%BD%AE>";
		testA = testA.substring(1,testA.length()-1);
		testA = testA.substring(testA.lastIndexOf("/")+1);
		//***********测试区域************
		
		String dataPathStr="./../Data/";
		String resultsPathStr="./../RefineData/";
		File resultsPathFile = new File(resultsPathStr);
		if (!resultsPathFile.exists()) resultsPathFile.mkdir();

		//分析训练数据集
	    String rawInfoBox=dataPathStr+"zhishime_infobox_properties_zh_enc.nt";

	    //分析的结果输出
	    String infoBoxList = resultsPathStr +"Step02_02_infoBox.lst";
		
		long readstart=System.currentTimeMillis();
		dataAnalysis(rawInfoBox, infoBoxList);
        long readend=System.currentTimeMillis();
        System.out.println((readend-readstart)/1000.0+"s had been consumed to process the raw training data");
	}

}
