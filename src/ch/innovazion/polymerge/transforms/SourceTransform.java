package ch.innovazion.polymerge.transforms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

public abstract class SourceTransform {
	
	private final Path root;
	
	public SourceTransform(Path root) {
		this.root = root;
	}
	
	protected Path resolveIdentifier(String identifier) throws IOException {
		Path resolved = root.resolve(identifier);
		
		Files.createDirectories(resolved.getParent());
		Files.createFile(resolved);
		
		return resolved;
	}
	
	public abstract void apply(String identifier, LinkedList<String> patchData) throws IOException;
}
