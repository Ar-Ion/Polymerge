package ch.innovazion.polymerge.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Utils {
	/*
	 * Finds the first element in a stream which starts with a given string.
	 * Returns the corresponding line (if any), trimmed, and without the given string at the beginning.
	 * If a line is found, it is removed from the stream.
	 */
	public static Optional<String> find(String start, LinkedList<String> list) {
		String output = null;
		
		for(int i = 0; i < list.size(); i++) {
			String line = list.get(i);
			
			if(line.startsWith(start)) {
				output = line.substring(start.length()).trim();
				
				for(int j = 0; j < i + 1; j++) {
					list.removeFirst();
				}
				
				break;
			}
		}

		return Optional.ofNullable(output);
	}
	
	/*
	 * Reads a paragraph in a stream.
	 * A paragraph is delimited by @begin and @end at the beginning of a line.
	 * If the paragraph is correctly delimited, it gets deleted from the stream.
	 */
	public static Optional<List<String>> readParagraph(LinkedList<String> list) {
		find("@begin", list);
		
		List<String> paragraph = new ArrayList<>();
		
		for(int i = 0; i < list.size(); i++) {
			String element = list.get(i).trim();
			
			if(element.startsWith("@end")) {
				for(int j = 0; j < i + 1; j++) {
					list.removeFirst();
				}
				
				return Optional.of(paragraph);
			} else {
				paragraph.add(element);
			}
		}
		
		return Optional.empty();
	}
}
