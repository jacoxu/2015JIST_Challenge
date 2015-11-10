package DataPreprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import Tools.TagInfo;

public class Step04_04_InfoBoxStat {
	/**
	 * @param args
	 * @author jacoxu.com-2015/10/24
	 * @param hasTestData 
	 * @param tagInfoList 
	 * @param testDataRefine 
	 * @param task1DataTermSet 
	 */
	//infoBoxData, tagInfoList, trainDescData, infoBoxStat
	private static void dataAnalysis(String infoBoxData, String tagInfoList
			, String trainDescData, String infoBoxStat) {
		//
		HashMap<Integer, String> tagInfoMap = new HashMap<Integer, String>();
		HashSet<String> infoBoxTopNSet = new HashSet<String>();
		int topN = 50;
		HashMap<String, TagInfo> infoBoxStatMap = new HashMap<String, TagInfo>();
		try {
			String encoding = "UTF-8";
			File infoBoxDatafile = new File(infoBoxData);
			File tagInfoListfile = new File(tagInfoList);
			File trainDescFile = new File(trainDescData);
			if (infoBoxDatafile.exists()) {
				int lineNum = 0;
				String tmpLineStr = null;
				//开始读取Tag列表信息
				BufferedReader tagInfoListReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(tagInfoListfile), encoding));
				System.out.println("Start to process tagInfo data info...");
				while ((tmpLineStr = tagInfoListReader.readLine()) != null) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] tagInfoArrays = tmpLineStr.split("\t");
					if (tagInfoArrays.length<3) {
						System.err.print("Error: tagInfoArrays.length is "+ tagInfoArrays.length 
								+ " in lineNum" + lineNum);
						System.exit(0);
					}
					String tmpTag = tagInfoArrays[0].trim();
					int tmpTagNum = Integer.valueOf(tagInfoArrays[1].trim());
					TagInfo tmpTagInfo = new TagInfo();
					tmpTagInfo.name = tmpTag;
					tmpTagInfo.num = tmpTagNum;
					tagInfoMap.put(tmpTagNum, tmpTag);
					infoBoxStatMap.put(tmpTag, tmpTagInfo);
					
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed tagInfo data numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed tagInfo data numbers:" + lineNum);
				tagInfoListReader.close();

				//开始读取InfoBox属性信息
				BufferedReader infoBoxDataReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(infoBoxDatafile), encoding));
				System.out.println("Start to process infoBox data info...");
				lineNum=0;
				while (((tmpLineStr = infoBoxDataReader.readLine()) != null)&&(lineNum<topN)) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] infoBoxArray = tmpLineStr.split("<->");
					if (infoBoxArray.length!=2) {
						System.err.print("Error: infoBoxArray.length is "+ infoBoxArray.length 
								+ " in lineNum" + lineNum);
						System.exit(0);
					}
					String tmpInfoBox = infoBoxArray[0].trim();
					infoBoxTopNSet.add(tmpInfoBox);
					for (int i = 0; i < tagInfoMap.size(); i++) {
						TagInfo tmpTagInfo = infoBoxStatMap.get(tagInfoMap.get(i));
						tmpTagInfo.unusedInfoBox.add(tmpInfoBox);
						infoBoxStatMap.put(tagInfoMap.get(i), tmpTagInfo);
					}

					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed infoBoxArray data numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed infoBoxArray data numbers:" + lineNum);
				infoBoxDataReader.close();
				
				//开始基于训练语料统计各个类别的属性信息
				BufferedReader trainDescReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(trainDescFile), encoding));
				System.out.println("Start to process trainDesc info...");
				lineNum=0;
				while (((tmpLineStr = trainDescReader.readLine()) != null)) {
					lineNum++;
					if (lineNum<=0) continue;
					String[] trainDescArrays = tmpLineStr.split("\t");
					if (trainDescArrays.length<4) {
						System.err.print("Error: trainDescArrays.length is "+ trainDescArrays.length 
								+ " in lineNum" + lineNum);
						System.exit(0);
					}
					String tmpTag = trainDescArrays[1].trim();
					TagInfo tmpTagInfo = infoBoxStatMap.get(tmpTag);
					
					for (int i = 4; i < trainDescArrays.length; i+=2) {
						String tmpInfoBox = trainDescArrays[i].trim();
						if (!infoBoxTopNSet.contains(tmpInfoBox)) continue;
						tmpTagInfo.unusedInfoBox.remove(tmpInfoBox);
						tmpTagInfo.usedInfoBox.add(tmpInfoBox);
					}
					infoBoxStatMap.put(tmpTag, tmpTagInfo);
					
					if (lineNum%1000 ==0) {
						System.out.println("hasProcessed trainDesc data numbers:" + lineNum);
					}
				}
				System.out.println("Totally processed trainDesc data numbers:" + lineNum);
				trainDescReader.close();
							
				
				System.out.println("Start to write all infoBoxStatMap into file...");
				for (int i = 0; i < tagInfoMap.size(); i++) {
					TagInfo tmpTagInfo = infoBoxStatMap.get(tagInfoMap.get(i));
					StringBuffer tmpBuffer = new StringBuffer();
					tmpBuffer.append(tagInfoMap.get(i));
					tmpBuffer.append("<->");
					Iterator tmpUsedIterator = tmpTagInfo.usedInfoBox.iterator();
					int addNum = 0;
					while (tmpUsedIterator.hasNext()) {
						if (addNum>0) {
							tmpBuffer.append(" ");
						}
						String tmpUsedInfoBox = (String) tmpUsedIterator.next();
						tmpBuffer.append(tmpUsedInfoBox);
						addNum++;
					}
					tmpBuffer.append("<->");
					Iterator tmpUnusedIterator = tmpTagInfo.unusedInfoBox.iterator();
					addNum = 0;
					while (tmpUnusedIterator.hasNext()) {
						if (addNum>0) {
							tmpBuffer.append(" ");
						}
						String tmpUnusedInfoBox = (String) tmpUnusedIterator.next();
						tmpBuffer.append(tmpUnusedInfoBox);
						addNum++;
					}
					Result2Txt(infoBoxStat, tmpBuffer.toString());
					tmpBuffer.delete(0, tmpBuffer.length());
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

		//分析数据
	    String infoBoxData = resultsPathStr+"Step02_02_infoBox.lst";
	    String tagInfoList=resultsPathStr+"Step02_01_tag_info.lst";
	    String trainDescData=resultsPathStr+"Step02_03_Train_desc.dat";

	    //分析的结果输出
	    String infoBoxStat = resultsPathStr +"Step04_04_infoBox.stat";
		
		long readstart=System.currentTimeMillis();
		dataAnalysis(infoBoxData, tagInfoList, trainDescData, infoBoxStat);
        long readend=System.currentTimeMillis();
        System.out.println((readend-readstart)/1000.0+"s had been consumed to process the raw training data");
	}

}
