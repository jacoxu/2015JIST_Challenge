package Tools;
import java.io.IOException;

import Tools.StringAnalyzer;
import TypeTrans.Full2Half;

public class PreProcessText {
	static public String preProcess4Task1(String inputStr, String tmpRelationP, String tmpEntityS, String tmpEntityO) throws IOException{
		if (inputStr.length()<1) return inputStr;
		//强制限制了实体词分开
		if (tmpRelationP!=null) {
			if (tmpRelationP.length()==4) { //传闻不和、同为校花、昔日情敌、绯闻女友 等关系 重心词在后面
				if (!inputStr.contains(tmpRelationP)) {
					tmpRelationP = tmpRelationP.substring(2);
				}
			} 
			inputStr = inputStr.replaceAll(tmpRelationP, " "+tmpRelationP+" ");
		}
		inputStr = inputStr.replaceAll(tmpEntityS, " "+tmpEntityS+" ");
		inputStr = inputStr.replaceAll(tmpEntityO, " "+tmpEntityO+" ");
		inputStr=Full2Half.ToDBC(inputStr);//全角转半角						
		inputStr=inputStr.toLowerCase();//字母全部小写
		inputStr=inputStr.replaceAll("\\s+", " ");//多个空格缩成单个空格
		inputStr = StringAnalyzer.extractGoodCharacter(inputStr); //去除所有特殊字符
		//                           无词性                                                                       带词性
		inputStr = WordSegment_Ansj.splitWord(inputStr)+"\t"+WordSegment_Ansj.splitWordwithTag(inputStr);//进行分词
		
		return inputStr;
	} 
	static public String preProcess4Task2(String inputStr) throws IOException{
		if (inputStr.length()<1) return inputStr;
		inputStr=Full2Half.ToDBC(inputStr);//全角转半角						
		inputStr=inputStr.toLowerCase();//字母全部小写
		inputStr=inputStr.replaceAll("\\s+", " ");//多个空格缩成单个空格
		inputStr = StringAnalyzer.extractGoodCharacter(inputStr); //去除所有特殊字符
		//                           无词性                                                                       带词性
		inputStr = WordSegment_Ansj.splitWordwithOutTag4Task2(inputStr);//进行分词
		
		return inputStr.trim();
	} 

	private static boolean isITSuffixSpamInfo(String tmpQuerySnippet, String tmpEntityS, String tmpEntityO) {
		if ((tmpQuerySnippet.contains(tmpEntityS)||tmpQuerySnippet.contains(tmpEntityO))
				&&tmpQuerySnippet.length()>4) {
			return false;
		}else {
			return true;
		}
	}
}
