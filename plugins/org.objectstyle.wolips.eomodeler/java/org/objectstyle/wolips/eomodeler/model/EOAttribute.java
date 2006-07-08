/*
 * ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0
 * 
 * Copyright (c) 2006 The ObjectStyle Group and individual authors of the
 * software. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowlegement: "This product includes software
 * developed by the ObjectStyle Group (http://objectstyle.org/)." Alternately,
 * this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse or
 * promote products derived from this software without prior written permission.
 * For written permission, please contact andrus@objectstyle.org.
 * 
 * 5. Products derived from this software may not be called "ObjectStyle" nor
 * may "ObjectStyle" appear in their names without prior written permission of
 * the ObjectStyle Group.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * OBJECTSTYLE GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the ObjectStyle Group. For more information on the ObjectStyle
 * Group, please see <http://objectstyle.org/>.
 *  
 */
package org.objectstyle.wolips.eomodeler.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EOAttribute extends EOModelObject implements IEOAttribute {
  public static final String PRIMARY_KEY = "primaryKey";
  public static final String CLASS_PROPERTY = "classProperty";
  public static final String USED_FOR_LOCKING = "usedForLocking";
  public static final String ALLOWS_NULL = "allowsNull";
  public static final String PROTOTYPE = "prototype";
  public static final String NAME = "name";
  public static final String COLUMN_NAME = "columnName";
  public static final String ADAPTOR_VALUE_CONVERSION_METHOD_NAME = "adaptorValueConversionMethodName";
  public static final String EXTERNAL_TYPE = "externalType";
  public static final String FACTORY_METHOD_ARGUMENT_TYPE = "factoryMethodArgumentType";
  public static final String PRECISION = "precision";
  public static final String SCALE = "scale";
  public static final String USER_INFO = "userInfo";
  public static final String VALUE_CLASS_NAME = "valueClassName";
  public static final String VALUE_FACTORY_METHOD_NAME = "valueFactoryMethodName";
  public static final String VALUE_TYPE = "valueType";
  public static final String DEFINITION = "definition";
  public static final String WIDTH = "width";
  public static final String READ_FORMAT = "readFormat";
  public static final String WRITE_FORMAT = "writeFormat";
  public static final String CLIENT_CLASS_PROPERTY = "clientClassProperty";

  private EOEntity myEntity;
  private EOAttribute myPrototype;
  private String myName;
  private String myColumnName;
  private String myExternalType;
  private String myValueType;
  private String myValueClassName;
  private String myValueFactoryMethodName;
  private String myFactoryMethodArgumentType;
  private String myAdaptorValueConversionMethodName;
  private Integer myScale;
  private Integer myPrecision;
  private Integer myWidth;
  private Boolean myAllowsNull;
  private Boolean myClassProperty;
  private Boolean myPrimaryKey;
  private Boolean myUsedForLocking;
  private Boolean myClientClassProperty;
  private String myDefinition;
  private String myReadFormat;
  private String myWriteFormat;
  private Map myUserInfo;
  private EOModelMap myAttributeMap;

  public EOAttribute(EOEntity _entity) {
    myEntity = _entity;
    myAttributeMap = new EOModelMap();
  }

  protected void _propertyChanged(String _propertyName, Object _oldValue, Object _newValue) {
    myEntity._attributeChanged(this);
  }

  public int hashCode() {
    return myEntity.hashCode() * myName.hashCode();
  }

  public boolean equals(Object _obj) {
    return (_obj instanceof EOAttribute && ((EOAttribute) _obj).myEntity.equals(myEntity) && ((EOAttribute) _obj).myName.equals(myName));
  }

  public boolean isPrototyped() {
    boolean prototyped = false;
    EOAttribute prototype = getPrototype();
    if (prototype != null) {
      prototyped = true;
    }
    return prototyped;
  }

  public boolean isInherited() {
    boolean inherited = false;
    EOEntity parent = myEntity.getParent();
    if (parent != null) {
      EOAttribute attribute = parent.getAttributeNamed(myName);
      inherited = (attribute != null);
    }
    return inherited;
  }

  public EOAttribute getPrototype() {
    return myPrototype;
  }

  public void setPrototype(EOAttribute _prototype, boolean _updateFromPrototype) {
    EOAttribute oldPrototype = myPrototype;
    boolean prototypeNameChanged = true;
    if (_prototype == null && myPrototype == null) {
      prototypeNameChanged = false;
    }
    else if ((_prototype != null && _prototype.equals(myPrototype)) || (myPrototype != null && myPrototype.equals(_prototype))) {
      prototypeNameChanged = false;
    }
    myPrototype = _prototype;
    if (prototypeNameChanged && _updateFromPrototype) {
      EOAttribute prototype = getPrototype();
      _updateFromPrototype(prototype);
    }
    firePropertyChange(EOAttribute.PROTOTYPE, oldPrototype, _prototype);
  }

  protected void _updateFromPrototype(EOAttribute _prototype) {
    if (_prototype != null) {
      setColumnName((String) _nullIfPrototyped(getColumnName(), _prototype.getColumnName()));
      setExternalType((String) _nullIfPrototyped(getExternalType(), _prototype.getExternalType()));
      setScale((Integer) _nullIfPrototyped(getScale(), _prototype.getScale()));
      setPrecision((Integer) _nullIfPrototyped(getPrecision(), _prototype.getPrecision()));
      setWidth((Integer) _nullIfPrototyped(getWidth(), _prototype.getWidth()));
      setValueType((String) _nullIfPrototyped(getValueType(), _prototype.getValueType()));
      setValueClassName((String) _nullIfPrototyped(getValueClassName(), _prototype.getValueClassName()));
      setValueFactoryMethodName((String) _nullIfPrototyped(getValueFactoryMethodName(), _prototype.getValueFactoryMethodName()));
      setFactoryMethodArgumentType((String) _nullIfPrototyped(getFactoryMethodArgumentType(), _prototype.getFactoryMethodArgumentType()));
      setAdaptorValueConversionMethodName((String) _nullIfPrototyped(getAdaptorValueConversionMethodName(), _prototype.getAdaptorValueConversionMethodName()));
      setAllowsNull((Boolean) _nullIfPrototyped(isAllowsNull(), _prototype.isAllowsNull()));
      setClassProperty((Boolean) _nullIfPrototyped(isClassProperty(), _prototype.isClassProperty()));
      setClientClassProperty((Boolean) _nullIfPrototyped(isClientClassProperty(), _prototype.isClientClassProperty()));
      setPrimaryKey((Boolean) _nullIfPrototyped(isPrimaryKey(), _prototype.isPrimaryKey()));
      setUsedForLocking((Boolean) _nullIfPrototyped(isUsedForLocking(), _prototype.isUsedForLocking()));
      setDefinition((String) _nullIfPrototyped(getDefinition(), _prototype.getDefinition()));
      setReadFormat((String) _nullIfPrototyped(getReadFormat(), _prototype.getReadFormat()));
      setWriteFormat((String) _nullIfPrototyped(getWriteFormat(), _prototype.getWriteFormat()));
    }
  }

  protected Object _nullIfPrototyped(Object _currentValue, Object _prototypeValue) {
    Object value = _currentValue;
    if (_isSet(_prototypeValue)) {
      value = null;
    }
    return value;
  }

  protected boolean _isSet(Object _value) {
    return (_value != null && (!(_value instanceof String) || ((String) _value).trim().length() > 0));
  }

  public void setName(String _name) throws DuplicateAttributeNameException {
    String oldName = myName;
    myEntity._checkForDuplicateAttributeName(this, _name);
    myName = _name;
    firePropertyChange(EOAttribute.NAME, oldName, myName);
  }

  public String getName() {
    return myName;
  }

  public Boolean isAllowsNull() {
    return myAllowsNull;
  }

  public void setAllowsNull(Boolean _allowsNull) {
    setAllowsNull(_allowsNull, true);
  }

  public void setAllowsNull(Boolean _allowsNull, boolean _fireEvents) {
    Boolean oldAllowsNull = myAllowsNull;
    if (myPrimaryKey != null && myPrimaryKey.booleanValue()) {
      myAllowsNull = Boolean.FALSE;
    }
    else {
      myAllowsNull = _allowsNull;
    }
    if (_fireEvents) {
      firePropertyChange(EOAttribute.ALLOWS_NULL, oldAllowsNull, myAllowsNull);
    }
  }

  public Boolean isClassProperty() {
    return myClassProperty;
  }

  public void setClassProperty(Boolean _classProperty) {
    setClassProperty(_classProperty, true);
  }

  public void setClassProperty(Boolean _classProperty, boolean _fireEvents) {
    Boolean oldClassProperty = myClassProperty;
    myClassProperty = _classProperty;
    if (_fireEvents) {
      firePropertyChange(EOAttribute.CLASS_PROPERTY, oldClassProperty, myClassProperty);
    }
  }

  public String getColumnName() {
    return myColumnName;
  }

  public void setColumnName(String _columnName) {
    String oldColumnName = myColumnName;
    myColumnName = _columnName;
    firePropertyChange(EOAttribute.COLUMN_NAME, oldColumnName, myColumnName);
  }

  public EOEntity getEntity() {
    return myEntity;
  }

  public Boolean isPrimaryKey() {
    return myPrimaryKey;
  }

  public void setPrimaryKey(Boolean _primaryKey) {
    setPrimaryKey(_primaryKey, true);
  }

  public void setPrimaryKey(Boolean _primaryKey, boolean _fireEvents) {
    Boolean oldPrimaryKey = myPrimaryKey;
    myPrimaryKey = _primaryKey;
    if (_primaryKey != null && _primaryKey.booleanValue()) {
      setAllowsNull(Boolean.FALSE, _fireEvents);
    }
    if (_fireEvents) {
      firePropertyChange(EOAttribute.PRIMARY_KEY, oldPrimaryKey, myPrimaryKey);
    }
  }

  public Boolean isUsedForLocking() {
    return myUsedForLocking;
  }

  public void setUsedForLocking(Boolean _usedForLocking) {
    setUsedForLocking(_usedForLocking, true);
  }

  public void setUsedForLocking(Boolean _usedForLocking, boolean _fireEvents) {
    Boolean oldUsedForLocking = myUsedForLocking;
    myUsedForLocking = _usedForLocking;
    if (_fireEvents) {
      firePropertyChange(EOAttribute.USED_FOR_LOCKING, oldUsedForLocking, myUsedForLocking);
    }
  }

  public String getAdaptorValueConversionMethodName() {
    return myAdaptorValueConversionMethodName;
  }

  public void setAdaptorValueConversionMethodName(String _adaptorValueConversionMethodName) {
    String oldAdaptorValueConversionMethodName = myAdaptorValueConversionMethodName;
    myAdaptorValueConversionMethodName = _adaptorValueConversionMethodName;
    firePropertyChange(EOAttribute.ADAPTOR_VALUE_CONVERSION_METHOD_NAME, oldAdaptorValueConversionMethodName, myAdaptorValueConversionMethodName);
  }

  public String getExternalType() {
    return myExternalType;
  }

  public void setExternalType(String _externalType) {
    String oldExternalType = myExternalType;
    myExternalType = _externalType;
    firePropertyChange(EOAttribute.EXTERNAL_TYPE, oldExternalType, myExternalType);
  }

  public String getFactoryMethodArgumentType() {
    return myFactoryMethodArgumentType;
  }

  public void setFactoryMethodArgumentType(String _factoryMethodArgumentType) {
    String oldFactoryMethodArgumentType = myFactoryMethodArgumentType;
    myFactoryMethodArgumentType = _factoryMethodArgumentType;
    firePropertyChange(EOAttribute.FACTORY_METHOD_ARGUMENT_TYPE, oldFactoryMethodArgumentType, myFactoryMethodArgumentType);
  }

  public Integer getPrecision() {
    return myPrecision;
  }

  public void setPrecision(Integer _precision) {
    Integer oldPrecision = myPrecision;
    myPrecision = _precision;
    firePropertyChange(EOAttribute.PRECISION, oldPrecision, myPrecision);
  }

  public Integer getScale() {
    return myScale;
  }

  public void setScale(Integer _scale) {
    Integer oldScale = myScale;
    myScale = _scale;
    firePropertyChange(EOAttribute.SCALE, oldScale, myScale);
  }

  public Map getUserInfo() {
    return myUserInfo;
  }

  public void setUserInfo(Map _userInfo) {
    Map oldUserInfo = myUserInfo;
    myUserInfo = _userInfo;
    firePropertyChange(EOAttribute.USER_INFO, oldUserInfo, myUserInfo);
  }

  public String getValueClassName() {
    return myValueClassName;
  }

  public void setValueClassName(String _valueClassName) {
    String oldValueClassName = myValueClassName;
    myValueClassName = _valueClassName;
    firePropertyChange(EOAttribute.VALUE_CLASS_NAME, oldValueClassName, myValueClassName);
  }

  public String getValueFactoryMethodName() {
    return myValueFactoryMethodName;
  }

  public void setValueFactoryMethodName(String _valueFactoryMethodName) {
    String oldValueFactoryMethodName = myValueFactoryMethodName;
    myValueFactoryMethodName = _valueFactoryMethodName;
    firePropertyChange(EOAttribute.VALUE_FACTORY_METHOD_NAME, oldValueFactoryMethodName, myValueFactoryMethodName);
  }

  public String getValueType() {
    return myValueType;
  }

  public void setValueType(String _valueType) {
    String oldValueType = myValueType;
    myValueType = _valueType;
    firePropertyChange(EOAttribute.VALUE_TYPE, oldValueType, myValueType);
  }

  public Integer getWidth() {
    return myWidth;
  }

  public void setWidth(Integer _width) {
    Integer oldWidth = myWidth;
    myWidth = _width;
    firePropertyChange(EOAttribute.WIDTH, oldWidth, myWidth);
  }

  public String getDefinition() {
    return myDefinition;
  }

  public void setDefinition(String _definition) {
    String oldDefinition = myDefinition;
    myDefinition = _definition;
    firePropertyChange(EOAttribute.DEFINITION, oldDefinition, myDefinition);
  }

  public String getReadFormat() {
    return myReadFormat;
  }

  public void setReadFormat(String _readFormat) {
    String oldReadFormat = myReadFormat;
    myReadFormat = _readFormat;
    firePropertyChange(EOAttribute.READ_FORMAT, oldReadFormat, myReadFormat);
  }

  public String getWriteFormat() {
    return myWriteFormat;
  }

  public void setWriteFormat(String _writeFormat) {
    String oldWriteFormat = myWriteFormat;
    myWriteFormat = _writeFormat;
    firePropertyChange(EOAttribute.WRITE_FORMAT, oldWriteFormat, myWriteFormat);
  }

  public void setClientClassProperty(Boolean _clientClassProperty) {
    Boolean oldClientClassProperty = myClientClassProperty;
    myClientClassProperty = _clientClassProperty;
    firePropertyChange(EOAttribute.CLIENT_CLASS_PROPERTY, oldClientClassProperty, myClientClassProperty);
  }

  public Boolean isClientClassProperty() {
    return myClientClassProperty;
  }

  public List getReferencingRelationships() {
    List referencingRelationships = new LinkedList();
    Iterator modelsIter = getEntity().getModel().getModelGroup().getModels().iterator();
    while (modelsIter.hasNext()) {
      EOModel model = (EOModel) modelsIter.next();
      Iterator entitiesIter = model.getEntities().iterator();
      while (entitiesIter.hasNext()) {
        EOEntity entity = (EOEntity) entitiesIter.next();
        Iterator relationshipsIter = entity.getRelationships().iterator();
        while (relationshipsIter.hasNext()) {
          EORelationship relationship = (EORelationship) relationshipsIter.next();
          if (relationship.isRelatedTo(this)) {
            referencingRelationships.add(relationship);
          }
        }
      }
    }
    return referencingRelationships;
  }

  public void loadFromMap(EOModelMap _attributeMap) {
    myAttributeMap = _attributeMap;
    myName = _attributeMap.getString("name", true);
    myColumnName = _attributeMap.getString("columnName", true);
    myExternalType = _attributeMap.getString("externalType", true);
    myScale = _attributeMap.getInteger("scale");
    myPrecision = _attributeMap.getInteger("precision");
    myWidth = _attributeMap.getInteger("width");
    myValueType = _attributeMap.getString("valueType", true);
    myValueClassName = _attributeMap.getString("valueClassName", true);
    myValueFactoryMethodName = _attributeMap.getString("valueFactoryMethodName", true);
    myFactoryMethodArgumentType = _attributeMap.getString("factoryMethodArgumentType", true);
    myAdaptorValueConversionMethodName = _attributeMap.getString("adaptorValueConversionMethodName", true);
    myAllowsNull = _attributeMap.getBoolean("allowsNull");
    myDefinition = _attributeMap.getString("definition", true);
    myReadFormat = _attributeMap.getString("readFormat", true);
    myWriteFormat = _attributeMap.getString("writeFormat", true);
    myUserInfo = _attributeMap.getMap("userInfo", true);
  }

  public EOModelMap toMap() {
    EOModelMap attributeMap = myAttributeMap.cloneModelMap();
    attributeMap.setString("name", myName, true);
    attributeMap.setString("columnName", myColumnName, true);
    if (myPrototype != null) {
      attributeMap.setString("prototypeName", myPrototype.getName(), true);
    }
    attributeMap.setString("externalType", myExternalType, true);
    attributeMap.setInteger("scale", myScale);
    attributeMap.setInteger("precision", myPrecision);
    attributeMap.setInteger("width", myWidth);
    attributeMap.setString("valueType", myValueType, true);
    attributeMap.setString("valueClassName", myValueClassName, true);
    attributeMap.setString("valueFactoryMethodName", myValueFactoryMethodName, true);
    attributeMap.setString("factoryMethodArgumentType", myFactoryMethodArgumentType, true);
    attributeMap.setString("adaptorValueConversionMethodName", myAdaptorValueConversionMethodName, true);
    attributeMap.setBoolean("allowsNull", myAllowsNull);
    attributeMap.setString("definition", myDefinition, true);
    attributeMap.setString("readFormat", myReadFormat, true);
    attributeMap.setString("writeFormat", myWriteFormat, true);
    attributeMap.setMap("userInfo", myUserInfo);
    return attributeMap;
  }

  public void resolve(List _failures) {
    String prototypeName = myAttributeMap.getString("prototypeName", true);
    if (prototypeName != null) {
      myPrototype = myEntity.getModel().getModelGroup().getPrototypeAttributeNamed(prototypeName);
      if (myPrototype == null) {
        _failures.add(new MissingPrototypeFailure(prototypeName, this));
      }
    }
  }

  public void verify(List _failures) {
    // TODO
  }

  public String toString() {
    return "[EOAttribute: " + myName + "]";
  }
}
