package ch.innovazion.polymerge.transforms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AppendTransform extends RawTransform {
	public AppendTransform(File root) {
		super(root);
	}

	protected PrintWriter getWriter(File target) throws IOException {
		return new PrintWriter(new FileWriter(target, true));
	}
}