package ch.innovazion.polymerge.transforms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.innovazion.polymerge.utils.PatchUtils;

public class MergeTransform extends SourceTransform {
		
	public MergeTransform(Path root) {
		super(root);
	}

	/*
	 * Every time an entry from symbol map is found in the core code base, it gets replaced by its corresponding patch.
	 */
	public void apply(String identifier, LinkedList<String> patchData) throws IOException {
		Path target = resolveIdentifier(identifier);
		Map<String, String> symbols = scanSymbols(patchData);
		
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
	private Map<String, String> scanSymbols(LinkedList<String> patchData) throws TransformException {
		Map<String, String> symbols = new HashMap<>();
		
		while(true) {
			Optional<String> element = PatchUtils.find("@patch", patchData);
			
			if(element.isPresent()) {
				String[] instruction = element.get().split(" ");
				
				if(instruction.length > 1) {
					String key = "patch<" + instruction[0] + ">";
					String value = instruction[instruction.length - 1];
					
					if(value.trim().equals("§")) {
						List<String> paragraph = PatchUtils.readParagraph(patchData).orElseThrow(() -> new TransformException("Paragraph is not correctly delimited."));
						symbols.put(key, String.join(System.lineSeparator(), paragraph));
					} else {
						symbols.put(key, value);
					}
				} else {
					throw new TransformException("Invalid command. Syntax: @patch key value");
				}
			} else {
				break;
			}
		}
		
		return symbols;
	}
	
	private String patchLine(String input, Map<String, String> symbols) {		
		for(Entry<String, String> symbol : symbols.entrySet()) {
			input = input.replace(symbol.getKey(), symbol.getValue());
		}
		
		return input;
	}
}
