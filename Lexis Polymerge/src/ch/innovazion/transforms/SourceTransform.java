package ch.innovazion.transforms;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public abstract class SourceTransform {
	
	private final File root;
	
	public SourceTransform(File root) {
		this.root = root;
	}
	
	protected File resolveIdentifier(String identifier) throws IOException {
		File resolved = new File(root, identifier.replace(".", File.separator));
		
		resolved.getParentFile().mkdirs();
		resolved.createNewFile();
		
		return resolved;
	}
	
	public abstract void apply(String identifier, LinkedList<String> patchData) throws IOException;
}
