package part2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {
	private Integer classOfData;
	private Map <String, Integer> attributesMap;
	public Data(Map <String, Integer> attributesMap, Integer classOfData) {
		this.classOfData = classOfData;
		this.attributesMap = attributesMap;
	}
	public Map<String, Integer> getAttributesMap(){
		return attributesMap;
	}
	public Integer getClassOfData() {
		return classOfData;
	}
}
