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
package ch.innovazion.polymerge;

import java.util.Optional;
import java.util.function.Supplier;

import ch.innovazion.polymerge.utils.LineStream;
import ch.innovazion.polymerge.utils.PatchUtils;

public class Configuration {
	
	private final String name;

	private boolean loaded = false;
	
	private String location;
	private Optional<String> target;
	private PatchMode patchMode;
	
	public Configuration(String name) {
		this.name = name;
	}
		
	public void read(LineStream stream) {		
		location = PatchUtils.find("@locate", stream).orElseThrow(err("Location not specified"));
		patchMode = PatchMode.valueOf(PatchUtils.find("@mode", stream).orElseThrow(err("Patch mode not specified")).toUpperCase());
		target = PatchUtils.find("@target", stream);

		loaded = true;
	}
	
	public String getLocation() {
		return ensure(location);
	}
	
	public Optional<String> getTarget() {
		return ensure(target);
	}
	
	public PatchMode getPatchMode() {
		return ensure(patchMode);
	}
	
	private <T> T ensure(T obj) {
		if(!loaded) {
			throw err("Configuration not yet loaded").get();
		} else {
			return obj;
		}
	}
	
	private Supplier<ConfigurationException> err(String message) {
		return () -> new ConfigurationException(message + " for " + name);
	}
	
	public String toString() {
		return "<Configuration>{ location: " + location + "; target: " + target + "; patch mode: " + patchMode + " }";
	}
}
