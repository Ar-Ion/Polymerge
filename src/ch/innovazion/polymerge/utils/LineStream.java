package ch.innovazion.polymerge.utils;

import java.util.Iterator;
import java.util.List;

public class LineStream implements Iterator<String>, Iterable<String> {

	private final List<String> lines;
	
	private int position = 0;
	private int mark = 0;
	
	public LineStream(List<String> lines) {
		this.lines = lines;
	}
	
	public boolean hasNext() {
		return position < lines.size();
	}
	
	public String next() {
		return lines.get(position++);
	}
	
	public void mark() {
		mark = position;
	}
	
	public void reset() {
		position = mark;
	}
	
	public int getLineNumber() {
		return position + 1;
	}

	public Iterator<String> iterator() {
		return this;
	}
}