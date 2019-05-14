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
