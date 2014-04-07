package org.opensha.eq.forecast;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

/**
 * Forecast source XML attributes.
 * @author Peter Powers
 */
@SuppressWarnings("javadoc")
public enum SourceAttribute {
	NAME,
	WEIGHT,
	
	DEPTH_MAP,
	MECH_MAP,
	
	/* Geometry specific */
	STRIKE,
	DIP,
	WIDTH,
	RAKE,
	DEPTH,
	
	/* MFD specific */
	TYPE,
	A,
	M,
	B,
	M_MIN,
	M_MAX,
	D_MAG,
	FLOATS,
	MAG_SCALING,
	MAGS,
	RATES;
	
	/**
	 * Returns an {@code CaseFormat#LOWER_CAMEL} {@code String} representation
	 * of this {@code MFD_Attribute}.
	 */
	@Override
	public String toString() { 
		return UPPER_UNDERSCORE.to(LOWER_CAMEL, name());
	}

	/**
	 * Converts supplied {@code String} to equivalent {@code MFD_Attribute}.
	 * Method expects a {@code String} with {@code CaseFormat#LOWER_CAMEL}
	 * @param s {@code String} to convert
	 * @return the corresponding {@code MFD_Attribute}
	 * @see CaseFormat
	 * @throws IllegalArgumentException if supplied String is incorrectly
	 *         formatted or no matching {@code MFD_Attribute} exists
	 */
	public static SourceAttribute fromString(String s) {
		return valueOf(LOWER_CAMEL.to(UPPER_UNDERSCORE, s));
	}

}
