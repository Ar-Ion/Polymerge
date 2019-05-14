package ch.innovazion.polymerge.transforms;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class AppendTransform extends RawTransform {
	public AppendTransform(Path root) {
		super(root);
	}

	protected PrintWriter getWriter(Path target) throws IOException {
		return new PrintWriter(Files.newBufferedWriter(target, StandardOpenOption.APPEND));
	}
}