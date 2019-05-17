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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.sun.nio.file.SensitivityWatchEventModifier;

import ch.innovazion.polymerge.utils.IOConsumer;
import ch.innovazion.polymerge.utils.IOUtils;
import ch.innovazion.polymerge.utils.LineStream;

public class HotPatcher extends Patcher implements Observer {
	
	private static final Kind<?>[] listenableEvents = { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW };
	
	private final Map<Path, String> locationCache = new HashMap<>();
	private final Map<String, Set<Path>> reverseLocationCache = new HashMap<>();

	private final WatchService coreService;
	private final WatchService patchesService;
	private final WatchService dynamicResourcesService;
	
	public HotPatcher(String target, Path core, Path patches, Path output) {
		super(target, core, patches, output);
		
		try {
			coreService = FileSystems.getDefault().newWatchService();
			patchesService = FileSystems.getDefault().newWatchService();
			dynamicResourcesService = FileSystems.getDefault().newWatchService();
		} catch(IOException e) {
			System.err.println("[" + target + "] Unable to create a watch service using the default filesystem");
			System.exit(666);
			throw new RuntimeException();
		}
		
		getLinker().addObserver(this);
	}
	
	public void patch() throws IOException {
		System.out.println("[" + getTargetName() + "] Applying a full patch...");

		super.patch();
		
		System.out.println("[" + getTargetName() + "] Starting watch service...");

		registerRecursiveWatchService(getCore(), coreService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
		registerRecursiveWatchService(getPatches(), patchesService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);

		while(true) {
			try {
				WatchKey coreKey = coreService.poll();
				WatchKey patchesKey = patchesService.poll();
				WatchKey dynamicKey = dynamicResourcesService.poll();

				if(coreKey != null) {
					coreKey.pollEvents().stream().map(this::cast).forEach((event) -> {
						handleCoreChange(coreService, (Path) coreKey.watchable(), event);
					});
					
					coreKey.reset();
				}
				
				if(patchesKey != null) {
					patchesKey.pollEvents().stream().map(this::cast).forEach((event) -> {
						handlePatchChange(patchesService, (Path) patchesKey.watchable(), event);
					});
					
					patchesKey.reset();
				}
				
				if(dynamicKey != null) {
					dynamicKey.pollEvents().stream().map(this::cast).forEach((event) -> {
						handleDynamicResourceChange(patchesService, (Path) dynamicKey.watchable(), event);
					});
					
					dynamicKey.reset();
				}
												
				Thread.sleep(200);
			} catch (InterruptedException e) {
				;
			}
		}
	}
	
	private void registerRecursiveWatchService(Path root, WatchService service, Kind<?>... events) throws IOException {
		Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
	        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	        	dir.register(service, events, SensitivityWatchEventModifier.HIGH);
	            return FileVisitResult.CONTINUE;
	        }
	    });
	}
	
	private void handleCoreChange(WatchService service, Path source, WatchEvent<Path> event) {
		Path modified = source.resolve(event.context());
		Path relative = getCore().relativize(modified);
		Path target = getOutput().resolve(relative);

		Kind<Path> kind = event.kind();
		
		try {
			System.out.println("[" + getTargetName() + "] Watchservice (Core) [" + kind + "]: " + relative);
			
			if(Files.isDirectory(modified)) {
				modified.register(service, listenableEvents);
								
				if(kind == ENTRY_CREATE) {
					Files.createDirectories(target);
				} else if(kind == ENTRY_DELETE) {
					IOUtils.deleteDirectory(target, Arrays.asList());
				}
			} else {
				if(kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
					Files.copy(modified, target, StandardCopyOption.REPLACE_EXISTING);
					patchFromCache(relative.toString());
				} else if(kind == ENTRY_DELETE) {
					Files.deleteIfExists(target);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void patchFromCache(String location) throws IOException {
		List<Path> patches = new ArrayList<>(reverseLocationCache.get(location));
		
		if(patches != null) {
			patches.forEach(IOConsumer.of(super::patch));
		}
	}
	
	private void handlePatchChange(WatchService service, Path source, WatchEvent<Path> event) {
		Path modified = source.resolve(event.context());
		Path relative = getPatches().relativize(modified);
		
		Kind<Path> kind = event.kind();
		
		try {
			System.out.println("[" + getTargetName() + "] Watchservice (Patches) [" + kind + "]: " + relative);
						
			if(Files.isDirectory(modified)) {
				modified.register(service, listenableEvents);
			} else {
				repatch(modified);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleDynamicResourceChange(WatchService service, Path source, WatchEvent<Path> event) {
		Path modified = source.resolve(event.context());
		Path relative = getPatches().resolve(event.context());
		
		PatchLinker linker = getLinker();
		
		try {
			System.out.println("[" + getTargetName() + "] Watchservice (Dynamic Resources) [" + event.kind() + "]: " + relative);
						
			if(Files.exists(modified)) {
				Path realPath = modified.toRealPath();
				
				List<Path> referencers = new ArrayList<>(linker.getReferencers(realPath));
								
				linker.invalidateImport(realPath);
								
				for(Path referencer : referencers) {
					repatch(referencer);
				}	
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Patches a given target again using a patch file.
	 * 
	 * Involves:
	 * 	- The destruction and copying of the core file before patching
	 * 	- Cache invalidation
	 * 	- Actual patching of the target
	 */
	private void repatch(Path path) throws IOException {
		String location = locationCache.remove(path);
		
		if(location != null) {
			reverseLocationCache.get(location).remove(path);
			
			Files.copy(getCore().resolve(location), getOutput().resolve(location), StandardCopyOption.REPLACE_EXISTING);
		
			patchFromCache(location);
		}
			
		if(Files.exists(path)) {
			patch(path);
		}
	}
	
	/*
	 * Saves the patch <-> location entry in the cache.
	 */
	public Configuration patchLocation(Path path, LineStream stream) throws IOException {
		Configuration config = super.patchLocation(path, stream);
		
		locationCache.put(path, config.getLocation());
		reverseLocationCache.computeIfAbsent(config.getLocation(), e -> new HashSet<>()).add(path);
		
		return config;
	}
	
	
	@SuppressWarnings("unchecked")
	private WatchEvent<Path> cast(WatchEvent<?> event) {
		return (WatchEvent<Path>) event;
	}

	/*
	 * When the patch linker imports a new dynamic resource, 
	 * this method is called and the path of the dynamic resource becomes handled by the watch service.
	 */
	public void update(Observable o, Object arg) {
		Path path = (Path) arg;

		try {
			path.getParent().register(dynamicResourcesService, listenableEvents, SensitivityWatchEventModifier.HIGH);
		} catch (IOException e) {
			System.err.println("Unable to register a watch service for '" + path + "'");
		}
	}
}
