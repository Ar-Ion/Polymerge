package ch.innovazion.polymerge.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class IOUtils {
	public static void copyDirectory(Path source, Path target) throws IOException {
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(target.resolve(source.relativize(dir)));  
				return FileVisitResult.CONTINUE;      
			}
    
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)));
				return FileVisitResult.CONTINUE;      
			}
		};
	
		Files.walkFileTree(source, visitor);
	}
	
	public static void deleteDirectory(Path path) throws IOException{
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			    Files.delete(file);
			    return FileVisitResult.CONTINUE;      
		    }
	    
		    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		    	if(exc != null) {
		    		throw exc;
		    	}
		    	
				Files.delete(dir);
				
				return FileVisitResult.CONTINUE;      
		    }
		};
		
		Files.walkFileTree(path, visitor);
	}
}