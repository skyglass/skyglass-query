package skyglass.query.api.test;

import java.io.Serializable;

import skyglass.query.metadata.Metadata;
import skyglass.query.metadata.MetadataHelper;

public class TestMetadataHelper implements MetadataHelper {

	@Override
	public Serializable getId(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isId(Class<?> rootClass, String propertyPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Metadata get(Class<?> klass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metadata get(Class<?> rootEntityClass, String propertyPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Class<T> getUnproxiedClass(Class<?> klass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Class<T> getUnproxiedClass(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNumericField(Class<?> rootClass, String propertyName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object convertObject(Class<?> rootClass, String property, Object value, boolean isCollection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCollection(Class<?> rootClass, String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntity(Class<?> rootClass, String path) {
		// TODO Auto-generated method stub
		return false;
	}

}
