package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;


public class PrivateFilterItemTree extends PrivateFilterItem {
	
	private List<PrivateFilterItem> children = new ArrayList<PrivateFilterItem>();
	
	private JunctionType junctionType;
	
	public PrivateFilterItemTree(JunctionType junctionType) {
		super(null, null, null, null);
		this.junctionType = junctionType;		
	}
	
	public void addChild(PrivateFilterItem filterItem) {
		this.children.add(filterItem);
	}

	public List<PrivateFilterItem> getChildren() {
		return children;
	}
	
	public JunctionType getJunctionType() {
		return junctionType;
	}
}
