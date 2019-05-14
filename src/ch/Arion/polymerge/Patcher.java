package ch.innovazion.polymerge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;

import ch.innovazion.polymerge.transforms.AppendTransform;
import ch.innovazion.polymerge.transforms.MergeTransform;
import ch.innovazion.polymerge.transforms.ReplaceTransform;
import ch.innovazion.polymerge.transforms.SourceTransform;
import ch.innovazion.polymerge.utils.IOConsumer;
import ch.innovazion.polymerge.utils.IOUtils;

public class Patcher {
	private final String target;
	private final Path core;
	private final Path patches;
	private final Path output;
	
	public Patcher(String target, Path core, Path patches, Path output) {
		this.target = target;
		this.core = core;
		this.patches = patches;
		this.output = output;
	}
		
	public void patch() throws IOException {
		install();
		patchAll(patches);
	}
	
	/*
	 * Installs the core code base into the output.
	 */
	private void install() throws IOException {
		IOUtils.deleteDirectory(output);
		IOUtils.copyDirectory(core, output);
	}
	
	/*
	 * Patches all files in a directory.
	 * Priority for the most nested patches.
	 */
	private void patchAll(Path dir) throws IOException {
	FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    patch(file);
    return FileVisitResult.CONTINUE;
    }
	};
	
	Files.walkFileTree(dir, visitor);
	}
	
	/*
	 * Patches a specific file.
	 */
	protected Configuration patch(Path file) throws IOException {
		LinkedList<String> lines = new LinkedList<String>(Files.readAllLines(file));
		Configuration config = new Configuration(patches.relativize(file).toString());
		
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
		
		return config;
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
	
	public String getTargetName() {
		return target;
	}
	
	protected Path getCore() {
		return core;
	}
	
	protected Path getPatches() {
		return patches;
	}
	
	protected Path getOutput() {
		return output;
	}
}
