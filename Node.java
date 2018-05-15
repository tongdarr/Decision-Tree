
public class Node {
	private Node left = null;
	private Node right = null;
	private String attName;
	private String className = null;
	private double probability;
	private boolean isLeaf = false;



	Node(String attName, Node left, Node right) {
		this.attName = attName;
		this.left = left;
		this.right = right;
	}

	public void report(String indent) {
		if(isLeaf) {
			System.out.format("%sClass %s, prob = %s\n", indent, className, probability);
		}
		else {
			if(left != null) {
				System.out.format("%s%s = True:\n", indent, attName);
				left.report(indent + "   ");
			}
			if(right != null) {
				System.out.format("%s%s = False:\n", indent, attName);
				right.report(indent + "   ");	
			}
		}

	}

	public String getAttName() {
		return attName;
	}

	public void setAttName(String attName) {
		this.attName = attName;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
}
