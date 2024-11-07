package guild.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.*;

/**
 * Represents a Tree of Objects of generic type T. The Tree is represented as
 * a single rootElement which points to a List<Node<T>> of children. There is
 * no restriction on the number of children that a particular node may have.
 * This Tree provides a method to serialize the Tree into a List by doing a
 * pre-order traversal. It has several methods to allow easy updation of Nodes
 * in the Tree.
 */
public class Tree<T> {

    private TreeNode<T> rootElement;

    /**
     * Default ctor.
     */
    public Tree() {
        super();
    }

    /**
     * Walks the Tree in pre-order style. This is a recursive method, and is
     * called from the toList() method with the root element as the first
     * argument. It appends to the second argument, which is passed by reference
     * as it recurses down the tree.
     *
     * @param element the starting element.
     * @param list    the output of the walk.
     */
    private static <T> void walk(TreeNode<T> element, List<TreeNode<T>> list) {
        list.add(element);
        final Map<String, TreeNode<T>> children = element.getChildren();
        if (children != null) {
            for (TreeNode<T> data : children.values()) {
                walk(data, list);
            }
        }
    }

    public static <T> List<TreeNode<T>> walk(TreeNode<T> tNode) {
        List<TreeNode<T>> list = new ArrayList<>();
        walk(tNode, list);
        return list;
    }

    public static <T> Tree<T> build(TreeNode<T>... elements) {
        Tree<T> ret = new Tree<>();
        ret.setRootElement(TreeNode.branchNode("root", elements));
        return ret;
    }

    /**
     * Return the root Node of the tree.
     *
     * @return the root element.
     */
    public TreeNode<T> getRootElement() {
        return this.rootElement;
    }

    /**
     * Set the root Element for the tree.
     *
     * @param rootElement the root element to set.
     */
    public void setRootElement(TreeNode<T> rootElement) {
        this.rootElement = rootElement;
    }

    /**
     * Returns the Tree<T> as a List of Node<T> objects. The elements of the
     * List are generated from a pre-order traversal of the tree.
     *
     * @return a List<Node<T>>.
     */
    public List<TreeNode<T>> toList() {
        List<TreeNode<T>> list = new ArrayList<>();
        walk(rootElement, list);
        return list;
    }

    public TreeNode<T> lookup(String namespace) {
        return lookup(namespace.split(":"));
    }

    public TreeNode<T> lookup(String... namespace) {
        int currIndex = 0;
        TreeNode<T> currNode = getRootElement();
        if (Objects.equals(namespace[currIndex], "root")) {
            currIndex++;
        }
        while (currIndex < namespace.length) {
            currNode = currNode.getChildren().get(namespace[currIndex++]);
            if (currNode == null) {
                throw new IllegalArgumentException("Tree did not contain namespace");
            }
        }
        return currNode;
    }

    public void merge(String[] namespace, T... leafValues) {
        int currIndex = 0;
        TreeNode<T> currNode = getRootElement();
        if (Objects.equals(namespace[currIndex], "root")) {
            currIndex++;
        }
        while (currIndex < namespace.length) {
            final String id = namespace[currIndex];
            if (currNode.getChildren().containsKey(id)) {
                currNode = currNode.getChildren().get(id);
            } else {
                TreeNode<T> tmp = TreeNode.branchNode(id);
                currNode.addChild(id, tmp);
                currNode = tmp;
            }
            currIndex++;
        }
        for (T data : leafValues) {
            currNode.addChild(TreeNode.leafNode(data));
        }
    }

    public void merge(String namespace, T... leafValues) {
        merge(namespace.split(":"), leafValues);
    }

    /**
     * Returns a String representation of the Tree. The elements are generated
     * from a pre-order traversal of the Tree.
     *
     * @return the String representation of the Tree.
     */
    public String toString() {
        return toList().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tree<?> tree = (Tree<?>) o;

        return getRootElement().equals(tree.getRootElement());
    }

    @Override
    public int hashCode() {
        return getRootElement().hashCode();
    }
}

/**
 * Represents a node of the Tree<T> class. The Node<T> is also a container, and
 * can be thought of as instrumentation to determine the location of the type T
 * in the Tree<T>.
 */
@JsonDeserialize(builder = TreeNode.Builder.class)
class TreeNode<T> {

    public final T data;
    public final Map<String, TreeNode<T>> children;
    public final String id;
    private TreeNode<T> parent;

    /**
     * Convenience ctor to create a Node<T> with an instance of T.
     *
     * @param data an instance of T.
     */
    private TreeNode(String id, T data) {
        if (id == null) {
            this.id = UUID.randomUUID().toString();
        } else {
            this.id = id;
        }
        this.data = data;
        if (data == null) {
            children = new HashMap<>();
        } else {
            children = null;
        }
    }

