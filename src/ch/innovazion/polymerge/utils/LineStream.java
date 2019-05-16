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

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class LineStream implements Iterator<String>, Iterable<String> {

	private final Stack<Integer> marks = new Stack<>();
	private final List<String> lines;
	
	private int position = 0;
	private int limit;
	
	public LineStream(List<String> lines) {
		this.lines = lines;
		this.limit = lines.size();
	}
	
	public boolean hasNext() {
		return position < limit;
	}
	
	public String next() {
		return lines.get(position++);
	}
	
	public void mark() {
		marks.push(position);
	}
	
	public void discardMark() {
		marks.pop();
	}
	
	public void reset() {
		position = marks.pop();
	}
	
	public int length() {
		return lines.size();
	}
	
	public void limit(int size) {
		if(limit >= 0 && limit <= lines.size()) {
			limit = size;
		} else {
			throw new IllegalArgumentException("Limit cannot exceed the total length of the stream");
		}
	}
	
	public int getPosition() {
		return position;
	}
	
	public int getLineNumber() {
		return position + 1;
	}

	public Iterator<String> iterator() {
		return this;
	}
}