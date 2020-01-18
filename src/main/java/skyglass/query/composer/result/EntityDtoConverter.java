package skyglass.query.composer.result;

import java.util.function.Function;

import skyglass.query.composer.QueryComposer;

public class EntityDtoConverter<T, DTO> extends QueryResultConverter<T, DTO, DTO> {

	public EntityDtoConverter(QueryComposer queryComposer, QueryResultProvider<T> queryResultProvider, Function<T, DTO> entityDtoConverter) {
		super(queryComposer, queryResultProvider, null, entityDtoConverter, null);
	}

}
