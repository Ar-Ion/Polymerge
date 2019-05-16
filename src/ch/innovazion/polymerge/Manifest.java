package ch.innovazion.polymerge;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.innovazion.polymerge.utils.LineStream;
import ch.innovazion.polymerge.utils.PatchUtils;

public class Manifest {
	
	private List<PathMatcher> staticMatchers = new ArrayList<>();
	
	public Manifest(Path manifest) {
		if(Files.exists(manifest)) {
			try {
				LineStream stream = new LineStream(Files.readAllLines(manifest));
				
				while(stream.hasNext()) {
					PatchUtils.find("@static", stream).ifPresent((selector) -> {
						staticMatchers.add(FileSystems.getDefault().getPathMatcher(selector));
						System.out.println("[Manifest] Static resources: " + selector);
					});
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public List<PathMatcher> getPathMatchers() {
		return Collections.unmodifiableList(staticMatchers);
	}
 }
