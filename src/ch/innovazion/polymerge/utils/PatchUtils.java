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
package ch.innovazion.polymerge.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatchUtils {
	/*
	 * Finds the first element in a stream which starts with a given string.
	 * Returns the corresponding line (if any), trimmed, and without the given string at the beginning.
	 * If a line is found, it is removed from the stream.
	 */
	public static Optional<String> find(String start, LineStream stream) {		
		stream.mark();
		
		for(String line : stream) {
			String trim = line.trim();
			
			if(trim.startsWith(start)) {
				return Optional.of(trim.substring(start.length()).trim());
			}
		}

		stream.reset();
		
		return Optional.empty();
	}
	
	/*
	 * Reads a paragraph in a stream.
	 * A paragraph is delimited by @begin and @end at the beginning of a line.
	 * If the paragraph is correctly delimited, it gets deleted from the stream.
	 */
	public static Optional<List<String>> readParagraph(LineStream stream) {
		if(find("@begin", stream).isPresent()) {
			
			List<String> paragraph = new ArrayList<>();
			
			stream.mark();
			
			for(String line : stream) {
				String trim = line.trim();

				if(trim.startsWith("@end")) {
					return Optional.of(paragraph);
				} else {
					paragraph.add(line);
				}
			}
			
			stream.reset();
		}
		
		return Optional.empty();
	}
}
