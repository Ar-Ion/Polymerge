package ch.innovazion.polymerge.transforms;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ReplaceTransform extends RawTransform {

	public ReplaceTransform(Path root) {
		super(root);
	}

	protected PrintWriter getWriter(Path target) throws IOException {
		return new PrintWriter(Files.newOutputStream(target, StandardOpenOption.TRUNCATE_EXISTING));
	}
}