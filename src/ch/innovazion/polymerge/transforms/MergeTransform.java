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
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.innovazion.polymerge.utils.LineStream;
import ch.innovazion.polymerge.utils.PatchUtils;

public class MergeTransform extends SourceTransform {
		
	public MergeTransform(Path root) {
		super(root);
	}

	/*
	 * Every time an entry from symbol map is found in the core code base, it gets replaced by its corresponding patch.
	 */
	public void apply(String identifier, LineStream stream) throws IOException {
		Path target = resolveIdentifier(identifier);
		Map<String, String> symbols = scanSymbols(stream);
		
		List<String> lines = Files.readAllLines(target);
		List<String> patched = lines.stream().map(line -> patchLine(line, symbols)).collect(Collectors.toList());
	
		Files.write(target, patched, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	/*
	 * Scans the patch file for required replacements and stores them in a hash map after parsing.
	 * 
	 * If an instruction such as "@patch key value" is found, a replacement "patch<key>" -> "value" is created.
	 * If an instruction such as "@patch key §" is found, the "Utils" class will attempt to read a paragraph and use it as a replacement for "patch<key>".
	 * 
	 * A paragraph must always be enclosed by "@begin" and "@end" and for each of those, an entire line must be dedicated.
	 */
	private Map<String, String> scanSymbols(LineStream stream) throws TransformException {
		Map<String, String> symbols = new HashMap<>();
		
		while(true) {
			Optional<String> element = PatchUtils.find("@patch", stream);
			
			if(element.isPresent()) {
				String[] instruction = element.get().split(" --- ");
				
				if(instruction.length > 1) {
					String key = "patch<" + instruction[0] + ">";
					
					String prepend = new String();
					String value = new String();
					String append = new String();
					
					int length = instruction.length;
					
					if(length > 3) {
						prepend = instruction[1];
						value = String.join(" --- ", Arrays.copyOfRange(instruction, 2, length - 1));
						append = instruction[length - 1];
					} else if(length == 3) {
						prepend = instruction[1];
						value = instruction[2];
					} else if(length == 2) {
						value = instruction[1];
					}
					
					if(value.trim().equals("§")) {
						List<String> paragraph = PatchUtils.readParagraph(stream).orElseThrow(() -> new TransformException("Paragraph is not correctly delimited."));
						
						paragraph = paragraph.stream().map(prepend::concat).collect(Collectors.toList());

						symbols.put(key, String.join(append + System.lineSeparator(), paragraph));
					} else {
						symbols.put(key, value);
					}
				} else {
					throw new TransformException("Invalid command at line " + stream.getLineNumber() + ". Syntax: @patch key ---< prepend ---> value <--- append >");
				}
			} else {
				break;
			}
		}
		
		return symbols;
	}
	
	private String patchLine(String line, Map<String, String> symbols) {		
		for(Entry<String, String> symbol : symbols.entrySet()) {
			line = line.replace(symbol.getKey(), symbol.getValue());
		}
		
		return line;
	}
}
