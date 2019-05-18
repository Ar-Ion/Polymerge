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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.WatchService;

import ch.innovazion.polymerge.PatchLinker;

public class DynamicFSHandler extends FileSystemHandler {
	
	private final HotPatcher patcher;
	private final PatchLinker linker;
	
	public DynamicFSHandler(String name, Path base, HotPatcher patcher, PatchLinker linker) throws IOException {
		super(name, base);
		
		this.patcher = patcher;
		this.linker = linker;
	}

	public void handleChange(WatchService service, Path path, Path relative, Kind<Path> kind) throws IOException {		
		System.out.println(getDebugPrependable() + "Watchservice (Dynamic Resources) [" + kind + "]: " + relative);
		
		if(Files.exists(path)) {
			List<Path> referencers = new ArrayList<>(linker.getReferencers(path));
						
			linker.invalidateImport(path);
							
			for(Path referencer : referencers) {
				patcher.hotPatch(referencer);
			}	
		}
	}
	
	public boolean shouldHandle(Path base, Path path) {
		return !linker.getReferencers(path).isEmpty();
	}
}
