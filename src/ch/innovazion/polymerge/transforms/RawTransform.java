package ch.innovazion.polymerge.transforms;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedList;

public abstract class RawTransform extends SourceTransform {

	public RawTransform(Path root) {
		super(root);
	}
		
	/*
	 * Simply overrides the whole file regardless of the "core" code base.
	 */
	public void apply(String identifier, LinkedList<String> patchData) throws IOException {
		PrintWriter writer = getWriter(resolveIdentifier(identifier));
		
		try {
			patchData.forEach(writer::println);
		} finally {
			writer.close();
		}
	}
	
	protected abstract PrintWriter getWriter(Path target) throws IOException;
}