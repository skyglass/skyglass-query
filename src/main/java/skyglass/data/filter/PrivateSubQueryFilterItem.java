package skyglass.data.filter;

public class PrivateSubQueryFilterItem extends PrivateCompositeFilterItem {
	
	private PrivateQueryContext queryContext;
	
	public PrivateSubQueryFilterItem(PrivateQueryContext queryContext, FilterType filterType) {
		this(queryContext, null, filterType);	
	}
	
	public PrivateSubQueryFilterItem(PrivateQueryContext queryContext,
			FieldResolver fieldResolver, FilterType filterType) {
		super(fieldResolver, filterType);	
		this.queryContext = queryContext;
	}
}
