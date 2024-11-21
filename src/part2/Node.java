package part2;

import java.util.HashMap;
import java.util.Map;

public class Node {
	private String attribute;
	private Map<Integer, Node> children = new HashMap <Integer, Node>();
	private Double entropy;
	private Double informationGain;
	private boolean isLeaf = false;
	//Leaf node value
	private Map <Integer, Integer> value;
	public Node(String attribute, Double informationGain, Double entropy) {
		this.attribute = attribute;
		this.informationGain = informationGain;
		this.entropy = entropy;
	}
	//Constructor for leaf node
	public Node(Map <Integer, Integer> value) {
		this.value = value;
		isLeaf = true;
	}
	public Double getInformationGain() {
		return informationGain;
	}
	public Double getEntropy() {
		return entropy;
	}
	public Map<Integer, Node> getChildren(){
		return children;
	}
	//Map of class label to number of nodes with that class label
	public Map <Integer, Integer> getValue(){
		return value;
	}
	public String getAttribute() {
		return attribute;
	}
	public boolean isLeaf() {
		return isLeaf;
	}
}
