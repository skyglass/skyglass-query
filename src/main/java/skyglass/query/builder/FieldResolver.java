package skyglass.query.builder;

import java.util.LinkedHashSet;
import java.util.Set;

import skyglass.query.builder.string.QueryParamProcessor;
import skyglass.query.builder.string.QueryRequestDTO;

public class FieldResolver {

	private Set<String> fieldResolvers = new LinkedHashSet<String>();

	public FieldResolver(QueryRequestDTO queryRequest, String... fieldResolvers) {
		addResolvers(queryRequest, fieldResolvers);
	}

	public boolean isEmpty() {
		return getResolvers().size() == 0;
	}

	public boolean isMultiple() {
		return getResolvers().size() > 1;
	}

	public boolean isSingle() {
		return getResolvers().size() == 1;
	}

	public String getResolver() {
		for (String fieldResolver : getResolvers()) {
			return fieldResolver;
		}
		return null;
	}

	public Set<String> getResolvers() {
		return fieldResolvers;
	}

	public void addResolvers(QueryRequestDTO queryRequest, String... resolvers) {
		for (String resolver : resolvers) {
			fieldResolvers.add(QueryParamProcessor.parseField(resolver, queryRequest));
		}
	}

}
