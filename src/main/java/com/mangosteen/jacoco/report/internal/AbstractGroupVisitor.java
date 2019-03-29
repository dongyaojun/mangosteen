/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package com.mangosteen.jacoco.report.internal;

import com.mangosteen.jacoco.core.analysis.CoverageNodeImpl;
import com.mangosteen.jacoco.core.analysis.IBundleCoverage;
import com.mangosteen.jacoco.core.analysis.ICoverageNode;
import com.mangosteen.jacoco.report.IReportGroupVisitor;
import com.mangosteen.jacoco.report.ISourceFileLocator;

import java.io.IOException;

/**
 * Internal base visitor to calculate group counter summaries for hierarchical
 * reports.
 */
public abstract class AbstractGroupVisitor implements IReportGroupVisitor {

	/** coverage node for this group to total counters */
	protected final CoverageNodeImpl total;

	private AbstractGroupVisitor lastChild;

	/**
	 * Creates a new group with the given name.
	 *
	 * @param name
	 *            name for the coverage node created internally
	 */
	protected AbstractGroupVisitor(final String name) {
		total = new CoverageNodeImpl(ICoverageNode.ElementType.GROUP, name);
	}

	public final void visitBundle(final IBundleCoverage bundle,
			final ISourceFileLocator locator) throws IOException {
		finalizeLastChild();
		total.increment(bundle);
		handleBundle(bundle, locator);
	}

	/**
	 * Called to handle the given bundle in a specific way.
	 *
	 * @param bundle
	 *            analyzed bundle
	 * @param locator
	 *            source locator
	 * @throws IOException
	 *             if the report can't be written
	 */
	protected abstract void handleBundle(IBundleCoverage bundle,
                                         ISourceFileLocator locator) throws IOException;

	public final IReportGroupVisitor visitGroup(final String name)
			throws IOException {
		finalizeLastChild();
		lastChild = handleGroup(name);
		return lastChild;
	}

	/**
	 * Called to handle a group with the given name in a specific way.
	 *
	 * @param name
	 *            name of the group
	 * @return created child group
	 * @throws IOException
	 *             if the report can't be written
	 */
	protected abstract AbstractGroupVisitor handleGroup(final String name)
			throws IOException;

	/**
	 * Must be called at the end of every group.
	 *
	 * @throws IOException
	 *             if the report can't be written
	 */
	public final void visitEnd() throws IOException {
		finalizeLastChild();
		handleEnd();
	}

	/**
	 * Called to handle the end of this group in a specific way.
	 *
	 * @throws IOException
	 *             if the report can't be written
	 */
	protected abstract void handleEnd() throws IOException;

	private void finalizeLastChild() throws IOException {
		if (lastChild != null) {
			lastChild.visitEnd();
			total.increment(lastChild.total);
			lastChild = null;
		}
	}

}