/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.controller;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ResourceBundle;

import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.validation.ParameterValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Defining characteristics of an attribute in a {@link org.jboss.as.controller.registry.Resource}, with utility
 * methods for conversion to and from xml and for validation.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public abstract class AttributeDefinition {

    private final String name;
    private final String xmlName;
    private final ModelType type;
    private final boolean allowNull;
    private final boolean allowExpression;
    private final ModelNode defaultValue;
    private final MeasurementUnit measurementUnit;
    private final ParameterValidator validator;

    protected AttributeDefinition(String name, String xmlName, final ModelNode defaultValue, final ModelType type,
                               final boolean allowNull, final boolean allowExpression, final MeasurementUnit measurementUnit,
                               final ParameterValidator validator) {
        this.name = name;
        this.xmlName = xmlName;
        this.type = type;
        this.allowNull = allowNull;
        this.allowExpression = allowExpression;
        this.defaultValue = new ModelNode();
        if (defaultValue != null) {
            this.defaultValue.set(defaultValue);
        }
        this.defaultValue.protect();
        this.measurementUnit = measurementUnit;
        this.validator = validator;
    }

    public String getName() {
        return name;
    }

    public String getXmlName() {
        return xmlName;
    }

    public ModelType getType() {
        return type;
    }

    public boolean isAllowNull() {
        return allowNull;
    }

    public boolean isAllowExpression() {
        return allowExpression;
    }

    public ModelNode getDefaultValue() {
        return defaultValue.isDefined() ? defaultValue : null;
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public ParameterValidator getValidator() {
        return validator;
    }

    /**
     * Gets whether the given {@code resourceModel} has a value for this attribute that should be marshalled to XML.
     *
     * @param resourceModel the model, a non-null node of {@link ModelType#OBJECT}.
     * @param marshallDefault {@code true} if the value should be marshalled even if it matches the default value
     *
     * @return {@true} if the given {@code resourceModel} has a defined value under this attribute's {@link #getName()} () name}
     * and {@code marshallDefault} is {@code true} or that value differs from this attribute's {@link #getDefaultValue() default value}.
     */
    public boolean isMarshallable(final ModelNode resourceModel, final boolean marshallDefault) {
         if (resourceModel.hasDefined(name)) {
            ModelNode node = resourceModel.get(name);
            if (marshallDefault || !node.equals(defaultValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a value in the given {@code operationObject} whose key matches this attribute's {@link #getName() name} and
     * validates it using this attribute's {@link #getValidator() validator}.
     *
     * @param operationObject model node of type {@link ModelType#OBJECT}, typically representing an operation request
     *
     * @return the value
     * @throws OperationFailedException if the value is not valid
     */
    public ModelNode validateOperation(final ModelNode operationObject) throws OperationFailedException {

        ModelNode node = new ModelNode();
        if (operationObject.has(name)) {
            node.set(operationObject.get(name)) ;
        }
        if (!node.isDefined() && defaultValue.isDefined()) {
            node.set(defaultValue);
        }
        validator.validateParameter(name, node);

        return node;
    }

    /**
     * Finds a value in the given {@code operationObject} whose key matches this attribute's {@link #getName() name},
     * validates it using this attribute's {@link #getValidator() validator}, and, if
     * {@link org.jboss.dmr.ModelNode#isDefined() defined} stores under this attribute's name in the given {@code model}.
     *
     * @param operationObject model node of type {@link ModelType#OBJECT}, typically representing an operation request
     * @parm model model node in which the value should be stored
     *
     * @throws OperationFailedException if the value is not valid
     */
    public void validateAndSet(final ModelNode operationObject, final ModelNode model) throws OperationFailedException {

        ModelNode node = validateOperation(operationObject);
        if (node.isDefined()) {
            model.get(name).set(node);
        }
    }

    /**
     * Finds a value in the given {@code operationObject} whose key matches this attribute's {@link #getName() name},
     * resolves it and validates it using this attribute's {@link #getValidator() validator}.
     *
     * @param operationObject model node of type {@link ModelType#OBJECT}, typically representing an operation request
     *
     * @return the resolved value
     * @throws OperationFailedException if the value is not valid
     */
    public ModelNode validateResolvedOperation(final ModelNode operationObject) throws OperationFailedException {
        ModelNode node = new ModelNode();
        if (operationObject.has(name)) {
            node.set(operationObject.get(name)) ;
        }
        if (!node.isDefined() && defaultValue.isDefined()) {
            node.set(defaultValue);
        }
        validator.validateParameter(name, node);

        return node.resolve();
    }

    /**
     * Marshalls the value from the given {@code resourceModel} as an xml element, if it
     * {@link #isMarshallable(org.jboss.dmr.ModelNode, boolean) is marshallable}.
     *
     * @param resourceModel the model, a non-null node of {@link org.jboss.dmr.ModelType#OBJECT}.
     * @param writer stream writer to use for writing the attribute
     * @throws javax.xml.stream.XMLStreamException
     */
    public abstract void marshallAsElement(final ModelNode resourceModel, final XMLStreamWriter writer) throws XMLStreamException;

    /**
     * Creates a returns a basic model node describing the attribute, after attaching it to the given overall resource
     * description model node.  The node describing the attribute is returned to make it easy to perform further
     * modification.
     *
     * @param bundle resource bundle to use for text descriptions
     * @param prefix prefix to prepend to the attribute name key when looking up descriptions
     * @param resourceDescription  the overall resource description
     * @return  the attribute description node
     */
    public ModelNode addResourceAttributeDescription(final ResourceBundle bundle, final String prefix, final ModelNode resourceDescription) {
        final ModelNode attr = new ModelNode();
        attr.get(ModelDescriptionConstants.TYPE).set(attr.getType());
        attr.get(ModelDescriptionConstants.DESCRIPTION).set(getAttributeTextDescription(bundle, prefix));
        // TODO enable when this metadata is finalized
//        attr.get(ModelDescriptionConstants.EXPRESSIONS_ALLOWED).set(isAllowExpression());
        attr.get(ModelDescriptionConstants.NILLABLE).set(isAllowNull());
        if (defaultValue != null && defaultValue.isDefined()) {
            attr.get(ModelDescriptionConstants.DEFAULT).set(defaultValue);
        }
        if (measurementUnit != MeasurementUnit.NONE) {
            attr.get(ModelDescriptionConstants.UNIT).set(measurementUnit.getName());
        }
        resourceDescription.get(ModelDescriptionConstants.ATTRIBUTES, getName()).set(attr);
        return attr;
    }

    /**
     * Creates a returns a basic model node describing a parameter that sets this attribute, after attaching it to the
     * given overall operation description model node.  The node describing the parameter is returned to make it easy
     * to perform further modification.
     *
     * @param bundle resource bundle to use for text descriptions
     * @param prefix prefix to prepend to the attribute name key when looking up descriptions
     * @param operationDescription  the overall resource description
     * @return  the attribute description node
     */
    public ModelNode addOperationParameterDescription(final ResourceBundle bundle, final String prefix, final ModelNode operationDescription) {
        final ModelNode param = new ModelNode();
        param.get(ModelDescriptionConstants.TYPE).set(type);
        param.get(ModelDescriptionConstants.DESCRIPTION).set(getAttributeTextDescription(bundle, prefix));
        // TODO enable when this metadata is finalized
//        param.get(ModelDescriptionConstants.EXPRESSIONS_ALLOWED).set(isAllowExpression());
        param.get(ModelDescriptionConstants.REQUIRED).set(!isAllowNull());
        param.get(ModelDescriptionConstants.NILLABLE).set(isAllowNull());
        if (measurementUnit != MeasurementUnit.NONE) {
            param.get(ModelDescriptionConstants.UNIT).set(measurementUnit.getName());
        }
        operationDescription.get(ModelDescriptionConstants.REQUEST_PROPERTIES, getName()).set(param);
        return param;
    }

    public String getAttributeTextDescription(final ResourceBundle bundle, final String prefix) {
        final String bundleKey = prefix == null ? name : (prefix + "." + name);
        return bundle.getString(bundleKey);
    }



}
