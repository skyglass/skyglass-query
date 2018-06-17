package skyglass.query.metadata.jpa;

import java.io.Serializable;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type;

import skyglass.query.metadata.Metadata;

/**
 * Implementation of Metadata for a non-entity type in JPA.
 * 
 */
public class JpaNonEntityMetadata extends JpaMetadata {

    @SuppressWarnings("rawtypes")
    public JpaNonEntityMetadata(Metamodel metamodel, Type<?> type, CollectionAttribute collectionAttribute) {
        super(metamodel, type, collectionAttribute);
    }

    @Override
    public String getIdProperty() {
        return null;
    }

    @Override
    public Metadata getIdType() {
        return null;
    }

    @Override
    public Serializable getIdValue(Object object) {
        return null;
    }

    @Override
    public boolean isEntity() {
        return false;
    }

}