    public static <T> TreeNode<T> leafNode(String id, T data) {
        return new TreeNode<>(id, data);
    }

    public static <T> TreeNode<T> leafNode(T data) {
        return leafNode(null, data);
    }

    public static <T> TreeNode<T> branchNode(String id, TreeNode<T>... children) {
        return branchNode(id, List.of(children));
    }

    public static <T> TreeNode<T> branchNode(String id, Collection<TreeNode<T>> children) {
        TreeNode<T> ret = new TreeNode<>(id, null);
        for (TreeNode<T> element : children) {
            assert element.id != null && !ret.children.containsKey(element.id);
            ret.addChild(element.id, element);
        }
        return ret;
    }

    /**
     * Return the children of Node<T>. The Tree<T> is represented by a single
     * root Node<T> whose children are represented by a List<Node<T>>. Each of
     * these Node<T> elements in the List can have children. The getChildren()
     * method will return the children of a Node<T>.
     *
     * @return the children of Node<T>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, TreeNode<T>> getChildren() {
        return this.children;
    }

    /**
     * Returns the number of immediate children of this Node<T>.
     *
     * @return the number of immediate children.
     */
    @JsonIgnore
    public int getNumberOfChildren() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    /**
     * Adds a child to the list of children for this Node<T>. The addition of
     * the first child will create a new List<Node<T>>.
     *
     * @param child a Node<T> object to set.
     */
//    public void addChild(Node<T> child) {
//        if (children == null) {
//            children = new ArrayList<>();
//        }
//        children.add(child);
//    }
    public void addChild(String id, TreeNode<T> child) {
        assert children != null;
        children.put(id, child);
        child.parent = this;
    }

    public void addChild(TreeNode<T> child) {
        addChild(child.id, child);
    }

    /**
     * Remove the Node<T> element at index index of the List<Node<T>>.
     *
     * @param index the index of the element to delete.
     */
    public void removeChildAt(String index) {
        if (children != null) {
            children.remove(index);
        }
    }

//    /**
//     * Inserts a Node<T> at the specified position in the child list. Will
//     * throw an ArrayIndexOutOfBoundsException if the index does not exist.
//     * @param index the position to insert at.
//     * @param child the Node<T> object to insert.
//     * @throws IndexOutOfBoundsException if thrown.
//     */
//    public void insertChildAt(int index, Node<T> child) throws IndexOutOfBoundsException {
//        if (index == getNumberOfChildren()) {
//            // this is really an append
//            addChild(child);
//        } else {
//            children.get(index); //just to throw the exception, and stop here
//            children.add(index, child);
//        }
//    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public T getData() {
        return this.data;
    }

    @JsonIgnore
    public String getNamespace() {
        StringBuilder s = new StringBuilder();
        if (parent != null) {
            s.append(parent.getNamespace() + ":");
        }
        return s.append(id).toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(id).append(",[");
        int i = 0;
        if (data != null) {
            sb.append(getData());
        } else {
            for (TreeNode<T> e : getChildren().values()) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(e.id);
                i++;
            }
        }
        sb.append("]").append("}");
        return sb.toString();
    }

    public TreeNode<T> lookup(String[] wordLocation) {
        if (wordLocation.length == 0) {
            return this;
        }
        return this.children.get(wordLocation[0])
                .lookup(Arrays.copyOfRange(wordLocation, 1, wordLocation.length));
    }

    @JsonIgnore
    public List<TreeNode<T>> getLeaves() {
        List<TreeNode<T>> ret = Tree.walk(this);
        ret.removeIf(node -> node.getChildren() != null);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNode<?> node = (TreeNode<?>) o;

        if (getData() != null ? !getData().equals(node.getData()) : node.getData() != null) return false;
        if (getChildren() != null ? !getChildren().equals(node.getChildren()) : node.getChildren() != null)
            return false;
        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        int result = getData() != null ? getData().hashCode() : 0;
        result = 31 * result + (getChildren() != null ? getChildren().hashCode() : 0);
        result = 31 * result + id.hashCode();
        return result;
    }

    @JsonPOJOBuilder
    static class Builder<T> {
        private String id;
        private T data;
        private Map<String, TreeNode<T>> children;

        public Builder<T> withId(String id) {
            this.id = id;
            return this;
        }

        public Builder<T> withData(T data) {
            assert children == null;
            this.data = data;
            return this;
        }

        public Builder<T> withChildren(Map<String, TreeNode<T>> children) {
            assert data == null;
            this.children = children;
            return this;
        }

        public TreeNode<T> build() {
            if (data == null) {
                return branchNode(id, children.values());
            } else {
                return leafNode(id, data);
            }
        }
    }
}