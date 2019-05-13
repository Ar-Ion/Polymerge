package ch.innovazion.polymerge.test;

import java.nio.file.Paths;

import ch.innovazion.polymerge.PolyMerge;

public class FeatureTests {
	
	private static final String currentTest = "alpha";
	
	public static void main(String args[]) {
		PolyMerge polymerge = new PolyMerge(Paths.get("test-sources"), Paths.get("test-patched"), currentTest);
		polymerge.start();
	}
}
