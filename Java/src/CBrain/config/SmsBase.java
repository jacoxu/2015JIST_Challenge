package CBrain.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import Tools.TagInfo;

public class SmsBase {
	public static boolean hasLoadConfig = false;
	//工程中使用的一些重定义
	public static String encode = "UTF-8";
	public static String abstractPrefix = "/abstract> \"";
	public static String abstractProfix = "\"@zh .";
	public static String infoBoxProfix = "\"@zh .";
	public static String abstractTagStr = "\t摘要\t";
	
	//停用词表
	private static HashSet<String> stopWordSet = new HashSet<String>();
	public static HashMap<Integer, TagInfo> tagInfoMap = new HashMap<Integer, TagInfo>();
	
	public static void SmsBaseLoadConfig() throws Exception {
		hasLoadConfig = true;
		//下面的函数没有使用，因为竞赛禁止使用任何外部语料
		System.out.println("+++{Start load the config}+++");
		File stopWordFile = new File("./library/stopDict.dic");
		loadStopWordSet(stopWordFile);
		File tagInfoFile = new File("./../RefineData/tag_info.lst");
		loadTagInfoMap(tagInfoFile);
	}
	private static void loadTagInfoMap(File tagInfoFile) throws IOException {
		FileInputStream fis3 = null;
		try {
			fis3 = new FileInputStream(tagInfoFile);
		} catch (FileNotFoundException fnfe) {
			System.out.println("not found tagInfoFile!");
		}
        try {
        	BufferedReader reader3 = new BufferedReader(new InputStreamReader(fis3, "UTF-8"));
            String tempString = null;
            while ((tempString = reader3.readLine()) != null) {
                TagInfo tmpTagInfo = new TagInfo();
            	String[] tmpStrs = tempString.split("\t");
            	tmpTagInfo.name = tmpStrs[0].trim();
            	tmpTagInfo.num = Integer.valueOf(tmpStrs[2].trim());
            	String[] tmpKeywords = tmpStrs[3].trim().split("\\s+");
            	for (int i = 0; i < tmpKeywords.length; i++) {
            		tmpTagInfo.keywordList.add(tmpKeywords[i].trim());
				}
            	tagInfoMap.put(Integer.valueOf(tmpStrs[1].trim()), tmpTagInfo);
            }
            System.out.println("load tagInfoFile... done");
            reader3.close();
        }  finally {
            fis3.close();
        }
	}
	private static void loadStopWordSet(File stopWordFile) throws IOException {
		FileInputStream fis3 = null;
		try {
			fis3 = new FileInputStream(stopWordFile);
		} catch (FileNotFoundException fnfe) {
			System.out.println("not found stopDict!");
		}
        try {
        	BufferedReader reader3 = new BufferedReader(new InputStreamReader(fis3, "UTF-8"));
            String tempString = null;
            while ((tempString = reader3.readLine()) != null) {
                stopWordSet.add(tempString);
            }
            System.out.println("load stop dictionary... done");
            reader3.close();
        }  finally {
            fis3.close();
        }
	}
	public static HashSet<String> getStopWordSet(){
		return stopWordSet;
	}
	
	public static void main (String [] args){
		String ss ="℃＄¤￠￡‰§№☆★○●◎◇◆□■△▲※→←↑↓〓ⅰⅱ我ⅲⅳⅴⅵⅶ北 京ⅷⅸⅹ⒈⒉，你呢？⒊⒋⒌⒍⒎⒏";
		StringBuffer str = new StringBuffer();
		char test1 = 0x25;
		System.out.println(test1);    
		char[] ch = ss.toCharArray();

		System.out.println(ss);
		System.out.println(str.toString());
	}
}
