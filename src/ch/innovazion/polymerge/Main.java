package ch.innovazion.polymerge;

import java.nio.file.Paths;

public class Main {
	public static void main(String args[]) {
		if(args.length > 0) {
			Polymerge polymerge = new Polymerge(Paths.get("sources"), Paths.get("patched"), args[0]);
			polymerge.start();
		}
	}
}
