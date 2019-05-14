package ch.innovazion.polymerge.test;

import java.nio.file.Paths;

import ch.innovazion.polymerge.Polymerge;

public class FeatureTests {
	
	private static final String[] testArgs = { "alpha.v2", "alpha", "omega" };
	
	public static void main(String args[]) {
		for(String target : testArgs) {
			new Thread(() ->  {
				Polymerge polymerge = new Polymerge(Paths.get("test-sources"), Paths.get("test-patched"), target);
				polymerge.start();
			}).start();
		}
	}
}
