package ch.innovazion.polymerge.utils;

import java.util.ArrayList;
import java.util.Queue;
import java.util.List;
import java.util.Optional;

public class Utils {
	/*
	 * Finds the first element in a stream which starts with a given string.
	 * Returns the corresponding line (if any), trimmed, and without the given string at the beginning.
	 * If a line is found, it is removed from the queue.
	 */
	public static Optional<String> find(String start, Queue<String> lines) {
		String output = null;
		
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			
			if(line.startsWith(start)) {
				output = line.substring(start.length()).trim();
				
				for(int j = 0; j < i + 1; j++) {
					lines.remove();
				}
				
				break;
			}
		}

		return Optional.ofNullable(output);
	}
	
	/*
	 * Reads a paragraph in a stream.
	 * A paragraph is delimited by @begin and @end at the beginning of a line.
	 * If the paragraph is correctly delimited, it gets deleted from the queue.
	 */
	public static Optional<List<String>> readParagraph(Queue<String> lines) {
		find("@begin", lines);
		
		List<String> paragraph = new ArrayList<>();
		
		for(int i = 0; i < lines.size(); i++) {
			String element = lines.get(i).trim();
			
			if(element.startsWith("@end")) {
				for(int j = 0; j < i + 1; j++) {
					lines.remove();
				}
				
				return Optional.of(paragraph);
			} else {
				paragraph.add(element);
			}
		}
		
		return Optional.empty();
	}
}
