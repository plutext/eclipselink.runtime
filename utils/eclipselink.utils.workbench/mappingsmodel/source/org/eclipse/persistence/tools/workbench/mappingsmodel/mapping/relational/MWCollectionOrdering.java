/*******************************************************************************
 * Copyright (c) 1998, 2015 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
******************************************************************************/
package org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.relational;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.persistence.tools.workbench.mappingsmodel.MWModel;
import org.eclipse.persistence.tools.workbench.mappingsmodel.MWQueryKey;
import org.eclipse.persistence.tools.workbench.mappingsmodel.ProblemConstants;
import org.eclipse.persistence.tools.workbench.mappingsmodel.handles.MWHandle;
import org.eclipse.persistence.tools.workbench.mappingsmodel.handles.MWQueryKeyHandle;
import org.eclipse.persistence.tools.workbench.mappingsmodel.handles.MWHandle.NodeReferenceScrubber;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.MWMapping;
import org.eclipse.persistence.tools.workbench.utility.node.Node;

import org.eclipse.persistence.mappings.CollectionMapping;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;

public final class MWCollectionOrdering extends MWModel {

    private volatile boolean ascending;
        public static final String ASCENDING_PROPERTY = "ascending";

    private MWQueryKeyHandle queryKeyHandle;
        public static final String QUERY_KEY_PROPERTY = "queryKey";


    // ********** static methods **********


    // **************** Constructors ***************

    /** Default constructor - for TopLink use only */
    private MWCollectionOrdering() {
        super();
    }

    MWCollectionOrdering(MWCollectionMapping owner) {
        super(owner);
    }

    public MWCollectionMapping getOwner() {
        return (MWCollectionMapping) this.getParent();
    }

    protected void initialize(Node parent) {
        super.initialize(parent);
        this.queryKeyHandle = new MWQueryKeyHandle(this, this.buildQueryKeyScrubber());
        this.ascending = true;
    }


    // **************** Accessors ***************

    public boolean isAscending() {
        return this.ascending;
    }

    public void setAscending(boolean newAscending) {
        boolean oldValue = this.ascending;
        this.ascending = newAscending;
        firePropertyChanged(ASCENDING_PROPERTY, oldValue, newAscending);
    }

    public MWQueryKey getQueryKey() {
        return this.queryKeyHandle.getQueryKey();
    }

    public void setQueryKey(MWQueryKey queryKey) {
        Object oldValue = this.getQueryKey();
        this.queryKeyHandle.setQueryKey(queryKey);
        firePropertyChanged(QUERY_KEY_PROPERTY, oldValue, queryKey);
    }


    //**************** Model Synchronization ************

    protected void addChildrenTo(List list) {
        super.addChildrenTo(list);
        list.add(this.queryKeyHandle);
    }

    private NodeReferenceScrubber buildQueryKeyScrubber() {
        return new NodeReferenceScrubber() {
            public void nodeReferenceRemoved(Node node, MWHandle handle) {
                MWCollectionOrdering.this.setQueryKey(null);
            }
            public String toString() {
                return "MWCollectionMapping.buildQueryKeyScrubber()";
            }
        };
    }

    /**
     * Need to check if the query key we're holding on to has been removed by
     * umapping action, this could be the case if its an auto-generated one.
     */
    public void descriptorUnmapped(Collection mappings) {
        super.descriptorUnmapped(mappings);
        for (Iterator stream = mappings.iterator(); stream.hasNext(); ) {
            MWMapping mapping = (MWMapping)stream.next();
            MWQueryKey queryKey = mapping.getAutoGeneratedQueryKey();
            if (queryKey != null && getQueryKey() == queryKey) {
                setQueryKey(null);
                return;
            }
        }
    }


    //**************** Problem Handling ************

    protected void addProblemsTo(List currentProblems) {
        super.addProblemsTo(currentProblems);
        this.checkQueryKey(currentProblems);
    }

    private void checkQueryKey(List currentProblems) {
        if (this.getQueryKey() == null) {
            currentProblems.add(this.buildProblem(ProblemConstants.MAPPING_ORDERING_QUERY_KEY_NOT_SPECIFIED));
        }
    }


    //**************** Runtime Conversion ************

    public void adjustRuntimeMapping(CollectionMapping runtimeMapping) {
        if (getQueryKey() != null) {
            if (isAscending()) {
                runtimeMapping.addAscendingOrdering(getQueryKey().getName());
            }
            else {
                runtimeMapping.addDescendingOrdering(getQueryKey().getName());
            }
        }
    }


    //**************** Display Methods ************

    public void toString(StringBuffer sb) {
        if (getQueryKey() != null) {
            sb.append(this.getQueryKey().getName());
        }
    }


    //**************** TopLink methods ************

    public static XMLDescriptor buildDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();

        descriptor.setJavaClass(MWCollectionOrdering.class);

        XMLDirectMapping ascendingMapping = (XMLDirectMapping) descriptor.addDirectMapping("ascending", "ascending/text()");
        ascendingMapping.setNullValue(Boolean.TRUE);

        XMLCompositeObjectMapping queryKeyHandleMapping = new XMLCompositeObjectMapping();
        queryKeyHandleMapping.setAttributeName("queryKeyHandle");
        queryKeyHandleMapping.setGetMethodName("getQueryKeyHandleForTopLink");
        queryKeyHandleMapping.setSetMethodName("setQueryKeyHandleForTopLink");
        queryKeyHandleMapping.setReferenceClass(MWQueryKeyHandle.class);
        queryKeyHandleMapping.setXPath("query-key-handle");
        descriptor.addMapping(queryKeyHandleMapping);

        return descriptor;
    }

    /** check for null */
    private MWQueryKeyHandle getQueryKeyHandleForTopLink() {
        return (this.queryKeyHandle.getQueryKey() == null) ? null : this.queryKeyHandle;
    }
    private void setQueryKeyHandleForTopLink(MWQueryKeyHandle queryKeyHandle) {
        NodeReferenceScrubber scrubber = this.buildQueryKeyScrubber();
        this.queryKeyHandle = ((queryKeyHandle == null) ? new MWQueryKeyHandle(this, scrubber) : queryKeyHandle.setScrubber(scrubber));
    }

}
