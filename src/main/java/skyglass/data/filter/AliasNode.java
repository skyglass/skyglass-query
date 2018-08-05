package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

public class AliasNode {
    private String property;
    private String alias;
    private boolean fetch;
    private AliasNode parent;
    private List<AliasNode> children = new ArrayList<AliasNode>();

    AliasNode(String property, String alias) {
        this.property = property;
        this.alias = alias;
    }

    void addChild(AliasNode node) {
        children.add(node);
        node.parent = this;
    }

    public String getFullPath() {
        if (parent == null)
            return "";
        else if (parent.parent == null)
            return property;
        else
            return parent.getFullPath() + "." + property;
    }

	public String getProperty() {
		return property;
	}

	public String getAlias() {
		return alias;
	}

	public boolean isFetch() {
		return fetch;
	}
	
	public void setFetch(boolean fetch) {
		this.fetch = fetch;
	}

	public AliasNode getParent() {
		return parent;
	}

	public List<AliasNode> getChildren() {
		return children;
	}
}
