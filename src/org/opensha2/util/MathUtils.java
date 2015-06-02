package org.opensha2.util;

import static java.lang.Math.sqrt;

/**
 * Miscellaneous utils that should be more appropriately located (TODO)
 * possibly rename to NSHMP_Utils
 * These are mostly ported from gov.usgs.earthquake.nshm.util
 * @author Peter Powers
 */
public final class MathUtils {

	// no instantiation
	private MathUtils() {}
	
	/**
	 * Same as {@link Math#hypot(double, double)} without regard to under/over flow.
	 * @param v1 first value
	 * @param v2 second value
	 * @return the hypotenuse
	 * @see Math#hypot(double, double)
	 */
	public static double hypot(double v1, double v2) {
		return sqrt(v1 * v1 + v2 * v2);
	}

}
