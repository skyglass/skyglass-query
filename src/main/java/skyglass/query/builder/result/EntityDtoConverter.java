package skyglass.query.builder.result;

import java.util.function.Function;

import skyglass.query.builder.string.QueryComposer;

class EntityDtoConverter<T, DTO> extends QueryResultConverter<T, DTO, DTO> {

	EntityDtoConverter(QueryComposer queryStringBuilder, QueryResultProvider<T> queryResultProvider, Function<T, DTO> entityDtoConverter) {
		super(queryStringBuilder, queryResultProvider, null, entityDtoConverter, null);
	}

}
