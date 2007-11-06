package org.objectstyle.wolips.bindings.wod;

import org.eclipse.jface.text.Position;

public class SimpleWodElement extends AbstractWodElement {
  private String _elementName;
  private String _elementType;
  private Position _elementNamePosition;
  private Position _elementTypePosition;

  public SimpleWodElement(IWodElement wodElement) {
    if (wodElement != null) {
      _elementName = wodElement.getElementName();
      _elementNamePosition = wodElement.getElementNamePosition();
      _elementType = wodElement.getElementType();
      _elementTypePosition = wodElement.getElementTypePosition();
      setTemporary(wodElement.isTemporary());
    }
    else {
      setTemporary(true);
    }
  }

  public SimpleWodElement(String name, String type) {
    _elementName = name;
    _elementType = type;
  }

  public void setElementName(String name) {
    String oldElementName = _elementName;
    _elementName = name;
    if (_elementNamePosition != null && _elementName != null) {
      setElementNamePosition(new Position(_elementNamePosition.getOffset(), _elementName.length()));
    }
    if (!isTemporary() && oldElementName != null) {
      int oldLength = oldElementName.length();
      int newLength = name.length();
      int diff = newLength - oldLength;
      setElementTypePosition(new Position(_elementTypePosition.getOffset() + diff, _elementTypePosition.getLength()));
    }
  }

  public String getElementName() {
    return _elementName;
  }

  public Position getElementNamePosition() {
    return _elementNamePosition;
  }

  public void setElementNamePosition(Position elementNamePosition) {
    _elementNamePosition = elementNamePosition;
  }

  public String getElementType() {
    return _elementType;
  }

  public void setElementType(String elementType) {
    _elementType = elementType;
    if (_elementTypePosition != null && _elementType != null) {
      setElementTypePosition(new Position(_elementTypePosition.getOffset(), _elementType.length()));
    }
  }

  public void setElementTypePosition(Position elementTypePosition) {
    _elementTypePosition = elementTypePosition;
  }

  public Position getElementTypePosition() {
    return _elementTypePosition;
  }

  public int getEndOffset() {
    return 0;
  }

  public int getStartOffset() {
    return 0;
  }

  @Override
  public int getLineNumber() {
    return -1;
  }
}