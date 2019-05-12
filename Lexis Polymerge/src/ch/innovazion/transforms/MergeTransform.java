package ch.innovazion.transforms;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.innovazion.polymerge.Utils;

public class MergeTransform extends SourceTransform {
		
	public MergeTransform(File root) {
		super(root);
	}

	public void apply(String identifier, LinkedList<String> patchData) throws IOException {
		File target = resolveIdentifier(identifier);
		Map<String, String> symbols = scanSymbols(patchData);
		
		List<String> lines = Files.readAllLines(target.toPath());
		List<String> patched = lines.stream().map(line -> patchLine(line, symbols)).collect(Collectors.toList());
	
		Files.write(target.toPath(), patched, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	private Map<String, String> scanSymbols(LinkedList<String> patchData) throws TransformException {
		Map<String, String> symbols = new HashMap<>();
		
		while(true) {
			Optional<String> element = Utils.find("@patch", patchData);
			
			if(element.isPresent()) {
				String[] instruction = element.get().split(" ");
				
				if(instruction.length > 1) {
					String key = "@patchme<" + instruction[0] + ">";
					String value = instruction[instruction.length - 1];
					
					if(value.trim().equals("§")) {
						List<String> paragraph = Utils.readParagraph(patchData).orElseThrow(() -> new TransformException("Paragraph is not correctly delimited."));
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
