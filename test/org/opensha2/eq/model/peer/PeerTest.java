package org.opensha2.eq.model.peer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.abs;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.opensha2.util.Parsing.Delimiter.COMMA;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.junit.Test;
import org.opensha2.calc.CalcConfig;
import org.opensha2.calc.HazardResult;
import org.opensha2.calc.Site;
import org.opensha2.eq.model.HazardModel;
import org.opensha2.gmm.Imt;
import org.opensha2.mfd.Mfds;
import org.opensha2.programs.HazardCurve;
import org.opensha2.util.Parsing;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;

@SuppressWarnings("javadoc")
// @RunWith(Parameterized.class)
public class PeerTest {

	public static final String S1_C1 = "Set1-Case1";
	public static final String S1_C2 = "Set1-Case2";
	public static final String S1_C2_F = "Set1-Case2-fast";
	public static final String S1_C3 = "Set1-Case3";
	public static final String S1_C3_F = "Set1-Case3-fast";
	public static final String S1_C4 = "Set1-Case4";
	public static final String S1_C4_F = "Set1-Case4-fast";
	public static final String S1_C5 = "Set1-Case5";
	public static final String S1_C5_F = "Set1-Case5-fast";
	public static final String S1_C6 = "Set1-Case6";
	public static final String S1_C6_F = "Set1-Case6-fast";
	public static final String S1_C7 = "Set1-Case7";
	public static final String S1_C7_F = "Set1-Case7-fast";
	public static final String S1_C8A = "Set1-Case8a";
	public static final String S1_C8B = "Set1-Case8b";
	public static final String S1_C8B_F = "Set1-Case8b-fast";
	public static final String S1_C8C = "Set1-Case8c";
	public static final String S1_C10 = "Set1-Case10";
	public static final String S1_C10_F = "Set1-Case10-fast";
	public static final String S1_C11 = "Set1-Case11";
	public static final String S1_C11_F = "Set1-Case11-fast";

	public static final String S2_C1 = "Set2-Case1";
	public static final String S2_C2A = "Set2-Case2a";
	public static final String S2_C2A_F = "Set2-Case2a-fast";
	public static final String S2_C2B = "Set2-Case2b";
	public static final String S2_C2B_F = "Set2-Case2b-fast";
	public static final String S2_C2C = "Set2-Case2c";
	public static final String S2_C2C_F = "Set2-Case2c-fast";
	public static final String S2_C2D = "Set2-Case2d";
	public static final String S2_C2D_F = "Set2-Case2d-fast";
	public static final String S2_C3A = "Set2-Case3a";
	public static final String S2_C3A_F = "Set2-Case3a-fast";
	public static final String S2_C3B = "Set2-Case3b";
	public static final String S2_C3B_F = "Set2-Case3b-fast";
	public static final String S2_C3C = "Set2-Case3c";
	public static final String S2_C3C_F = "Set2-Case3c-fast";
	public static final String S2_C3D = "Set2-Case3d";
	public static final String S2_C3D_F = "Set2-Case3d-fast";
	public static final String S2_C4A = "Set2-Case4a";
	public static final String S2_C4A_F = "Set2-Case4a-fast";
	public static final String S2_C4B = "Set2-Case4b";
	public static final String S2_C4B_F = "Set2-Case4b-fast";
	public static final String S2_C5A = "Set2-Case5a";
	public static final String S2_C5B = "Set2-Case5b";

	// private static final Path PEER_DIR = Paths.get("etc", "peer");
	private static final Path PEER_DIR = Paths.get("..", "nshmp-model-dev", "models", "PEER");
	private static final Path MODEL_DIR = PEER_DIR.resolve("models");
	private static final Path RESULT_DIR = PEER_DIR.resolve("results");

	/*
	 * Each models specifies the sites and periods of interest in its config For
	 * each model:
	 * 
	 * Load model Parameterize sites Load result -pair site name in result
	 * 
	 * 
	 * (fast and slow variants)
	 */

	private String modelName; // just used to improve test name
	private HazardModel model;
	private Site site;
	private double[] expected;
	private double tolerance;

	public PeerTest(
			String modelName,
			HazardModel model,
			Site site,
			double[] expected,
			double tolerance) {

		this.modelName = modelName;
		this.model = model;
		this.site = site;
		this.expected = expected;
		this.tolerance = tolerance;
	}

	@Test public void test() {
		System.out.println(site.name);
		HazardResult result = HazardCurve.calc(model, model.config(), site,
			Optional.<Executor> absent());
		// compute y-values converting to Poiss prob
		double[] actual = Doubles.toArray(
			FluentIterable.from(result.curves().get(Imt.PGA).yValues())
				.transform(Mfds.rateToProbConverter())
				.toList()
			);
		checkArgument(actual.length == expected.length);

		assertArrayEquals(expected, actual, tolerance);
		for (int i = 0; i < expected.length; i++) {
			String message = String.format("arrays differ at [%s] expected:<[%s]> but was:<[%s]>",
				i, expected[i], actual[i]);
			assertTrue(message, compare(expected[i], actual[i], tolerance));
		}
	}

	private static boolean compare(double expected, double actual, double tolerance) {
		return abs(actual - expected) / expected < tolerance ||
			Double.valueOf(expected).equals(Double.valueOf(actual));
	}

	static List<Object[]> load(String modelId, double tolerance) throws IOException {
		Map<String, double[]> expectedsMap = loadExpecteds(modelId);
		HazardModel model = HazardModel.load(MODEL_DIR.resolve(modelId));
		CalcConfig config = model.config();

		// ensure that only PGA is being used
		checkState(config.imts().size() == 1);
		checkState(config.imts().iterator().next() == Imt.PGA);

		List<Object[]> argsList = new ArrayList<>();
		for (Site site : config.sites()) {
			checkState(expectedsMap.containsKey(site.name()));
			Object[] args = new Object[] {
				model.name(),
				model,
				site,
				expectedsMap.get(site.name()),
				tolerance
			};
			argsList.add(args);
		}
		return argsList;
	}

	private static Map<String, double[]> loadExpecteds(String modelId) throws IOException {
		Path results = RESULT_DIR.resolve(modelId + ".csv");
		List<String> lines = Files.readAllLines(results, UTF_8);
		Map<String, double[]> siteValuesMap = new HashMap<>();
		for (String line : Iterables.skip(lines, 1)) {
			String[] splitLine = line.split(",", 4);
			String siteName = splitLine[0];
			List<Double> values = Parsing.splitToDoubleList(splitLine[3], COMMA);
			siteValuesMap.put(siteName, Doubles.toArray(values));
		}
		return siteValuesMap;
	}

	public static void main(String[] args) {
		String model = MODEL_DIR.resolve(S1_C11_F).toString();
		HazardCurve.main(new String[] { model });
	}

}
