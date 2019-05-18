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
package ch.innovazion.polymerge.transforms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import ch.innovazion.polymerge.utils.LineStream;
import ch.innovazion.polymerge.utils.PatchUtils;

public class AssetTransform extends SourceTransform {
	public AssetTransform(Path root) {
		super(root);
	}

	public void apply(String identifier, LineStream stream) throws IOException {
		Path output = resolveIdentifier(identifier);
		Path input = Paths.get(PatchUtils.find("@raw", stream).orElseThrow(this::importRequired));
		
		if(Files.exists(input)) {
			Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
		} else {
			System.err.println("Unable to import asset '" + input.toString() + "' while patching '" + identifier + "'");
		}
	}
	
	private IOException importRequired() {
		return new IOException("Asset mode requires an @raw instruction");
	}
}
