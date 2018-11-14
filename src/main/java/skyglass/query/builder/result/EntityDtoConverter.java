package skyglass.query.builder.result;

import java.util.function.Function;

import skyglass.query.builder.string.QueryStringBuilder;

class EntityDtoConverter<T, DTO> extends QueryResultConverter<T, DTO, DTO> {

	EntityDtoConverter(QueryStringBuilder queryStringBuilder, QueryResultProvider<T> queryResultProvider, Function<T, DTO> entityDtoConverter) {
		super(queryStringBuilder, queryResultProvider, null, entityDtoConverter, null);
	}

}
