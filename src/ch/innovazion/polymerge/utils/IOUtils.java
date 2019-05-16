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

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class IOUtils {
	public static void copyDirectory(Path source, Path target, List<PathMatcher> exclude) throws IOException {
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			    if(exclude.stream().anyMatch(ex -> ex.matches(source.relativize(dir)))) {
			    	return FileVisitResult.SKIP_SUBTREE;
			    }
			    
				Files.createDirectories(target.resolve(source.relativize(dir)));  
				
				return FileVisitResult.CONTINUE;      
			}
    
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path relative = source.relativize(file);
				Path destination = target.resolve(relative);
				
				if(!Files.exists(destination)) {
				    if(exclude.stream().noneMatch(ex -> ex.matches(relative))) {
				    	Files.copy(file, destination);
				    }
				}
				
				return FileVisitResult.CONTINUE;      
			}
		};
	
		Files.walkFileTree(source, visitor);
	}
	
	public static void deleteDirectory(Path path, List<PathMatcher> exclude) throws IOException{
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) throws IOException {
			    if(exclude.stream().anyMatch(ex -> ex.matches(path.relativize(file)))) {
			    	return FileVisitResult.SKIP_SUBTREE;
			    }
			    
			    return FileVisitResult.CONTINUE;
			}
			
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			    if(exclude.stream().noneMatch(ex -> ex.matches(path.relativize(file)))) {
			    	Files.delete(file);
			    }
			    
			    return FileVisitResult.CONTINUE;      
		    }
	    
		    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		    	if(exc != null) {
		    		System.err.println("Failed to delete directory '" + dir + "'");
		    	} else {
				    if(exclude.stream().noneMatch(ex -> ex.matches(path.relativize(dir)))) {
		    			try {
		    				Files.delete(path);
		    			} catch(DirectoryNotEmptyException e) {
		    				;
		    			}
				    }
		    	}
		    					
				return FileVisitResult.CONTINUE;      
		    }
		};
		
		Files.walkFileTree(path, visitor);
	}
}