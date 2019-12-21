package skyglass.query.builder.result;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;

import skyglass.query.EnumUtil;
import skyglass.query.builder.QueryResult;
import skyglass.query.builder.string.QueryComposer;
import skyglass.query.builder.string.SelectField;

class QueryResultConverter<T, DTO, DTO2> extends QueryResultBuilder<T> {

	private Supplier<DTO> activeRecordSupplier;

	private Function<T, DTO2> entityDtoConverter;

	private Function<DTO, DTO2> activeRecordDtoConverter;

	QueryResultConverter(QueryComposer queryStringBuilder, QueryResultProvider<T> queryResultProvider, Function<T, DTO2> dtoConverter) {
		this(queryStringBuilder, queryResultProvider, null, dtoConverter, null);
	}

	QueryResultConverter(QueryComposer queryStringBuilder, QueryResultProvider<T> queryResultProvider, Supplier<DTO> activeRecordSupplier) {
		this(queryStringBuilder, queryResultProvider, activeRecordSupplier, null, null);
	}

	QueryResultConverter(QueryComposer queryStringBuilder, QueryResultProvider<T> queryResultProvider, Supplier<DTO> activeRecordSupplier, Function<DTO, DTO2> activeRecordDtoConverter) {
		this(queryStringBuilder, queryResultProvider, activeRecordSupplier, null, activeRecordDtoConverter);
	}

	QueryResultConverter(QueryComposer queryStringBuilder, QueryResultProvider<T> queryResultProvider, Supplier<DTO> activeRecordSupplier, Function<T, DTO2> entityDtoConverter,
			Function<DTO, DTO2> activeRecordDtoConverter) {
		super(queryStringBuilder, queryResultProvider);
		this.activeRecordSupplier = activeRecordSupplier;
		this.entityDtoConverter = entityDtoConverter;
		this.activeRecordDtoConverter = activeRecordDtoConverter;
	}

	public QueryResult<DTO2> convert() {
		return convertDtoList(getQueryStringBuilder(), getResult());
	}

	public List<DTO2> convertList() {
		return convert().getResult();
	}

	public QueryResult<DTO> convertActiveRecord() {
		return convertActiveRecordList(getQueryStringBuilder(), getResult());
	}

	public List<DTO> convertActiveRecordList() {
		return convertActiveRecord().getResult();
	}

	private QueryResult<DTO> convertActiveRecordList(QueryComposer queryStringBuilder, QueryResult<T> queryResult) {
		List<DTO> activeRecordList = null;
		if (CollectionUtils.isNotEmpty(queryStringBuilder.getSelectFields()) && activeRecordSupplier != null) {
			activeRecordList = buildActiveRecordFromSelectFields(queryStringBuilder, queryResult);
		}
		QueryResult<DTO> result = new QueryResult<>();
		result.setResult(activeRecordList);
		result.setTotalCount(queryResult.getTotalCount());
		return result;
	}

	@SuppressWarnings("unchecked")
	private QueryResult<DTO2> convertDtoList(QueryComposer queryStringBuilder, QueryResult<T> queryResult) {
		List<DTO2> dtoList = null;
		if (CollectionUtils.isNotEmpty(queryStringBuilder.getSelectFields()) && activeRecordSupplier != null) {
			List<DTO> activeRecordList = buildActiveRecordFromSelectFields(queryStringBuilder, queryResult);
			if (activeRecordDtoConverter != null) {
				dtoList = buildFromActiveRecordDtoConverter(activeRecordList);
			} else {
				dtoList = (List<DTO2>) activeRecordList;
			}
		} else if (entityDtoConverter != null) {
			dtoList = buildFromEntityDtoConverter(queryResult);
		}
		QueryResult<DTO2> result = new QueryResult<>();
		result.setResult(dtoList);
		result.setTotalCount(queryResult.getTotalCount());
		return result;
	}

	private List<DTO> buildActiveRecordFromSelectFields(QueryComposer queryStringBuilder, QueryResult<T> queryResult) {
		List<DTO> activeRecordList = new ArrayList<>();
		for (T result : queryResult.getResult()) {
			DTO activeRecord = this.activeRecordSupplier.get();
			int i = 0;
			for (SelectField selectField : queryStringBuilder.getSelectFields()) {
				Object propValue = getPropertyValue(result, selectField.getAlias(), i);
				if (propValue != null) {
					try {
						Field typeField = activeRecord.getClass().getDeclaredField(selectField.getAlias());
						@SuppressWarnings("rawtypes")
						Class typeClass = typeField.getType();
						if (typeClass.isEnum()) {
							propValue = EnumUtil.getEnumInstanceObject(propValue, typeClass);
						}
						PropertyUtils.setSimpleProperty(activeRecord, selectField.getAlias(), getPropertyValue(result, selectField.getAlias(), i));
					} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException ex) {
						throw new IllegalArgumentException("Could not set value of the property " + selectField.getAlias() + " to " + propValue, ex);
					}
				}
				i++;
			}
			activeRecordList.add(activeRecord);
		}
		return activeRecordList;
	}

	private List<DTO2> buildFromEntityDtoConverter(QueryResult<T> queryResult) {
		return queryResult.getResult().stream().map(t -> entityDtoConverter.apply(t)).collect(Collectors.toList());
	}

	private List<DTO2> buildFromActiveRecordDtoConverter(List<DTO> activeRecordList) {
		return activeRecordList.stream().map(t -> activeRecordDtoConverter.apply(t)).collect(Collectors.toList());
	}

	private Object getPropertyValue(T source, String alias, int index) {
		Object value = null;
		if (source instanceof Object[]) {
			Object[] array = (Object[]) source;
			return array[index];
		}
		try {
			value = PropertyUtils.getSimpleProperty(source, alias);
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
			return null;
		}
		return value;
	}
}
