package ch.innovazion.polymerge;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import ch.innovazion.polymerge.utils.LineStream;
import ch.innovazion.polymerge.utils.PatchUtils;

public class Manifest {
	
	private PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:___.___");
	
	public Manifest(Path manifest) {
		if(Files.exists(manifest)) {
			try {
				LineStream stream = new LineStream(Files.readAllLines(manifest));
				
				PatchUtils.find("@static", stream).ifPresent((selector) -> {
					this.matcher = FileSystems.getDefault().getPathMatcher(selector);
					System.out.println("[Manifest] Static resources: " + selector);
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public PathMatcher getPathMatcher() {
		return matcher;
	}
 }
