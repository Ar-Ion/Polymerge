package ch.innovazion.polymerge;

import java.io.File;

public class Main {
	
	private static final File sources = new File("rn-src");
	private static final File patched = new File("rn-patched");

	private static final File core = new File(sources, "core");
	
	public static void main(String args[]) {
		if(!sources.exists()) {
			sources.mkdirs();
		}
		
		if(!patched.exists()) {
			patched.mkdirs();
		}
		
		if(!core.exists()) {
			core.mkdir();
		}
		
		if(args.length > 0) {
			String target = args[0];
			
			System.out.println("Starting HotPatcher for target " + target + "...");
			
			File src = new File(sources, target);
			File output = new File(patched, target);
			
			if(src.exists() && src.isDirectory()) {
				
			} else {
				System.out.println("Nothing to be done.");
			}
		}
	}
}
