package ch.innovazion.polymerge;

import java.nio.file.Paths;

public class Main {
	public static void main(String args[]) {
		for(String target : args) {
			new Thread(() ->  {
				Polymerge polymerge = new Polymerge(Paths.get("sources"), Paths.get("patched"), target);
				polymerge.start();
			}).start();
		}
	}
}
