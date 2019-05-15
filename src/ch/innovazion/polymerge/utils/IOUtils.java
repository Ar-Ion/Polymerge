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
		    		System.err.println("Failed to delete directory '" + dir + "'");
		    	} else {
					Files.delete(dir);	
		    	}
		    					
				return FileVisitResult.CONTINUE;      
		    }
		};
		
		Files.walkFileTree(path, visitor);
	}
}