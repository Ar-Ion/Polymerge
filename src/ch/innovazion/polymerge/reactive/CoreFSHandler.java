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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchService;
import java.util.Collections;

import ch.innovazion.polymerge.utils.IOUtils;

public class CoreFSHandler extends FileSystemHandler {
	
	private final Path output;
	private final PatchCache cache;
	
	public CoreFSHandler(Path base, Path output, PatchCache cache) throws IOException {
		super(base);
		this.output = output;
		this.cache = cache;
	}

	public void handleChange(WatchService service, Path path, Path relative, Kind<Path> kind) throws IOException {
		Path target = output.resolve(relative);
		
		System.out.println("Watchservice (Core) [" + kind + "]: " + relative);
		
		if(Files.isDirectory(path)) {
			path.register(service, listenableEvents);
							
			if(kind == ENTRY_CREATE) {
				Files.createDirectories(target);
			} else if(kind == ENTRY_DELETE) {
				IOUtils.deleteDirectory(target, Collections.emptyList());
			}
		} else {
			if(kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
				Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
				cache.patch(relative.toString());
			} else if(kind == ENTRY_DELETE) {
				Files.deleteIfExists(target);
			}
		}
	}
}
