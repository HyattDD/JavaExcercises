import java.io.File;
import java.util.*;

// Multi-forked Tree Structure
public class MTreeNode {
    private String nodeName;
    private String hashValue;
    private MTreeNode parent;
    private ArrayList<MTreeNode> childList;

    // when we build the fileName tree, we can just know the name of a blob 
    // or a tree, so the constructor is just for nodeName
    // each node's hashValue can be accessed by post-root-travel
    public MTreeNode() {}

    public MTreeNode(String nodeName) {
        this.nodeName = nodeName;
        this.hashValue = new String();
        this.parent = new MTreeNode();
        this.childList = new ArrayList<MTreeNode>();
    }


    // setters

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public void setParent(MTreeNode parent) {
        this.parent = parent;
    }

    public void setChildList(ArrayList<MTreeNode> childList) {
        this.childList = childList;
        for (MTreeNode node : childList) {
            node.setParent(this);
        }
    }

    // getters

    public String getHashValue() {
        return this.hashValue;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public MTreeNode getParent() {
        return this.parent;
    }

    public ArrayList<MTreeNode> getChildren() {
        return childList;
    }

    public boolean isLeaf() {
        if (childList.isEmpty())
            return true;
        return false;
    }

    public boolean hasChildNamed(String name) {
        boolean has = false;
        if (childList.equals(null)) return has;
        for (MTreeNode node : childList) {
            if (node.getNodeName().equals(name)) has = true;
        }
        return has;
    }

    public MTreeNode getChildNamed(String name) {
        for (MTreeNode node : childList) {
            if (node.getNodeName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public ArrayList<MTreeNode> getChildList() {
        return childList;
    }


    /*
    since the node just stores the single name rather than a file's relativePath
    we use a func to get the real indexItemName of a file, for example:
    a filename stored in node : tree.java 
    its real name in IndexFile: javaProject/src/tree.java
    */
    public String getIndexName(MTreeNode node) {
        if (node.getNodeName().equals("root")) return "root";
        String name = node.getNodeName();
        String sep = File.separator;
        if (!(node.getParent().getNodeName().equals("root"))) {
            name = getIndexName(node.getParent()) + sep + name;
        }
        return name;
    }

    public boolean addChild(MTreeNode node) {
        childList.add(node);
        node.setParent(this);
        return true;
    }

    /**
     * post travel
     */

    public LinkedList<MTreeNode> postOrder() {
        Stack<MTreeNode> stack = new Stack<>();
        LinkedList<MTreeNode> post = new LinkedList<>();
        stack.add(this);
        while (!stack.isEmpty()) {
            MTreeNode node = stack.pop();
            post.addFirst(node);
            stack.addAll(node.childList);
        }
        return post;
    }
}



