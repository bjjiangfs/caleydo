/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.enroute.correlation;

import java.util.Collections;
import java.util.List;

import org.caleydo.core.util.color.Color;

import com.google.common.collect.Lists;

/**
 * @author Christian
 *
 */
public class NumericalThresholdClassifier implements IDataClassifier {

	protected final float threshold;
	protected final List<SimpleCategory> dataClasses;

	/**
	 * @param threshold
	 */
	public NumericalThresholdClassifier(float threshold) {
		this.threshold = threshold;
		dataClasses = Lists.newArrayList(new SimpleCategory("less than or equal " + threshold, Color.CYAN),
				new SimpleCategory("greater than " + threshold, Color.MAGENTA));
	}

	@Override
	public SimpleCategory apply(Object input) {
		if (input instanceof Number) {
			Number num = (Number) input;
			if (num.floatValue() <= threshold) {
				return dataClasses.get(0);
			} else {
				return dataClasses.get(1);
			}
		}
		return null;
	}

	/**
	 * @return the threshold, see {@link #threshold}
	 */
	public float getThreshold() {
		return threshold;
	}

	@Override
	public List<SimpleCategory> getDataClasses() {
		return Collections.unmodifiableList(dataClasses);
	}

}