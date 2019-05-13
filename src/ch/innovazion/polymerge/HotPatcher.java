package ch.innovazion.polymerge;

import java.io.File;

public class HotPatcher extends Patcher {
	public HotPatcher(String target, File core, File version, File output) {
		super(target, core, version, output);
	}
}
