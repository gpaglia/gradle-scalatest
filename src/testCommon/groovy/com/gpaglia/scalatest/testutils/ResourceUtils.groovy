package com.gpaglia.scalatest.testutils

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile

class ResourceUtils {
    public static void copyResources(String srcDir, Path dest)
            throws URISyntaxException, UnsupportedEncodingException, UnsupportedOperationException, IOException {

        URL url = Thread.currentThread().getContextClassLoader().getResource(srcDir)
        if (url != null) {
            if (url.getProtocol() == "file") {
                Path file = Paths.get(url.toURI())
                if (file != null) {
                    Files.walk(file).withCloseable {
                        paths ->
                            paths
                                    .filter { Files.isRegularFile(it) }
                                    .each {
                                        Path relative = file.relativize(it)
                                        Path parent = relative.getParent()
                                        if (parent != null) {
                                            Files.createDirectories(dest.resolve(parent))
                                        }
                                        if (Files.isRegularFile(it) || parent != null) {
                                            Files.copy(it, dest.resolve(relative))
                                        }
                                    }
                    }
                }
            } else if (url.getProtocol() == "jar") {

                String dirname = srcDir + "/"
                String path = url.getPath()
                String jarPath = path.substring(5, path.indexOf("!"))
                JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))
                jarFile.withCloseable {jar ->
                    Enumeration<JarEntry> entries = jar.entries()
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement()
                        String name = entry.getName()
                        if (name.startsWith(dirname) && dirname != name) {
                            String relativeName = name.substring(dirname.length())
                            if (relativeName.endsWith('/')) {
                                // a directory - strip ending '/'
                                relativeName = relativeName.substring(0, relativeName.length() - 1)
                                Files.createDirectories(dest.resolve(relativeName))
                            } else {
                                Path dstPath = Paths.get(relativeName)
                                Thread.currentThread().getContextClassLoader().getResource(name).withInputStream {
                                    if (dstPath.getParent() != null) {
                                        Path dir = dest.resolve(dstPath.getParent())
                                        if (Files.exists(dir) && ! Files.isDirectory(dir)) {
                                            throw new IllegalStateException("jar ${jarPath} contains a file ${dir.toString()} but a directory was expected")
                                        } else if (! Files.exists(dir)) {
                                            Files.createDirectories(dest.resolve(dir))
                                        }
                                    }
                                    Files.copy(it, dest.resolve(dstPath))

                                }

                            }
                        }
                    }
                }
            } else {
                throw new UnsupportedOperationException("Unhandled protocol ${url.getProtocol()}")
            }
        }
    }
}
