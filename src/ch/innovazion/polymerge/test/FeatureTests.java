package ch.innovazion.polymerge.test;

import java.nio.file.Paths;

import ch.innovazion.polymerge.PolyMerge;

public class FeatureTests {
	
	private static final String currentTest = "alpha";
	
	public static void main(String args[]) {
		Polymerge polymerge = new Polymerge(Paths.get("test-sources"), Paths.get("test-patched"), currentTest);
		polymerge.start();
	}
}
