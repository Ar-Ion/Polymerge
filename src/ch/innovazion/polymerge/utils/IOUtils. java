package ch.innovazion.polymerge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;

public class IOUtils {
	public static void copyDirectory(Path source, Path target) {
	FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
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
	
	public static void deleteDirectory(Path path) {
	FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    Files.delete(file);
    return FileVisitResult.CONTINUE;      
    }
    
    public FileVisitResult postVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	Files.delete(dir);  
	return FileVisitResult.CONTINUE;      
    }
	};
	
	Files.walkFileTree(path, visitor);
	}
}