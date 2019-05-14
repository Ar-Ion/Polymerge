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

public class Polymerge {
	
	private final Path sources;
	private final Path patched;
	private final Path core;
	
	private final String target;
	
	public Polymerge(Path sources, Path patched, String target) {
		this.sources = sources;
		this.patched = patched;
		this.core = sources.resolve("core");
		
		this.target = target;
	}
	
	public void start() {
		try {
			Files.createDirectories(sources);
			Files.createDirectories(patched);
			Files.createDirectories(core);
		} catch (IOException e) {
			System.err.println("Failed to create root directories.");
			return;
		}
			
		System.out.println("Starting HotPatcher for target '" + target + "'...");
		
		String[] splitted = target.split("\\.");
		String main = target;
		
		if(splitted.length > 0) {
			main = splitted[0];
		}
		
		Path patches = sources.resolve(main);
		Path output = patched.resolve(target);
		
		if(Files.exists(patches) && Files.isDirectory(patches)) {
			Patcher patcher = new HotPatcher(target, core, patches, output);
			
			try {
				patcher.patch();
				System.out.println("Patcher terminated normally.");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Patcher terminated with failure.");
			}
			
		} else {
			System.err.println("Nothing to be done.");
		}
	}
}
