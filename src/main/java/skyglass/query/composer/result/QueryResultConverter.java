package skyglass.query.composer.result;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;

import skyglass.query.composer.QueryComposer;
import skyglass.query.composer.QueryResult;
import skyglass.query.composer.SelectField;
import skyglass.query.composer.config.Constants;
import skyglass.query.composer.util.EnumUtil;

class QueryResultConverter<T, DTO, DTO2> extends QueryResultBuilder<T> {

	private Supplier<DTO> activeRecordSupplier;

	private Function<T, DTO2> entityDtoConverter;

	private Function<DTO, DTO2> activeRecordDtoConverter;

	QueryResultConverter(QueryComposer queryComposer, QueryResultProvider<T> queryResultProvider, Function<T, DTO2> dtoConverter) {
		this(queryComposer, queryResultProvider, null, dtoConverter, null);
	}

	QueryResultConverter(QueryComposer queryComposer, QueryResultProvider<T> queryResultProvider, Supplier<DTO> activeRecordSupplier) {
		this(queryComposer, queryResultProvider, activeRecordSupplier, null, null);
	}

	QueryResultConverter(QueryComposer queryComposer, QueryResultProvider<T> queryResultProvider, Supplier<DTO> activeRecordSupplier, Function<DTO, DTO2> activeRecordDtoConverter) {
		this(queryComposer, queryResultProvider, activeRecordSupplier, null, activeRecordDtoConverter);
	}

	QueryResultConverter(QueryComposer queryComposer, QueryResultProvider<T> queryResultProvider, Supplier<DTO> activeRecordSupplier, Function<T, DTO2> entityDtoConverter,
			Function<DTO, DTO2> activeRecordDtoConverter) {
		super(queryComposer, queryResultProvider);
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

	private QueryResult<DTO> convertActiveRecordList(QueryComposer queryComposer, QueryResult<T> queryResult) {
		List<DTO> activeRecordList = null;
		if (CollectionUtils.isNotEmpty(queryComposer.getSelectFields()) && activeRecordSupplier != null) {
			activeRecordList = buildActiveRecordFromSelectFields(queryComposer, queryResult);
		}
		QueryResult<DTO> result = new QueryResult<>();
		result.setResult(activeRecordList);
		result.setTotalCount(queryResult.getTotalCount());
		return result;
	}

	@SuppressWarnings("unchecked")
	private QueryResult<DTO2> convertDtoList(QueryComposer queryComposer, QueryResult<T> queryResult) {
		List<DTO2> dtoList = null;
		if (CollectionUtils.isNotEmpty(queryComposer.getSelectFields()) && activeRecordSupplier != null) {
			List<DTO> activeRecordList = buildActiveRecordFromSelectFields(queryComposer, queryResult);
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

	private List<DTO> buildActiveRecordFromSelectFields(QueryComposer queryComposer, QueryResult<T> queryResult) {
		List<DTO> activeRecordList = new ArrayList<>();
		for (T result : queryResult.getResult()) {
			DTO activeRecord = this.activeRecordSupplier.get();
			int i = 0;
			for (SelectField selectField : queryComposer.getSelectFields()) {
				Object propValue = getPropertyValue(result, getAlias(selectField), i);
				if (propValue != null) {
					try {
						Field typeField = activeRecord.getClass().getDeclaredField(getAlias(selectField));
						@SuppressWarnings("rawtypes")
						Class typeClass = typeField.getType();
						if (typeClass.isEnum()) {
							propValue = EnumUtil.getEnumInstanceObject(propValue, typeClass);
						}
						PropertyUtils.setSimpleProperty(activeRecord, getAlias(selectField), getPropertyValue(result, getAlias(selectField), i));
					} catch (IllegalAccessException | NoSuchMethodException 
							| InvocationTargetException | NoSuchFieldException ex) {
						throw new IllegalArgumentException("Could not set value of the property " + getAlias(selectField) + " to " + propValue, ex);
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
	
	private String getAlias(SelectField selectField) {
		String result = selectField.getAlias();
		if (Constants.UUID.equals(result)) {
			return Constants.UUID_ALIAS;
		}
		return result;
	}
}
