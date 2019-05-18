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
package ch.innovazion.polymerge.reactive.monolithic;

import java.io.IOException;
import java.nio.file.Path;

import ch.innovazion.polymerge.reactive.FileSystemHandler;
import ch.innovazion.polymerge.reactive.HotPatcher;

public class MonolithicHotPatcher extends HotPatcher {

	public MonolithicHotPatcher(String target, Path core, Path patches, Path output) {
		super(target, core, patches, output);
	}
	
	protected FileSystemHandler createPatchesFSHandler(Path patches) throws IOException {
		return new MonolithicPatchFSHandler(patches, this);
	}
	
	protected void registerHandlers() {
		coreHandler.registerRecursive(getCore());
		patchesHandler.register(getPatches().getParent());
	}

}
