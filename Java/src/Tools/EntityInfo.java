package Tools;

import java.util.ArrayList;

public class EntityInfo {
	public String abstractStr = null;
	public boolean hasAbstractStr = false;

	public ArrayList<String> infoBoxList = new ArrayList<String>();
	public boolean hasInfoBoxList = false;
	
	public String getInfoStr(){
		StringBuffer tmpResultBuf = new StringBuffer();
		if (hasAbstractStr) {
			tmpResultBuf.append("\t");
			tmpResultBuf.append(abstractStr);
		}
		if (hasInfoBoxList) {
			for (int i = 0; i < infoBoxList.size(); i++) {
				tmpResultBuf.append("\t");
				tmpResultBuf.append(infoBoxList.get(i));
			}
		}
		return tmpResultBuf.toString();
	}
}
