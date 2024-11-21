package part2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DecisionTree {
	/**
	 * Start running the algorithm
	 * @param trainingFileName File name of all the data
	 * @param outputFile The file to output the tree
	 */
	public void run(String trainingFileName,  String outputFile) {
		List <Data> trainingData = loadData(trainingFileName);
		//Get all data names and store in map.
		Map <String, Integer> dataNames = new HashMap <String, Integer>();
		for (Map.Entry<String, Integer> m : trainingData.get(0).getAttributesMap().entrySet()) {
			dataNames.put(m.getKey(), m.getValue());
		}
		//Start building the tree
		Node root = buildTree(trainingData, dataNames);
		//Get tree structure using Strings and output result to output file.
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder = printTree(root, "", stringBuilder);
		try {
			FileWriter writer = new FileWriter(outputFile);
			writer.write(stringBuilder.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Get the accuracy of our decision tree
		double numberOfCorrectPredictions = 0;
		for (int i = 0; i < trainingData.size(); i++) {
			int predict = traverseTree(root, trainingData.get(i));
			if (trainingData.get(i).getClassOfData() == predict) {
				numberOfCorrectPredictions++;
			}	
		}
		//Print the accuracy
		System.out.println("Clasification accuracy: " + numberOfCorrectPredictions/trainingData.size()*100);
	}
	/**
	 * Traverse through tree to make prediction of class label.
	 * @param n The node to traverse through
	 * @param d Row of data 
	 */
	public int traverseTree(Node n, Data d) {
		int value = -1;
		//If node is leaf, get class label of that node.
		if (n.isLeaf()) {
			for (Map.Entry<Integer, Integer> m : n.getValue().entrySet()) {
				if (m.getValue() != 0) {
					return m.getKey();
				}
			}
		}
		//Continue traversing until leaf node has been reached
		else {
			value = traverseTree(n.getChildren().get(d.getAttributesMap().get(n.getAttribute())), d);	
		}
		return value;
	}
	/**
	 * Traverse through tree to make prediction of class label.
	 * @param n The node to traverse through
	 * @param whiteSpace For indentation purposes
	 * @param stringBuilder For building up the tree using strings
	 */
	public StringBuilder printTree(Node n, String whiteSpace, StringBuilder stringBuilder) {
		if (!n.isLeaf()) {
			stringBuilder.append(whiteSpace + n.getAttribute() + " (IG:" + n.getInformationGain() + ", Entropy:" + n.getEntropy() + ")").append("\n");
			for (Map.Entry<Integer, Node> m : n.getChildren().entrySet()) {
				stringBuilder.append(whiteSpace + "--" + n.getAttribute()  + " == " + m.getKey() + "--").append("\n");
				stringBuilder = printTree(m.getValue(), whiteSpace + "    ", stringBuilder);
			}	
		}
		else {
			stringBuilder.append(whiteSpace + "leaf {");
			for (Map.Entry<Integer, Integer> m : n.getValue().entrySet()) {
				stringBuilder.append(m.getKey() + ":" + m.getValue());
				if (m.getKey() == 0) {
					stringBuilder.append(", ");
				}
			}
			stringBuilder.append("}").append("\n");
			return stringBuilder;
		}
		return stringBuilder;
		
	}
	/** Runs the ID3 decision tree algorithm
	 * @param trainingData Contains all the training data
	 * @param dataNames Contains all attribute names
	 */
	public Node buildTree(List <Data> trainingData, Map <String, Integer> dataNames) {
		List <Integer> uniqueValues = findUniqueValues(trainingData);
		Collections.sort(uniqueValues);
		//Stopping conditions for the recursion
		List <String> attributesList = new ArrayList <String>();
		for (Map.Entry<String, Integer> m : dataNames.entrySet()) {
			attributesList.add(m.getKey());
		}
		//If all data are same
		int sameClass = 0;
		for (Data d: trainingData) {
			if (trainingData.get(0).getClassOfData() == d.getClassOfData()) {
				sameClass++;
			}
		}
		if (sameClass == trainingData.size()) {
			Map <Integer, Integer> mapOfValues = findLeafValue(trainingData);
			Node leaf = new Node(mapOfValues);
			return leaf;
		}
		Map <String, Double> attributeToInformationGain = new HashMap <String, Double>();
		//calculate entropy of parent
		double entropyOfRoot = 0;
		double classOf0 = 0;
		double classOf1 = 0;
		for (Data d : trainingData) {
			if (d.getClassOfData() == 0) {
				classOf0++;
			}
			else {
				classOf1++;
			}
		}
		entropyOfRoot = -(classOf0/trainingData.size())*(Math.log(classOf0/trainingData.size())/Math.log(2)) - (classOf1/trainingData.size())*(Math.log(classOf1/trainingData.size())/ Math.log(2));
		//Loop through each attribute to decide what to split on
		for (int i = 0; i < dataNames.size(); i++) {
			//Map containing total number of data belonging to specific value 
			//eg. How many data points of att 2 has value 0
			Map <Integer, Double> valueMap = new HashMap <Integer, Double>();
			//Map containing attribute with a specific class, like value of attribute 
			//for a specific row of data is 0 and class label is 0.
			////eg. How many data points of att2 has value 0 and class label is 0 and so on.
			Map <List <Integer> , Double> attributeBelongToClassToMap = new HashMap <List <Integer>, Double>();
			//default values 0 and 1 will be put in map
			valueMap.put(0, 0.0);
			valueMap.put(1, 0.0);
			//Map where value of particular attribute is 0, and the class it belongs to is 0
			attributeBelongToClassToMap.put(List.of(0, 0), 0.0);
			//Map where value of particular attribute is 0, and the class it belongs to is 1
			attributeBelongToClassToMap.put(List.of(0, 1), 0.0);
			//Map where value of particular attribute is 1, and the class it belongs to is 0
			attributeBelongToClassToMap.put(List.of(1, 0), 0.0);
			//Map where value of particular attribute is 1, and the class it belongs to is 1
			attributeBelongToClassToMap.put(List.of(1, 1), 0.0);
			//add additional ones in case of multi way split
			for (int a = 0; a < uniqueValues.size(); a++) {
				if (uniqueValues.get(a) > 1) {
					valueMap.put(uniqueValues.get(a), 0.0);
					attributeBelongToClassToMap.put(List.of(uniqueValues.get(a), 0), 0.0);
					attributeBelongToClassToMap.put(List.of(uniqueValues.get(a), 1), 0.0);
				}
			}
			for (Data d: trainingData) {
				for (int a = 0; a < uniqueValues.size(); a++) {
					//find the value of current attribute and current row of data being loop, and find class label of that row of data
					//and add it to the relevant maps
					if (d.getAttributesMap().get(attributesList.get(i)) == uniqueValues.get(a)) {
						if (d.getClassOfData() == 0) {
							attributeBelongToClassToMap.put(List.of(uniqueValues.get(a), 0), attributeBelongToClassToMap.get(List.of(uniqueValues.get(a), 0)) + 1);
						}
						else {
							attributeBelongToClassToMap.put(List.of(uniqueValues.get(a), 1), attributeBelongToClassToMap.get(List.of(uniqueValues.get(a), 1)) + 1);
						}
						valueMap.put(uniqueValues.get(a), valueMap.get(uniqueValues.get(a))+1);
					}
				}
			}
			Map <Integer, Double> entropyValues = findEntropy(uniqueValues, valueMap, attributeBelongToClassToMap);
			List <String> attributes = new ArrayList<String>(dataNames.keySet());
			double informationGain = entropyOfRoot;
			//Find information gain for each attribute to split on
			for (Map.Entry<Integer, Double> m : entropyValues.entrySet()) {
				if (m.getValue()!=0.0) {
					informationGain = informationGain - valueMap.get(m.getKey())/trainingData.size() * m.getValue();	
				}
			}
			attributeToInformationGain.put(attributes.get(i), informationGain);
		}
		//Find attribute with highest information gain.
		double max = 0;
		String maxAttribute = "";
		double minimalEntropy = Double.MAX_VALUE;
		for (Map.Entry<String, Double> m : attributeToInformationGain.entrySet()) {
			if (m.getValue() > max) {
				max = m.getValue();
				maxAttribute = m.getKey();
			}
			if (m.getValue() + entropyOfRoot < minimalEntropy) {
				minimalEntropy = m.getValue() + entropyOfRoot;
			}
		}
		if (max < 0.00001) {
			Map <Integer, Integer> mapOfValues = findLeafValue(trainingData);
			Node leaf = new Node(mapOfValues);
			return leaf;
		}
		//Set node with this attribute that maximises information gain. Remove it from the list of attributes
		//ideally remove ith attribute from all data sets so 
		//that when finding entropy will be easier.
		Node n = new Node(maxAttribute, max, minimalEntropy);
		//Split data
		List <Data> leftSplit = new ArrayList <Data>();
		List <Data> rightSplit = new ArrayList <Data>();
		for (Data d: trainingData) {
			Map <String, Integer> attributes = d.getAttributesMap();
			if (attributes.get(maxAttribute) == 0) {
				leftSplit.add(d);
			}
			else {
				rightSplit.add(d);
			}
		}
		//For multi value split when attribute value isnt 0 or 1.
		Map <Integer, List <Data>> attributeValueAmount = new HashMap <Integer, List <Data>>();
		for (int i = rightSplit.size()-1; i >= 0; i--) {
			if (rightSplit.get(i).getAttributesMap().get(maxAttribute) != 1) {
				if (!attributeValueAmount.containsKey(rightSplit.get(i).getAttributesMap().get(maxAttribute))) {
					List <Data> data = new ArrayList <Data>();
					data.add(rightSplit.get(i));
					attributeValueAmount.put(rightSplit.get(i).getAttributesMap().get(maxAttribute), data);
				}
				else {
					attributeValueAmount.put(rightSplit.get(i).getAttributesMap().get(maxAttribute), attributeValueAmount.get(rightSplit.get(i).getAttributesMap().get(maxAttribute))).add(rightSplit.get(i));
				}
				rightSplit.remove(i);
			}
		}
		for (Map.Entry<Integer, List <Data>> a : attributeValueAmount.entrySet()) {
			Node node = buildTree(a.getValue(), dataNames);
			n.getChildren().put(a.getKey(), node);
		}
		Node nodeLeft = buildTree(leftSplit, dataNames);
		Node nodeRight = buildTree(rightSplit, dataNames);
		n.getChildren().put(0, nodeLeft);
		n.getChildren().put(1, nodeRight);
		return n;
	}
	/**
	 * Calculate entropy
	 * @param uniqueValues The different number of values for a given attribute
	 * @param valueMap Map containing total number of data belonging to specific value 
	 * @param attributeBelongToClassToMap Map containing attribute with a specific class like attribute is 0 and class label is 0.
	 */
	public Map <Integer, Double> findEntropy(List <Integer> uniqueValues, Map <Integer, Double> valueMap, Map <List <Integer> , Double> attributeBelongToClassToMap){
		Map <Integer, Double> entropyValues = new HashMap <Integer, Double>();
		//Set entropy to 0 by default
		for (Integer value : uniqueValues) {
			entropyValues.put(value, 0.0);
		}
		//Check if attribute only has 0s or 1s like att3 in rtg_A.
		//If so, entropy should remain zero.
		for (int a = 0; a < uniqueValues.size(); a++) {
			if (valueMap.get(a)!=null) {
				if ((valueMap.get(a) != 0 && -attributeBelongToClassToMap.get(List.of(a, 0))/valueMap.get(a)!=0 && -attributeBelongToClassToMap.get(List.of(a, 1))/valueMap.get(a)!=0)) {
					entropyValues.put(a, (-attributeBelongToClassToMap.get(List.of(a, 0))/valueMap.get(a))*(Math.log(attributeBelongToClassToMap.get(List.of(a, 0))/valueMap.get(a))/Math.log(2)) - ((attributeBelongToClassToMap.get(List.of(a, 1))/valueMap.get(a))*(Math.log(attributeBelongToClassToMap.get(List.of(a, 1))/valueMap.get(a)) / Math.log(2))));
				}
			}	
		}
		return entropyValues;
	}
	/**
	 * We want DT to allow mutli way splits. We will go through 
	 * each attribute and find the number of unique values
	 * @param trainingData The whole training data represented as a data object
	 */
	public List <Integer> findUniqueValues(List <Data> trainingData) {
		List <Integer> uniqueValues = new ArrayList <Integer>();
		for (Data d : trainingData) {
			for (Map.Entry <String, Integer> m : d.getAttributesMap().entrySet()) {
				if (!uniqueValues.contains(m.getValue())) {
					uniqueValues.add(m.getValue());
				}
			}
		}
		return uniqueValues;
	}
	/**
	 * At a leaf node find the number of instances that reached
	 * that particular leaf node belonging to a specific class
	 * @param data The whole training data represented as a data object
	 */
	public Map <Integer, Integer> findLeafValue(List <Data> data) {
		//Map of class value (0 or 1) to how many data belong to that attribute value
		Map <Integer, Integer> mapOfValues = new HashMap <Integer, Integer>();
		mapOfValues.put(0, 0);
		mapOfValues.put(1, 0);
		for (Data d : data) {
			if (d.getClassOfData() == 0) {
				mapOfValues.put(0, mapOfValues.get(0) + 1);
			}
			else {
				mapOfValues.put(1, mapOfValues.get(1) + 1);
			}
		}
		return mapOfValues;
	}
	/**
	 * Load the data into a list of data with each row being a data object
	 * @param dataFileName Filename containing the training data
	 */
	public List <Data> loadData(String dataFileName) {
		List <Data> trainingData = new ArrayList <Data>();
		List <Integer> attributes = new ArrayList <Integer>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFileName));
			//read first line without storing as it is just attributes as first line
			reader.readLine();
			String line = reader.readLine();
			while (line != null) {
				Map <String, Integer> attributesMap = new HashMap <String, Integer>();
				String [] rows = line.split(",");
				Integer [] row = new Integer [rows.length];
				//convert the data to double and store it.
				for (int i = 0; i < rows.length; i++) {
					row[i] = (Integer.parseInt(rows[i]));
					attributes.add(row[i]);
				}
				for (int i = 0; i < attributes.size()-1; i++) {
					attributesMap.put("att" + i, attributes.get(i));
				}
				trainingData.add(new Data(attributesMap, attributes.get(attributes.size()-1)));
				attributes.clear();
				//read next line in csv
				line = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trainingData;
		
	}
	public static void main(String [] args) {
		DecisionTree dt = new DecisionTree();
		dt.run(args[0], args[1]);
		}
	}
