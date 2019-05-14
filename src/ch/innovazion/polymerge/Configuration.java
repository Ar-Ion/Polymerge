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
}
