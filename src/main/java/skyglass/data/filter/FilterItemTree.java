package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterItemTree extends FilterItem {
	
	public static FilterItemTree NULL_FILTER = new FilterItemTree(JunctionType.AND);
	
	private List<FilterItem> children = new ArrayList<FilterItem>();
	
	private JunctionType junctionType;
	
	public FilterItemTree(JunctionType junctionType) {
		super(null, null, null, null);
		this.junctionType = junctionType;		
	}
	
	public void addChild(FilterItem filterItem) {
		this.children.add(filterItem);
	}

	public List<FilterItem> getChildren() {
		return children;
	}
	
	public JunctionType getJunctionType() {
		return junctionType;
	}
}
