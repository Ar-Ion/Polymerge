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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Observable;
import java.util.Observer;

import ch.innovazion.polymerge.Configuration;
import ch.innovazion.polymerge.Patcher;
import ch.innovazion.polymerge.utils.IOConsumer;
import ch.innovazion.polymerge.utils.LineStream;

public class HotPatcher extends Patcher implements Observer {
		
	private final PatchCache cache;
	
	protected final FileSystemHandler coreHandler;
	protected final FileSystemHandler patchesHandler;
	protected final FileSystemHandler dynamicHandler;
	
	public HotPatcher(String target, Path core, Path patches, Path output) {
		super(target, core, patches, output);
						
		try {
			this.cache = new PatchCache(IOConsumer.of(this::patch));
			
			this.coreHandler = new CoreFSHandler(core, output, cache);
			this.patchesHandler = createPatchesFSHandler(patches);
			this.dynamicHandler = new DynamicFSHandler(Paths.get("").toAbsolutePath(), this, getLinker());
		} catch(IOException e) {
			System.err.println("[" + target + "] Unable to create a watch service using the default filesystem");
			System.exit(666);
			throw new RuntimeException();
		}
		
		getLinker().addObserver(this);
	}
	
	protected FileSystemHandler createPatchesFSHandler(Path patches) throws IOException {
		return new PatchesFSHandler(patches, this);
	}
	
	public void patch() throws IOException {
		System.out.println("[" + getTargetName() + "] Applying a full patch...");

		super.patch();
		
		System.out.println("[" + getTargetName() + "] Starting watch service...");

		registerHandlers();
		
		while(true) {
			try {
				coreHandler.processQueue();
				patchesHandler.processQueue();
				dynamicHandler.processQueue();
												
				Thread.sleep(200);
			} catch (InterruptedException e) {
				;
			}
		}
	}
	
	protected void registerHandlers() {
		coreHandler.registerRecursive(getCore());
		patchesHandler.registerRecursive(getPatches());
	}
	
	/*
	 * Patches a given target again using a patch file.
	 * 
	 * Involves:
	 * 	- The destruction and copying of the core file before patching
	 * 	- Cache invalidation
	 * 	- Actual patching of the target
	 */
	public void hotPatch(Path path) throws IOException {		
		cache.invalidate(path).forEach(IOConsumer.of(location -> {			
			Files.copy(getCore().resolve(location), getOutput().resolve(location), StandardCopyOption.REPLACE_EXISTING);
			cache.patch(location);
		}));
				
		if(Files.exists(path)) {
			patch(path);
		}
	}
	
	/*
	 * Saves the [patch <-> location] entry in the cache.
	 */
	public Configuration patchLocation(Path path, LineStream stream) throws IOException {		
		Configuration config = super.patchLocation(path, stream);
		cache.addEntry(path, config.getLocation());
		return config;
	}

	/*
	 * When the patch linker imports a new dynamic resource, 
	 * this method is called and the path of the dynamic resource becomes handled by the watch service.
	 */
	public void update(Observable o, Object arg) {
		Path path = (Path) arg;
		dynamicHandler.register(path.getParent());
	}
}
