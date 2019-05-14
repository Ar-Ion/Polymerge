package ch.innovazion.polymerge.utils;

import java.io.IOException;
import java.util.function.Consumer;

@FunctionalInterface
public interface IOConsumer<T> {
	
	public void accept(T object) throws IOException;
	
	public static <T> Consumer<T> of(IOConsumer<T> consumer) {
		return (t) -> {
			try {
				consumer.accept(t);
			} catch(IOException e) {
				e.printStackTrace();
			}
		};
	}
}
