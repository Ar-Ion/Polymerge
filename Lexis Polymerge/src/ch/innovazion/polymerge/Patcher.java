package ch.innovazion.polymerge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import ch.innovazion.transforms.AppendTransform;
import ch.innovazion.transforms.MergeTransform;
import ch.innovazion.transforms.ReplaceTransform;
import ch.innovazion.transforms.SourceTransform;

public class Patcher {
	private final String target;
	private final File core;
	private final File version;
	private final File output;
	
	public Patcher(String target, File core, File version, File output) {
		this.target = target;
		this.core = core;
		this.version = version;
		this.output = output;
	}
	
	public void patch() {
		try {
			install();
			patchAll(version, new ArrayList<>());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Installs the core source into the output.
	 */
	private void install() throws IOException {
		output.delete();
		output.mkdir();
		
		File[] sub = core.listFiles();
		
		for(File file : sub) {
			File out = new File(output, file.getName());
			Files.copy(file.toPath(), out.toPath());
		}
	}
	
	
	/*
	 * Patches all files in a directory.
	 */
	private void patchAll(File dir, List<String> hierarchy) throws IOException {
		File[] sub = dir.listFiles();
		
		for(File file : sub) {
			List<String> newParents = new ArrayList<>(hierarchy);
			
			newParents.add(file.getName());
			
			if(file.isDirectory()) {
				patchAll(file, hierarchy);
			} else if(file.isFile()) {
				patch(file, hierarchy);
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}
	
	/*
	 * Patches a specific file.
	 */
	private void patch(File file, List<String> hierarchy) throws IOException {
		LinkedList<String> lines = new LinkedList<String>(Files.readAllLines(file.toPath()));
		Configuration config = new Configuration(String.join(".", hierarchy));
		
		config.read(lines);
		
		if(shouldPatch(config)) {
			SourceTransform transform = null;
			
			switch(config.getPatchMode()) {
				case REPLACE:
					transform = new ReplaceTransform(output);
					break;
				case APPEND:
					transform = new AppendTransform(output);
					break;
				case MERGE:
					transform = new MergeTransform(output);
					break;
				default:
					throw new UnsupportedOperationException();
			}		
					
			transform.apply(config.getLocation(), lines);
		}
	}
	
	/*
	 * Checks if there is any "@target" instruction specified at the beginning of the file.
	 * Returns true if the specified target matches the current target.
	 */
	private boolean shouldPatch(Configuration config) {
		Optional<String> specifiedTarget = config.getTarget();
		
		if(specifiedTarget.isPresent()) {
			return specifiedTarget.get().equalsIgnoreCase(target);
		} else {
			return true;
		}
	}
}
