package skyglass.query.metadata.jpa;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import skyglass.query.metadata.Metadata;
import skyglass.query.metadata.MetadataHelper;

/**
 * Implementation of MetadataUtil for JPA
 * 
 * A singleton instance of this class is maintained for each
 * EntityManagerFactory. This should be accessed using
 * {@link JpaMetadataHelper#getInstanceForEntityManagerFactory(EntityManagerFactory)}.
 */
public class JpaMetadataHelper implements MetadataHelper {

	public static JpaMetadataHelper getInstance(EntityManagerFactory entityManagerFactory) {
		JpaMetadataHelper instance = new JpaMetadataHelper();
		instance.metamodel = entityManagerFactory.getMetamodel();
		return instance;
	}

	private JpaMetadataHelper() {

	}

	private Metamodel metamodel;

	// --- Public Methods ---

	@Override
	public Serializable getId(Object entity) {
		if (entity == null)
			throw new NullPointerException("Cannot get ID from null object.");
		return get(entity.getClass()).getIdValue(entity);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean isId(Class<?> rootClass, String propertyPath) {
		if (propertyPath == null || "".equals(propertyPath))
			return false;
		// "id" always refers to the id property, no matter what
		// that property is named. just make sure the segment before this "id"
		// refers to an entity since only entities have ids.
		if (propertyPath.equals("id") || (propertyPath.endsWith(".id")
				&& get(rootClass, propertyPath.substring(0, propertyPath.length() - 3)).isEntity()))
			return true;

		// see if the property is the identifier property of the entity it
		// belongs to.
		int pos = propertyPath.lastIndexOf(".");
		if (pos != -1) {
			Metadata parentType = get(rootClass, propertyPath.substring(0, pos));
			if (!parentType.isEntity())
				return false;
			return propertyPath.substring(pos + 1).equals(parentType.getIdProperty());
		} else {
			return propertyPath.equals(new JpaEntityMetadata(metamodel,
					(EntityType) metamodel.managedType(rootClass), null).getIdProperty());
		}
	}

	@Override
	public Metadata get(Class<?> entityClass) {
		entityClass = getUnproxiedClass(entityClass);
		EntityType<?> entityType = metamodel.entity(entityClass);
		if (entityType == null) {
			throw new IllegalArgumentException(
					"Unable to introspect " + entityClass.toString() + ". The class is not a registered JPA entity.");
		} else {
			return new JpaEntityMetadata(metamodel, entityType, null);
		}
	}

	@Override
	public <T> Class<T> getUnproxiedClass(Object entity) {
		return getUnproxiedClass(entity.getClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Class<T> getUnproxiedClass(Class<?> klass) {
		// cm will be null if entityClass is not registered with Hibernate or
		// when
		// it is a Hibernate proxy class (e.x.
		// test.trg.model.Person_$$_javassist_5).
		// So if a class is not recognized, we will look at superclasses to see
		// if
		// it is a proxy.
		while (metamodel.managedType(klass) == null) {
			klass = klass.getSuperclass();
			if (Object.class.equals(klass))
				return null;
		}

		return (Class<T>) klass;
	}

	@Override
	public boolean isNumericField(Class<?> rootClass, String propertyName) {
		return get(rootClass, propertyName).isNumeric();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object convertObject(Class<?> rootClass, String property, Object value, boolean isCollection) {
		if (value == null)
			return null;

		Class<?> expectedClass;
		if (property != null && ("class".equals(property) || property.endsWith(".class"))) {
			expectedClass = Class.class;
		} else if (property != null && ("size".equals(property) || property.endsWith(".size"))) {
			expectedClass = Integer.class;
		} else {
			expectedClass = get(rootClass, property).getJavaClass();
		}

		// convert numbers to the expected type if needed (ex: Integer to Long)
		if (isCollection) {
			// Check each element in the collection.
			Object[] val2;

			if (value instanceof Collection) {
				val2 = new Object[((Collection) value).size()];
				int i = 0;
				for (Object item : (Collection) value) {
					val2[i++] = convertIfNeeded(item, expectedClass);
				}
			} else {
				val2 = new Object[((Object[]) value).length];
				int i = 0;
				for (Object item : (Object[]) value) {
					val2[i++] = convertIfNeeded(item, expectedClass);
				}
			}
			return val2;
		} else {
			return convertIfNeeded(value, expectedClass);
		}
	}

	public static String paramDisplayString(Object val) {
		if (val == null) {
			return "null";
		} else if (val instanceof String) {
			return "\"" + val + "\"";
		} else if (val instanceof Collection) {
			StringBuilder sb = new StringBuilder();
			sb.append(val.getClass().getSimpleName());
			sb.append(" {");
			boolean first = true;
			for (Object o : (Collection<?>) val) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(paramDisplayString(o));
			}
			sb.append("}");
			return sb.toString();
		} else if (val instanceof Object[]) {
			StringBuilder sb = new StringBuilder();
			sb.append(val.getClass().getComponentType().getSimpleName());
			sb.append("[] {");
			boolean first = true;
			for (Object o : (Object[]) val) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(paramDisplayString(o));
			}
			sb.append("}");
			return sb.toString();
		} else {
			return val.toString();
		}
	}

	@Override
	public Metadata get(Class<?> rootEntityClass, String propertyPath) {
		Metadata md = get(rootEntityClass);
		if (propertyPath == null || "".equals(propertyPath))
			return md;

		String[] chain = propertyPath.split("\\.");

		for (int i = 0; i < chain.length; i++) {
			md = md.getPropertyType(chain[i]);
		}

		return md;
	}

	/**
	 * <p>
	 * Return an instance of the given class type that has the given value. For
	 * example, if type is <code>Long</code> and <code>Integer</code> type with
	 * the value 13 is passed in, a new instance of <code>Long</code> will be
	 * returned with the value 13.
	 * 
	 * <p>
	 * If the value is already of the correct type, it is simply returned.
	 * 
	 * @throws ClassCastException
	 *         if the value cannot be converted to the given type.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object convertIfNeeded(Object value, Class<?> type) throws ClassCastException {
		if (value == null)
			return null;
		if (type.isInstance(value))
			return value;

		if (type.isEnum()) {
			return Enum.valueOf((Class<Enum>) type, value.toString());
		}

		if (String.class.equals(type)) {
			return value.toString();
		} else if (Number.class.isAssignableFrom(type)) {
			// the desired type is a number
			if (value instanceof Number) {
				// the value is also a number of some kind. do a conversion
				// to the correct number type.
				Number num = (Number) value;

				if (type.equals(Double.class)) {
					return new Double(num.doubleValue());
				} else if (type.equals(Float.class)) {
					return new Float(num.floatValue());
				} else if (type.equals(Long.class)) {
					return new Long(num.longValue());
				} else if (type.equals(Integer.class)) {
					return new Integer(num.intValue());
				} else if (type.equals(Short.class)) {
					return new Short(num.shortValue());
				} else {
					try {
						return type.getConstructor(String.class).newInstance(value.toString());
					} catch (IllegalArgumentException e) {
					} catch (SecurityException e) {
					} catch (InstantiationException e) {
					} catch (IllegalAccessException e) {
					} catch (InvocationTargetException e) {
					} catch (NoSuchMethodException e) {
					}
				}
			} else if (value instanceof String) {
				// the value is a String. attempt to parse the string
				try {
					if (type.equals(Double.class)) {
						return Double.parseDouble((String) value);
					} else if (type.equals(Float.class)) {
						return Float.parseFloat((String) value);
					} else if (type.equals(Long.class)) {
						return Long.parseLong((String) value);
					} else if (type.equals(Integer.class)) {
						return Integer.parseInt((String) value);
					} else if (type.equals(Short.class)) {
						return Short.parseShort((String) value);
					} else if (type.equals(Byte.class)) {
						return Byte.parseByte((String) value);
					}
				} catch (NumberFormatException ex) {
					// fall through to the error thrown below
				}
			}
		} else if (Class.class.equals(type)) {
			try {
				return Class.forName(value.toString());
			} catch (ClassNotFoundException e) {
				throw new ClassCastException("Unable to convert value " + value.toString() + " to type Class");
			}
		}

		throw new ClassCastException(
				"Unable to convert value of type " + value.getClass().getName() + " to type " + type.getName());
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
