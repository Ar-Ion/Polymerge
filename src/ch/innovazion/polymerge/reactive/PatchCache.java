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
package ch.innovazion.polymerge.reactive;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class PatchCache {
	
	private final Map<Path, Set<String>> locationCache = new HashMap<>();
	private final Map<String, Set<Path>> reverseLocationCache = new HashMap<>();
	
	private final Consumer<Path> patcher;
	
	protected PatchCache(Consumer<Path> patcher) {
		this.patcher = patcher;
	}
	
	public void patch(String location) throws IOException {
		List<Path> patches = new ArrayList<>(reverseLocationCache.get(location));
		
		if(patches != null) {
			patches.forEach(patcher);
		}
	}
	
	public void addEntry(Path path, String location) {		
		locationCache.computeIfAbsent(path, e -> new HashSet<>()).add(location);
		reverseLocationCache.computeIfAbsent(location, e -> new HashSet<>()).add(path);
	}

	public Set<String> invalidate(Path path) {
		Set<String> locations = locationCache.remove(path);
		
		if(locations != null) {
			for(String location : locations) {
				reverseLocationCache.get(location).remove(path);
			}
			
			return locations;
		} else {
			return Collections.emptySet();
		}
	}
}
