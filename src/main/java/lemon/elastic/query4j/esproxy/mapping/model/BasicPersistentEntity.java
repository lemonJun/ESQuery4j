/*
 * Copyright 2011-2015 by the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lemon.elastic.query4j.esproxy.mapping.model;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;

import lemon.elastic.query4j.esproxy.annotations.TypeAlias;
import lemon.elastic.query4j.esproxy.mapping.Association;
import lemon.elastic.query4j.esproxy.mapping.AssociationHandler;
import lemon.elastic.query4j.esproxy.mapping.IdentifierAccessor;
import lemon.elastic.query4j.esproxy.mapping.PersistentEntity;
import lemon.elastic.query4j.esproxy.mapping.PersistentProperty;
import lemon.elastic.query4j.esproxy.mapping.PersistentPropertyAccessor;
import lemon.elastic.query4j.esproxy.mapping.PreferredConstructor;
import lemon.elastic.query4j.esproxy.mapping.PropertyHandler;
import lemon.elastic.query4j.esproxy.mapping.SimpleAssociationHandler;
import lemon.elastic.query4j.esproxy.mapping.SimplePropertyHandler;
import lemon.elastic.query4j.util.AnnotationUtils;
import lemon.elastic.query4j.util.SpringStringUtils;
import lemon.elastic.query4j.util.TypeInformation;

/**
 * Simple value object to capture information of {@link PersistentEntity}s.
 * 
 * @author Oliver Gierke
 * @author Jon Brisbin
 * @author Patryk Wasik
 * @author Thomas Darimont
 */
public class BasicPersistentEntity<T, P extends PersistentProperty<P>> implements MutablePersistentEntity<T, P> {

    private final PreferredConstructor<T, P> constructor;
    private final TypeInformation<T> information;
    private final List<P> properties;
    private final Comparator<P> comparator;
    private final Set<Association<P>> associations;

    private final Map<String, P> propertyCache;
    private final Map<Class<? extends Annotation>, Annotation> annotationCache;

    private P idProperty;
    private P versionProperty;

    /**
     * Creates a new {@link BasicPersistentEntity} from the given {@link TypeInformation}.
     * 
     * @param information must not be {@literal null}.
     */
    public BasicPersistentEntity(TypeInformation<T> information) {
        this(information, null);
    }

