/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2019 Innovazion
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package ch.innovazion.polymerge;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import ch.innovazion.polymerge.transforms.AppendTransform;
import ch.innovazion.polymerge.transforms.AssetTransform;
import ch.innovazion.polymerge.transforms.MergeTransform;
import ch.innovazion.polymerge.transforms.ReplaceTransform;
import ch.innovazion.polymerge.transforms.SourceTransform;
import ch.innovazion.polymerge.utils.IOUtils;
import ch.innovazion.polymerge.utils.LineStream;
import ch.innovazion.polymerge.utils.PatchUtils;

public class Patcher {
	private final String target;
	private final Path core;
	private final Path patches;
	private final Path output;
	
	private final Manifest manifest;
	
	public Patcher(String target, Path core, Path patches, Path output) {
		this.target = target;
		this.core = core;
		this.patches = patches;
		this.output = output;
		
		this.manifest = new Manifest(core.resolve("manifest"));
	}
		
	public void patch() throws IOException {
		install();
		patchAll(patches);
	}
	
	/*
	 * Installs the core code base into the output.
	 */
	private void install() throws IOException {
		if(Files.exists(output)) {
			IOUtils.deleteDirectory(output, manifest.getPathMatchers());
		}
		
		PathMatcher manifestMatcher = FileSystems.getDefault().getPathMatcher("glob:manifest");		
		IOUtils.copyDirectory(core, output, Arrays.asList(manifestMatcher));
	}
		
	/*
	 * Patches all files in a directory.
	 * Priority for the most nested patches.
	 */
	private void patchAll(Path dir) throws IOException {
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if(!Files.isHidden(file)) { // Not a symlink directory					
					if(attrs.isSymbolicLink()) {
						Path resolved = Files.readSymbolicLink(file);
						
						if(Files.isDirectory(resolved)) {
							patchAll(resolved);
						} else {
							patch(resolved);
						}
					} else {
					    patch(file);
					}
				}
				
			    return FileVisitResult.CONTINUE;
		    }
		};
		
		Files.walkFileTree(dir, visitor);
	}
	
	/*
	 * Reads a patch and checks for multiple patch entries.
	 */
	protected void patch(Path file) throws IOException {
		LineStream stream = new LineStream(Files.readAllLines(file));		
		LinkedList<Integer> locations = new LinkedList<>();
		
		stream.mark();
		
		while(PatchUtils.find("@locate", stream).isPresent()) {
			locations.add(stream.getPosition());
		}
		
		stream.reset();
		
		locations.removeFirst();
		locations.addLast(stream.length());
		
		for(int nextLocation : locations) {
			stream.limit(nextLocation);
			patchLocation(file, stream);
		}
	}
	
	/*
	 * Patches a target according to a given LineStream.
	 */
	protected Configuration patchLocation(Path path, LineStream stream) throws IOException {
		Configuration config = new Configuration(patches.relativize(path).toString());
		
		config.read(stream);
		
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
				case ASSET:
					transform = new AssetTransform(output);
					break;
				default:
					throw new UnsupportedOperationException();
			}		
					
			transform.apply(config.getLocation(), stream);
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
			return target.contains(specifiedTarget.get());
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
