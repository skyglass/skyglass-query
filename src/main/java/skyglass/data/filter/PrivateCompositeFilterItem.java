package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

import skyglass.query.model.query.InternalUtil;


public class PrivateCompositeFilterItem extends PrivateFilterItem {
	
	private List<PrivateFilterItem> children = new ArrayList<PrivateFilterItem>();
	
	public PrivateCompositeFilterItem(FilterType filterType) {
		this(null, filterType);	
	}
	
	public PrivateCompositeFilterItem(FieldResolver fieldResolver, FilterType filterType) {
		super(null, fieldResolver, null, filterType);	
	}
	
	public void addChild(PrivateFilterItem filterItem) {
		this.children.add(filterItem);
	}

	public List<PrivateFilterItem> getChildren() {
		return children;
	}
	
	public PrivateFilterItem getSingleChild() {
		if (children.size() != 1) {
			throw new UnsupportedOperationException("This filter must have one and only one child");
		}
		return children.get(0);
	}
	
    @Override
    public String toString() {
        switch (filterType) {
        case And:
        case Or:
            String op = filterType == FilterType.And ? " and " : " or ";

            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            for (PrivateFilterItem filterItem: children) {
                if (first) {
                    first = false;
                } else {
                    sb.append(op);
                }
                sb.append(filterItem.toString());
            }
            if (first)
                return (filterType == FilterType.And ? "AND: " : "OR: ") + "**EMPTY LIST**";

            sb.append(")");
            return sb.toString();
        case Not:
            return "not " + getSingleChild().toString();
        case Some:
            return "some `" + fieldResolver.getFieldName() + "` {" + getSingleChild().toString() + "}";
        case All:
            return "all `" + fieldResolver.getFieldName() + "` {" + getSingleChild().toString() + "}";
        case None:
            return "none `" + fieldResolver.getFieldName() + "` {" + getSingleChild().toString() + "}";
        default:
            return "**INVALID OPERATOR: (" + filterType + ") - VALUE: " + InternalUtil.paramDisplayString(filterValue) + " **";
        }
    }
}