    /**
     * Creates a new {@link BasicPersistentEntity} for the given {@link TypeInformation} and {@link Comparator}. The given
     * {@link Comparator} will be used to define the order of the {@link PersistentProperty} instances added to the
     * entity.
     * 
     * @param information must not be {@literal null}.
     * @param comparator can be {@literal null}.
     */
    public BasicPersistentEntity(TypeInformation<T> information, Comparator<P> comparator) {

        Preconditions.checkNotNull(information);

        this.information = information;
        this.properties = new ArrayList<P>();
        this.comparator = comparator;
        this.constructor = new PreferredConstructorDiscoverer<T, P>(information, this).getConstructor();
        this.associations = comparator == null ? new HashSet<Association<P>>() : new TreeSet<Association<P>>(new AssociationComparator<P>(comparator));

        this.propertyCache = new HashMap<String, P>();
        this.annotationCache = new HashMap<Class<? extends Annotation>, Annotation>();
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getPersistenceConstructor()
     */
    public PreferredConstructor<T, P> getPersistenceConstructor() {
        return constructor;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#isConstructorArgument(org.springframework.data.mapping.PersistentProperty)
     */
    public boolean isConstructorArgument(PersistentProperty<?> property) {
        return constructor == null ? false : constructor.isConstructorParameter(property);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#isIdProperty(org.springframework.data.mapping.PersistentProperty)
     */
    public boolean isIdProperty(PersistentProperty<?> property) {
        return this.idProperty == null ? false : this.idProperty.equals(property);
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#isVersionProperty(org.springframework.data.mapping.PersistentProperty)
     */
    public boolean isVersionProperty(PersistentProperty<?> property) {
        return this.versionProperty == null ? false : this.versionProperty.equals(property);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getName()
     */
    public String getName() {
        return getType().getName();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getIdProperty()
     */
    public P getIdProperty() {
        return idProperty;
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getVersionProperty()
     */
    public P getVersionProperty() {
        return versionProperty;
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#hasIdProperty()
     */
    public boolean hasIdProperty() {
        return idProperty != null;
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#hasVersionProperty()
     */
    public boolean hasVersionProperty() {
        return versionProperty != null;
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.MutablePersistentEntity#addPersistentProperty(P)
     */
    public void addPersistentProperty(P property) {

        Preconditions.checkNotNull(property);

        if (properties.contains(property)) {
            return;
        }

        properties.add(property);

        if (!propertyCache.containsKey(property.getName())) {
            propertyCache.put(property.getName(), property);
        }

        P candidate = returnPropertyIfBetterIdPropertyCandidateOrNull(property);

        if (candidate != null) {
            this.idProperty = candidate;
        }

        if (property.isVersionProperty()) {

            if (this.versionProperty != null) {
                throw new MappingException(String.format("Attempt to add version property %s but already have property %s registered " + "as version. Check your mapping configuration!", property.getField(), versionProperty.getField()));
            }

            this.versionProperty = property;
        }
    }

    /**
     * Returns the given property if it is a better candidate for the id property than the current id property.
     * 
     * @param property the new id property candidate, will never be {@literal null}.
     * @return the given id property or {@literal null} if the given property is not an id property.
     */
    protected P returnPropertyIfBetterIdPropertyCandidateOrNull(P property) {

        if (!property.isIdProperty()) {
            return null;
        }

        if (this.idProperty != null) {
            throw new MappingException(String.format("Attempt to add id property %s but already have property %s registered " + "as id. Check your mapping configuration!", property.getField(), idProperty.getField()));
        }

        return property;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.mapping.MutablePersistentEntity#addAssociation(org.springframework.data.mapping.model.Association)
     */
    public void addAssociation(Association<P> association) {

        if (!associations.contains(association)) {
            associations.add(association);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getPersistentProperty(java.lang.String)
     */
    public P getPersistentProperty(String name) {
        return propertyCache.get(name);
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getPersistentProperty(java.lang.Class)
     */
    @Override
    public P getPersistentProperty(Class<? extends Annotation> annotationType) {

        Preconditions.checkNotNull(annotationType, "Annotation type must not be null!");

        for (P property : properties) {
            if (property.isAnnotationPresent(annotationType)) {
                return property;
            }
        }

        for (Association<P> association : associations) {

            P property = association.getInverse();

            if (property.isAnnotationPresent(annotationType)) {
                return property;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getType()
     */
    public Class<T> getType() {
        return information.getType();
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getTypeAlias()
     */
    public Object getTypeAlias() {

        TypeAlias alias = getType().getAnnotation(TypeAlias.class);
        return alias == null ? null : SpringStringUtils.hasText(alias.value()) ? alias.value() : null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getTypeInformation()
     */
    public TypeInformation<T> getTypeInformation() {
        return information;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#doWithProperties(org.springframework.data.mapping.PropertyHandler)
     */
    public void doWithProperties(PropertyHandler<P> handler) {

        Preconditions.checkNotNull(handler);

        for (P property : properties) {
            if (!property.isTransient() && !property.isAssociation()) {
                handler.doWithPersistentProperty(property);
            }
        }
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#doWithProperties(org.springframework.data.mapping.PropertyHandler.Simple)
     */
    @Override
    public void doWithProperties(SimplePropertyHandler handler) {

        Preconditions.checkNotNull(handler);

        for (PersistentProperty<?> property : properties) {
            if (!property.isTransient() && !property.isAssociation()) {
                handler.doWithPersistentProperty(property);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#doWithAssociations(org.springframework.data.mapping.AssociationHandler)
     */
    public void doWithAssociations(AssociationHandler<P> handler) {

        Preconditions.checkNotNull(handler);

        for (Association<P> association : associations) {
            handler.doWithAssociation(association);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#doWithAssociations(org.springframework.data.mapping.SimpleAssociationHandler)
     */
    public void doWithAssociations(SimpleAssociationHandler handler) {

        Preconditions.checkNotNull(handler);

        for (Association<? extends PersistentProperty<?>> association : associations) {
            handler.doWithAssociation(association);
        }
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#findAnnotation(java.lang.Class)
     */
    @Override
    public <A extends Annotation> A findAnnotation(Class<A> annotationType) {

        A annotation = annotationType.getAnnotation(annotationType);

        if (annotation != null) {
            return annotation;
        }

        annotation = AnnotationUtils.findAnnotation(getType(), annotationType);
        annotationCache.put(annotationType, annotation);

        return annotation;
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.MutablePersistentEntity#verify()
     */
    public void verify() {

        if (comparator != null) {
            Collections.sort(properties, comparator);
        }
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getPropertyAccessor(java.lang.Object)
     */
    @Override
    public PersistentPropertyAccessor getPropertyAccessor(Object bean) {

        Preconditions.checkNotNull(bean, "Target bean must not be null!");
        Preconditions.checkState(getType().isInstance(bean), "Target bean is not of type of the persistent entity!");

        return new BeanWrapper<Object>(bean);
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.mapping.PersistentEntity#getIdentifierAccessor(java.lang.Object)
     */
    @Override
    public IdentifierAccessor getIdentifierAccessor(Object bean) {

        Preconditions.checkNotNull(bean, "Target bean must not be null!");
        Preconditions.checkState(getType().isInstance(bean), "Target bean is not of type of the persistent entity!");

        return hasIdProperty() ? new IdPropertyIdentifierAccessor(this, bean) : NullReturningIdentifierAccessor.INSTANCE;
    }

    /**
     * A null-object implementation of {@link IdentifierAccessor} to be able to return an accessor for entities that do
     * not have an identifier property.
     *
     * @author Oliver Gierke
     */
    private static enum NullReturningIdentifierAccessor implements IdentifierAccessor {

        INSTANCE;

        /* 
         * (non-Javadoc)
         * @see org.springframework.data.mapping.IdentifierAccessor#getIdentifier()
         */
        @Override
        public Object getIdentifier() {
            return null;
        }
    }

    /**
     * Simple {@link Comparator} adaptor to delegate ordering to the inverse properties of the association.
     * 
     * @author Oliver Gierke
     */
    private static final class AssociationComparator<P extends PersistentProperty<P>> implements Comparator<Association<P>>, Serializable {

        private static final long serialVersionUID = 4508054194886854513L;
        private final Comparator<P> delegate;

        public AssociationComparator(Comparator<P> delegate) {
            Preconditions.checkNotNull(delegate);
            this.delegate = delegate;
        }

        public int compare(Association<P> left, Association<P> right) {
            return delegate.compare(left.getInverse(), right.getInverse());
        }
    }
}
