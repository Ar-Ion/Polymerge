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
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.sun.nio.file.SensitivityWatchEventModifier;

public abstract class FileSystemHandler {
	
	protected static final Kind<?>[] listenableEvents = { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW };

	private final Path base;
	private final WatchService service;
	
	public FileSystemHandler(Path base) throws IOException {
		this.base = base;
		this.service = FileSystems.getDefault().newWatchService();
	}
	
	public void processQueue() {
		WatchKey key = service.poll();
		
		if(key != null) {
			key.pollEvents().stream().map(this::cast).forEach((event) -> {
				Path path = ((Path) key.watchable()).resolve(event.context());
				
				Path relative = null;
				
				if(!base.equals(path)) {
					try {
						relative = base.relativize(path.toRealPath());
					} catch(IllegalArgumentException | IOException e) {
						System.err.println("Resource '" + path + "' is out of scope.");
						System.err.println("You must ensure all the resources you import remain in the same root project directory.");
					}
				} else {
					relative = Paths.get("<root>");
				}
				
				if(shouldHandle(base, path)) {
					try {
						handleChange(service, path, relative, event.kind());
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			key.reset();
		}
	}
	
	public void register(Path path) {
		try {
			path.register(service, listenableEvents, SensitivityWatchEventModifier.HIGH);
		} catch (IOException e) {
			System.err.println("Unable to register a watch service for '" + path + "'");
		}
	}
	
	public void registerRecursive(Path root) {
		try {
			Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
			    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			    	register(dir);
			        return FileVisitResult.CONTINUE;
			    }
			});
		} catch (IOException e) {
			System.err.println("Unable to register a watch service for '" + root + "' and its subdirectories");
		}
	}
	
	protected boolean shouldHandle(Path base, Path path) {
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private WatchEvent<Path> cast(WatchEvent<?> event) {
		return (WatchEvent<Path>) event;
	}
	
	protected abstract void handleChange(WatchService service, Path path, Path relative, Kind<Path> kind) throws IOException;
}
