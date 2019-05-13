package ch.innovazion.polymerge;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;

import ch.innovazion.polymerge.utils.Utils;

public class Configuration {
	
	private final String name;
	
	private boolean loaded = false;
	
	private String location;
	private Optional<String> target;
	private PatchMode patchMode;
	
	public Configuration(String name) {
		this.name = name;
	}
		
	public void read(LinkedList<String> stream) {
		location = Utils.find("@locate", stream).orElseThrow(err("Location not specified"));
		patchMode = PatchMode.valueOf(Utils.find("@mode", stream).orElseThrow(err("Patch mode not specified")));
		target = Utils.find("@target", stream);

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
}
