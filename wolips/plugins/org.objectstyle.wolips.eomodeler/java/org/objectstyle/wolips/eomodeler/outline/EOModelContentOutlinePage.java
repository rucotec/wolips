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
package org.objectstyle.wolips.eomodeler.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.objectstyle.wolips.eomodeler.editors.EOModelEditorInput;
import org.objectstyle.wolips.eomodeler.model.EOEntity;
import org.objectstyle.wolips.eomodeler.model.EOModel;
import org.objectstyle.wolips.eomodeler.model.EORelationshipPath;

public class EOModelContentOutlinePage extends ContentOutlinePage {
  private EOModelEditorInput myEditorInput;
  private ModelPropertyChangeListener myModelListener;
  private EntityPropertyChangeListener myEntityListener;
  private List myEntities;

  public EOModelContentOutlinePage(EOModelEditorInput _editorInput) {
    myEditorInput = _editorInput;
    myEntities = new LinkedList(myEditorInput.getModel().getEntities());
    myModelListener = new ModelPropertyChangeListener();
    myEntityListener = new EntityPropertyChangeListener();
    updatePropertyChangeListeners();
  }

  public void createControl(Composite _parent) {
    super.createControl(_parent);
    TreeViewer treeViewer = getTreeViewer();
    treeViewer.setContentProvider(new EOModelOutlineContentProvider());
    treeViewer.setLabelProvider(new EOModelOutlineLabelProvider());
    treeViewer.setInput(myEditorInput);
    treeViewer.expandToLevel(2);
    setFocus();
  }

  public void init(IPageSite _pageSite) {
    super.init(_pageSite);
  }
  
  public void selectionChanged(SelectionChangedEvent _event) {
    super.selectionChanged(_event);
  }
  
  public void setSelection(ISelection _selection) {
    super.setSelection(_selection);
  }

  protected TreeViewer getTreeViewer() {
    return super.getTreeViewer();
  }

  protected void updatePropertyChangeListeners() {
    myEditorInput.getModel().removePropertyChangeListener(myModelListener);
    if (myEntities != null) {
      Iterator oldEntitiesIter = myEntities.iterator();
      while (oldEntitiesIter.hasNext()) {
        EOEntity entity = (EOEntity) oldEntitiesIter.next();
        entity.removePropertyChangeListener(myEntityListener);
      }
    }
    myEntities = new LinkedList(myEditorInput.getModel().getEntities());
    Iterator newEntitiesIter = myEntities.iterator();
    while (newEntitiesIter.hasNext()) {
      EOEntity entity = (EOEntity) newEntitiesIter.next();
      entity.addPropertyChangeListener(myEntityListener);
    }
    myEditorInput.getModel().addPropertyChangeListener(myModelListener);
  }

  protected void refreshRelationshipsForEntity(EOEntity _entity) {
    getTreeViewer().refresh(_entity, true);
    Object[] expandedElements = getTreeViewer().getExpandedElements();
    for (int expandedElementNum = 0; expandedElementNum < expandedElements.length; expandedElementNum++) {
      if (expandedElements[expandedElementNum] instanceof EORelationshipPath) {
        EORelationshipPath relationshipPath = (EORelationshipPath) expandedElements[expandedElementNum];
        if (relationshipPath.getChildRelationship().getEntity().equals(_entity)) {
          getTreeViewer().refresh(relationshipPath, true);
        }
      }
    }
  }

  protected class ModelPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent _event) {
      String changedPropertyName = _event.getPropertyName();
      if (EOModel.ENTITIES.equals(changedPropertyName)) {
        getTreeViewer().refresh(true);
      }
    }
  }

  protected class EntityPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent _event) {
      EOEntity entity = (EOEntity) _event.getSource();
      String changedPropertyName = _event.getPropertyName();
      if (EOEntity.NAME.equals(changedPropertyName)) {
        getTreeViewer().refresh(entity, true);
      }
      else if (EOEntity.FETCH_SPECIFICATIONS.equals(changedPropertyName)) {
        getTreeViewer().refresh(entity, true);
      }
      else if (EOEntity.RELATIONSHIPS.equals(changedPropertyName)) {
        EOModelContentOutlinePage.this.refreshRelationshipsForEntity(entity);
      }
      else if (EOEntity.RELATIONSHIP.equals(changedPropertyName)) {
        EOModelContentOutlinePage.this.refreshRelationshipsForEntity(entity);
      }
    }
  }
}
