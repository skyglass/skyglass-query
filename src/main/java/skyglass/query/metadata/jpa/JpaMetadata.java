package skyglass.query.metadata.jpa;

import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import skyglass.data.common.util.reflection.ReflectionMethodsHelper;
import skyglass.query.metadata.Metadata;

public abstract class JpaMetadata implements Metadata {

    @SuppressWarnings("rawtypes")
    protected Type type;
    @SuppressWarnings("rawtypes")
    protected CollectionAttribute collectionAttribute;
    protected Metamodel metamodel;

    @SuppressWarnings("rawtypes")
    public JpaMetadata(Metamodel metamodel, Type type, CollectionAttribute collectionAttribute) {
        this.metamodel = metamodel;
        this.type = type;
        this.collectionAttribute = collectionAttribute;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public String[] getProperties() {
        if (isComponentType()) {
            ManagedType managedType = (ManagedType) type;
            Set<Attribute> attributes = managedType.getAttributes();
            String[] result = new String[attributes.size()];
            int i = 0;
            for (Attribute attribute : attributes) {
                result[i] = attribute.getName();
                i++;
            }
            return result;
        } else
            return null;
    }

    @Override
    public Object getPropertyValue(Object object, String property) {
        if (!isComponentType())
            return null;
        return ReflectionMethodsHelper.getFieldValue(object, property);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Attribute getAttribute(String name) {
        if (isComponentType()) {
            ManagedType managedType = (ManagedType) type;
            Set<Attribute> attributes = managedType.getAttributes();
            for (Attribute attribute : attributes) {
                if (name.equals(attribute.getName())) {
                    return attribute;
                }
            }
        }
        return null;
    }

    public Class<?> getJavaClass() {
        return type.getJavaType();
    }

    @SuppressWarnings("rawtypes")
    public Metadata getPropertyType(String property) {
        if (!isComponentType()) {
            return null;
        }
        Attribute attribute = getAttribute(property);
        CollectionAttribute pCollectionAttribute = null;
        Type pType = null;
        if (isCollectionAttribute(attribute)) {
            pType = ((CollectionAttribute) attribute).getElementType();
            pCollectionAttribute = (CollectionAttribute) attribute;
        } else if (attribute != null) {
            if (isManagedAttribute(attribute)) {
                pType = getManagedType(attribute);
            } else {
                pType = getBasicType(attribute);
            }
        }
        if (isEntityType(pType)) {
            return new JpaEntityMetadata(metamodel, (EntityType) pType, pCollectionAttribute);
        } else if (pType != null) {
            return new JpaNonEntityMetadata(metamodel, pType, pCollectionAttribute);
        } else {
            return null;
        }
    }

    private boolean isComponentType() {
        return type != null && type instanceof ManagedType;
    }

    @Override
    public boolean isEmbeddable() {
        return type != null && type instanceof EmbeddableType;
    }

    public boolean isString() {
        return isBasicType() && (String.class.equals(type.getJavaType()) || type.getJavaType().isEnum());
    }

    private boolean isBasicType() {
        return type != null && type instanceof BasicType;
    }

    @SuppressWarnings("rawtypes")
    private boolean isCollectionAttribute(Attribute attribute) {
        return attribute != null && attribute instanceof CollectionAttribute;
    }

    @SuppressWarnings("rawtypes")
    private boolean isManagedAttribute(Attribute attribute) {
        return attribute != null && attribute instanceof ManagedType;
    }

    @SuppressWarnings("rawtypes")
    private boolean isEntityType(Type type) {
        return type != null && type instanceof EntityType;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Type getManagedType(Attribute attribute) {
        // Get a managed type (entity, embeddable or mapped super classes):
        return metamodel.managedType(attribute.getJavaType());
    }

    @SuppressWarnings("rawtypes")
    private Type getBasicType(Attribute attribute) {
        // Get a basic type:
        return ((SingularAttribute) attribute).getType();
    }

    @Override
    public boolean isCollection() {
        return collectionAttribute != null;
    }

    @Override
    public Class<?> getCollectionClass() {
        return collectionAttribute.getJavaType();
    }

    @Override
    public boolean isNumeric() {
        return Number.class.isAssignableFrom(getJavaClass());
    }

}
