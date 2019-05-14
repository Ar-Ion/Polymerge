package ch.innovazion.polymerge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Polymerge {
	
	private final Path sources;
	private final Path patched;
	private final Path core;
	
	private final String target;
	
	public Polymerge(Path sources, Path patched, String target) {
		this.sources = sources;
		this.patched = patched;
		this.core = sources.resolve("core");
		
		this.target = target;
	}
	
	public void start() {
		try {
			Files.createDirectories(sources);
			Files.createDirectories(patched);
			Files.createDirectories(core);
		} catch (IOException e) {
			System.err.println("Failed to create root directories.");
			return;
		}
			
		System.out.println("Starting HotPatcher for target '" + target + "'...");
		
		String[] splitted = target.split("\\.");
		String main = target;
		
		if(splitted.length > 0) {
			main = splitted[0];
		}
		
		Path patches = sources.resolve(main);
		Path output = patched.resolve(target);
		
		if(Files.exists(patches) && Files.isDirectory(patches)) {
			Patcher patcher = new HotPatcher(target, core, patches, output);
			
			try {
				patcher.patch();
				System.out.println("Patcher terminated normally.");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Patcher terminated with failure.");
			}
			
		} else {
			System.err.println("Nothing to be done.");
		}
	}
}
