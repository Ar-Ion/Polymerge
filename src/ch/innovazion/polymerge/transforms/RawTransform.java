package ch.innovazion.polymerge.transforms;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

public abstract class RawTransform extends SourceTransform {

	public RawTransform(File root) {
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
	
	protected abstract PrintWriter getWriter(File target) throws IOException;
}