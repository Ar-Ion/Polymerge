package ch.innovazion.polymerge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
	
	private static final Path sources = Paths.get("src");
	private static final Path patched = Paths.get("patched");

	private static final Path core = sources.resolve("core");
	
	public static void main(String args[]) {
		try {
			Files.createDirectories(sources);
			Files.createDirectories(patched);
			Files.createDirectory(core);
		} catch (IOException e) {
			System.err.println("Failed to create root directories.");
			return;
		}

		if(args.length > 0) {
			String target = args[0];
			
			System.out.println("Starting HotPatcher for target " + target + "...");
			
			Path patches = sources.resolve(target);
			Path output = patched.resolve(target);
			
			if(Files.exists(patches) && Files.isDirectory(patches)) {
				Patcher patcher = new HotPatcher(target, core, patches, output);
				
				try {
					patcher.patch();
					System.out.println("Patcher terminated normally.");
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Patcher terminated with fatal failure.");
				}
				
			} else {
				System.err.println("Nothing to be done.");
			}
		}
	}
}
