package ch.innovazion.polymerge.transforms;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import ch.innovazion.polymerge.utils.LineStream;

public abstract class RawTransform extends SourceTransform {

	public RawTransform(Path root) {
		super(root);
	}

	public void apply(String identifier, LineStream stream) throws IOException {
		PrintWriter writer = getWriter(resolveIdentifier(identifier));
		
		try {
			stream.forEach(writer::println);
		} finally {
			writer.close();
		}
	}
	
	protected abstract PrintWriter getWriter(Path target) throws IOException;
}