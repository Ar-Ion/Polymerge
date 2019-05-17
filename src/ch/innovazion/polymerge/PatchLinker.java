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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;

import ch.innovazion.polymerge.utils.LineStream;
import ch.innovazion.polymerge.utils.PatchUtils;

public class PatchLinker extends Observable {
	
	private final Map<Path, LinkerCacheEntry> cache = new HashMap<>();
	
	
	public LineStream link(LineStream stream, Path source) throws IOException {
		return link(stream, source, new HashSet<>());
	}
	
	/*
	 * Returns the LineStream representation of the requested imports according to the @import instructions.
	 * Warning: this method does not preserve the stream meta data, such as the marks (and the internal position and limit).
	 */
	private LineStream link(LineStream stream, Path source, HashSet<String> alreadyImported) throws IOException {
		List<String> linkedLines = new ArrayList<>();
		List<String> imports = readImports(stream);
		
		for(String name : imports) {
			if(!alreadyImported.contains(name)) {
				linkedLines.addAll(resolveImport(source, name));
				alreadyImported.add(name);
			}
		}
		
		stream.forEach(linkedLines::add);
		
		LineStream linkedStream = new LineStream(linkedLines);
		
		if(imports.size() > 0) {
			return link(linkedStream, source, alreadyImported);
		}

		
		return linkedStream;
	}
	
	/*
	 * Recursively attempts to read imports until there are no more left to process.
	 */
	private List<String> readImports(LineStream stream) {
		List<String> imports = new ArrayList<>();
		
		PatchUtils.find("@import", stream).ifPresent(include -> {
			imports.add(include);
			readImports(stream);
		});
		
		return imports;
	}
	
	/*
	 * If a given patch is a dependency of another patch, the latter has to be notified when the former is modified.
	 */
	public List<Path> getReferencers(Path patch) {
		return Optional.ofNullable(cache.get(patch)).flatMap(LinkerCacheEntry::getReferencers).orElse(Collections.emptyList());
	}
	
	public void invalidateImport(Path path) {
		cache.remove(path);
	}
	
	/*
	 * Tries to fetch a pre-loaded version of the patch to import from the cache.
	 */
	private List<String> resolveImport(Path source, String name) throws IOException {
		Path resolved = source.getParent().resolve(name);
		
		if(Files.exists(resolved)) {
			LinkerCacheEntry entry = cache.computeIfAbsent(resolved.toRealPath(), LinkerCacheEntry::new);
			
			entry.referencers.add(source);
			
			return entry.getContent();
		} else {
			System.err.println("[Linker] Failed to resolve import '" + name + "' while linking '" + source + "'");
		}
		
		return Collections.emptyList();
	}
		
	
	
	private class LinkerCacheEntry {
		
		private final List<String> content = new ArrayList<>();
		private final List<Path> referencers = new ArrayList<>();
				
		private LinkerCacheEntry(Path path) {			
			try {
				this.content.addAll(Files.readAllLines(path));
			} catch(IOException e) {
				System.err.println("[Linker] Failed to import '" + path + "'");
			}

			PatchLinker.this.setChanged();
			PatchLinker.this.notifyObservers(path);
		}
		
		private List<String> getContent() {
			return Collections.unmodifiableList(content);
		}
		
		private Optional<List<Path>> getReferencers() {
			return Optional.of(Collections.unmodifiableList(referencers));
		}
	}
}