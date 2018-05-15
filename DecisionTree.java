import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DecisionTree {
	static Node root;

	private static List<Instance> instancesTrain = new ArrayList<Instance>();
	private static List<Instance> instancesTest = new ArrayList<Instance>();

	private static List<String> categoryNames = new ArrayList<String>();

	private static List<String> attributes = new ArrayList<String>();

	DecisionTree() {
	}

	private class Instance {

		private int category;
		private List<Boolean> vals;

		public Instance(int cat, Scanner s) {
			category = cat;
			vals = new ArrayList<Boolean>();
			while (s.hasNextBoolean())
				vals.add(s.nextBoolean());
		}

		public boolean getAtt(int index) {
			return vals.get(index);
		}

		public int getCategory() {
			return category;
		}

		public String toString() {
			StringBuilder ans = new StringBuilder(categoryNames.get(category));
			ans.append(" ");
			for (Boolean val : vals)
				ans.append(val ? "true  " : "false ");
			return ans.toString();
		}

	}
	
	
	// builds the decision tree and then returns the root node
	public static Node buildTree(List<Instance> instances, List<String> attributes) {
		Node left = null;
		Node right = null;
		String bestAttr = null;

		// case 1
		// if instances set is empty
		if (instances.isEmpty()) {
			Node n = new Node(null, null, null);
			n.setLeaf(true);
			int live = 0;
			int die = 0;
			for (int i = 0; i < DecisionTree.instancesTrain.size(); i++) {
				if (categoryNames.get(instancesTrain.get(i).getCategory()).equals("live")) {
					live++;
				} else {
					die++;
				}
			}
			if (live > die) {
				n.setProbability((double) live / (double) DecisionTree.instancesTrain.size());
				n.setClassName("live");
			} else if (live < die) {
				n.setProbability((double) die / (double) DecisionTree.instancesTrain.size());
				n.setClassName("die");
			} else if (live == die) {
				n.setProbability(0.5);
				double x = Math.random();
				if (x < 0.5) {
					n.setClassName("die");
				} else {
					n.setClassName("live");
				}
			}
			return n;
		}

		// case 2
		// check if instances are pure (i.e all in the same class)
		boolean allPure = true;
		int categoryNum = instances.get(0).getCategory();
		for (Instance i : instances) {
			// if there is an nstance that belongs to another class,
			// then break out of for loop and move on to next case
			if (categoryNum != i.getCategory()) {
				allPure = false;
				break;
			}
		}

		// if all pure return leaf node containing name of the class and probability 1
		if (allPure) {
			Node n = new Node(null, null, null);
			n.setProbability(1);
			n.setLeaf(true);
			if (categoryNames.get(categoryNum).equals("live")) {
				n.setClassName("live");
			} else {
				n.setClassName("die");
			}
			return n;
		}

		// case 3, if attributes are empty, return leaf node containing the name and
		// probability of the
		// majority class of the instances in the node (choose randomly if classes are
		// equal)
		if (attributes.isEmpty()) {
			Node n = new Node(null, null, null);
			n.setLeaf(true);
			List<Instance> live = new ArrayList<Instance>();
			List<Instance> die = new ArrayList<Instance>();

			for (Instance i : instances) {
				String s = categoryNames.get(i.getCategory());
				if (s.equals("live")) {
					live.add(i);
				} else if (s.equals("die")) {
					die.add(i);
				}
			}

			if (live.size() > die.size()) {
				n.setProbability((double) live.size() / (double) instances.size());
				n.setClassName("live");
			} else if (die.size() > live.size()) {
				n.setProbability((double) die.size() / (double) instances.size());
				n.setClassName("die");
			} else if (live.size() == die.size()) {
				n.setProbability(0.5);
				double x = Math.random();
				if (x < 0.5) {
					n.setClassName("die");
				} else {
					n.setClassName("live");
				}
			}
			return n;
		}

		// case 4, find best attribute
		boolean stopLeftSplit = false;
		boolean stopRightSplit = false;
		List<Instance> bestInstsTrue = new ArrayList<Instance>();
		List<Instance> bestInstsFalse = new ArrayList<Instance>();
		
		int trueLive = 0;
		int trueDie = 0;
		int falseLive = 0;
		int falseDie = 0;
		
		double avgImpurity = 1;
		for (int i = 0; i < attributes.size(); i++) {
			List<Instance> instsTrue = new ArrayList<Instance>();
			List<Instance> instsFalse = new ArrayList<Instance>();

			// iterate through all instances and split the attribute into true and false
			// sets
			for (int j = 0; j < instances.size(); j++) {
				if (instances.get(j).getAtt(i)) {
					instsTrue.add(instances.get(j));
				} else {
					instsFalse.add(instances.get(j));
				}
			}

			double probabilityFalse = (double) instsFalse.size() / (double) instances.size();
			double probabilityTrue = (double) 1.0 - probabilityFalse;

			// count how many instances from the trueset that are of class live or die
			double impurityTrue = 1;
			double impurityFalse = 1;

			// count how many instances from the trueset that are of class live or die
			for (Instance inst : instsTrue) {
				int cate = inst.getCategory();
				if (categoryNames.get(cate).equals("live")) {
					trueLive++;
				} else if (categoryNames.get(cate).equals("die")) {
					trueDie++;
				}
			}

			// count how many instances from the falseset that are of class live or die
			for (Instance inst : instsFalse) {
				int cate = inst.getCategory();
				if (categoryNames.get(cate).equals("live")) {
					falseLive++;
				} else if (categoryNames.get(cate).equals("die")) {
					falseDie++;
				}
			}
			
			// calculate impurity of true set
			double numerator = 2.0 * trueLive * trueDie;
			double denominator = Math.pow((trueLive + trueDie), 2);
			if (denominator == 0.0) {
				impurityTrue = 0;
				stopLeftSplit = true;
			} else {
				impurityTrue = numerator / denominator;
			}
			
			// calculate impurity of false set
			numerator = 2.0 * falseLive * falseDie;
			denominator = Math.pow((falseLive + falseDie), 2);
			if (denominator == 0.0) {
				impurityFalse = 0;
				stopRightSplit = true;
			} else {
				impurityFalse = numerator / denominator;
			}
			
			double weightedAvg = (probabilityFalse * impurityFalse) + (probabilityTrue * impurityTrue);
			if (weightedAvg < avgImpurity) {
				avgImpurity = ((probabilityFalse * impurityFalse) + (probabilityTrue * impurityTrue));
				bestAttr = attributes.get(i);
				bestInstsTrue = instsTrue;
				bestInstsFalse = instsFalse;
			}
		}

		List<String> newAttributes = new ArrayList<>(attributes);
		newAttributes.remove(bestAttr);
		
		// split both left and right nodes
		if(stopRightSplit == false && stopLeftSplit == false) {
			left = buildTree(bestInstsTrue, newAttributes);
			right = buildTree(bestInstsFalse, newAttributes);
		}
		// split right but stop splitting left
		else if(stopLeftSplit && stopRightSplit == false) {
			right = buildTree(bestInstsFalse, newAttributes);
			Node n = new Node(null, null, null);
			n.setLeaf(true);
			if(trueLive > trueDie) {n.setClassName("live"); n.setProbability((double) trueLive / (double) (trueLive+trueDie));}
			else if(trueLive < trueDie) {n.setClassName("die"); n.setProbability((double) trueDie / (double) (trueLive+trueDie));}
			else {
				n.setProbability(0.5);
				double x = Math.random();
				if (x < 0.5) {
					n.setClassName("die");
				} else {
					n.setClassName("live");
				}
			}
			left = n;
			
		}
		// split left but stop splitting right
		else if(stopRightSplit && stopLeftSplit == false) {
			left = buildTree(bestInstsTrue, newAttributes);
			Node n = new Node(null, null, null);
			n.setLeaf(true);
			if(falseLive > falseDie) {n.setClassName("live"); n.setProbability((double) falseLive / (double) (falseLive+falseDie));}
			else if(trueLive < trueDie) {n.setClassName("die"); n.setProbability((double) falseDie / (double) (falseLive+falseDie));}
			else {
				n.setProbability(0.5);
				double x = Math.random();
				if (x < 0.5) {
					n.setClassName("die");
				} else {
					n.setClassName("live");
				}
			}
			right = n;
		}
		
		return new Node(bestAttr, left, right);
	}

	public static void main(String args[]) {
		// the arguments that must be provided are the training set and test set respectively
		BufferedReader readerTraining = null;
		BufferedReader readerTest = null;
		Scanner sc = null;

		try {
			File f1 = new File(args[0]);
			File f2 = new File(args[1]);
			readerTraining = new BufferedReader(new FileReader(f1));
			readerTest = new BufferedReader(new FileReader(f2));

			String lineTrain;
			String lineArrayTrain[] = new String[1000];
			int counterTrain = 1;

			// while there are still lines to be read in file
			while ((lineTrain = readerTraining.readLine()) != null) {
				lineArrayTrain = lineTrain.split("\n");
				sc = new Scanner(lineArrayTrain[0]);

				if (counterTrain == 3) {
					while (sc.hasNext()) {
						DecisionTree dt = new DecisionTree();
						instancesTrain.add(dt.new Instance(categoryNames.indexOf(sc.next()), sc));
					}
				}

				// read the classes/labels
				else if (counterTrain == 1) {
					while (sc.hasNext()) {
						categoryNames.add(sc.next());
						categoryNames.add(sc.next());
					}
					counterTrain++;
				}

				// read the attributes
				else if (counterTrain == 2) {
					while (sc.hasNext()) {
						attributes.add(sc.next());
					}
					counterTrain++;
				}
			}

			String lineTest;
			String lineArrayTest[] = new String[1000];
			int counterTest = 1;

			// while there are still lines to be read in file
			while ((lineTest = readerTest.readLine()) != null) {
				lineArrayTest = lineTest.split("\n");
				sc = new Scanner(lineArrayTest[0]);

				if (counterTest == 3) {
					while (sc.hasNext()) {
						DecisionTree dt = new DecisionTree();
						instancesTest.add(dt.new Instance(categoryNames.indexOf(sc.next()), sc));
					}
				}

				// read the classes/labels
				else if (counterTest == 1) {
					while (sc.hasNext()) {
						sc.next();
						sc.next();
					}
					counterTest++;
				}

				// read the attributes
				else if (counterTest == 2) {
					while (sc.hasNext()) {
						sc.next();
					}
					counterTest++;
				}
			}

			sc.close();
		} catch (IOException e) {
			System.out.println("invalid file");
		}
		root = buildTree(DecisionTree.instancesTrain, DecisionTree.attributes);

		 root.report(" ");

		// for each instance in the test instance traverse the decision tree and find
		// class
		int counter = 0;
		for (int i = 0; i < instancesTest.size(); i++) {
			String result = traversal(instancesTest.get(i), root);
			if (result.equals(categoryNames.get(instancesTest.get(i).getCategory()))) {
				counter++;
			}
		}
		System.out.println(counter + " out of " + instancesTest.size() + " classified correctly");
		System.out.println("Accuracy = " + (double) counter / instancesTest.size());
		
	}

	// find the class name of the test instance in the tree
	public static String traversal(Instance i, Node n) {
		String result = null;
		if (n.isLeaf()) {
			return n.getClassName();
		} else {
			// find what the current attribute is
			String currentAttr = n.getAttName();

			// look for the index of the attribute, then find out whether it is true or false
			int index = attributes.indexOf(currentAttr);
			boolean b = i.getAtt(index);

			if (b) {
				result = traversal(i, n.getLeft());
			} 
			else {
				result = traversal(i, n.getRight());
			}
		}
		return result;
	}

}
