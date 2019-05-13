package ch.innovazion.polymerge.transforms;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ReplaceTransform extends RawTransform {

	public ReplaceTransform(File root) {
		super(root);
	}

	protected PrintWriter getWriter(File target) throws IOException {
		return new PrintWriter(target);
	}
}