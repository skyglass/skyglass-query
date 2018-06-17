package skyglass.query.metadata.jpa;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import skyglass.data.common.util.reflection.ReflectionMethodsHelper;
import skyglass.query.metadata.Metadata;

public class JpaEntityMetadata extends JpaMetadata {

    @SuppressWarnings("rawtypes")
    public JpaEntityMetadata(Metamodel metamodel, EntityType entityType, CollectionAttribute collectionAttribute) {
        super(metamodel, entityType, collectionAttribute);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public String getIdProperty() {
        String idProperty = null;
        Set<SingularAttribute> singularAttributes = getEntityType().getSingularAttributes();
        for (SingularAttribute singularAttribute : singularAttributes) {
            if (singularAttribute.isId()) {
                idProperty = singularAttribute.getName();
                break;
            }
        }
        return idProperty;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Type getIdentifierType() {
        Set<SingularAttribute> singularAttributes = getEntityType().getSingularAttributes();
        for (SingularAttribute singularAttribute : singularAttributes) {
            if (singularAttribute.isId()) {
                return singularAttribute.getType();
            }
        }
        return null;
    }

    public Metadata getIdType() {
        return new JpaNonEntityMetadata(metamodel, getIdentifierType(), null);
    }

    public Serializable getIdValue(Object object) {
        // TODO: test this method in Hibernate
        /*
         * if (object instanceof HibernateProxy) { return ((HibernateProxy)
         * object).getHibernateLazyInitializer().getIdentifier(); } else {
         * return metadata.getIdentifier(object,
         * (SessionImplementor)entityManagerFactory.createEntityManager()); }
         */
        return (Serializable) ReflectionMethodsHelper.getFieldValue(object, getIdProperty());
    }

    public Object getPropertyValue(Object object, String property) {
        return ReflectionMethodsHelper.getFieldValue(object, property);
    }

    @Override
    public boolean isEmbeddable() {
        return false;
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    private EntityType getEntityType() {
        return (EntityType) type;
    }

}