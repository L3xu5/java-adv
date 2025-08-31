package info.kgeorgiy.ja.petrasiuk.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.tools.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Implements interfaces or extends classes by generating their implementations and optionally creating JAR files.
 * This class provides functionality to create source code for a given type and compile it into a JAR file if needed.
 * It supports both interface implementation and class extension, handling abstract methods and constructors.
 */
public class Implementor implements JarImpler {
    /**
     * The indentation string used in generated code, consisting of four spaces.
     */
    private static final String TAB = "    ";

    /**
     * The newline character sequence specific to the current system.
     */
    private static final String NEWLINE = System.lineSeparator();

    /**
     * Constructs a new instance of {@code Implementor}.
     * This default constructor initializes an object capable of generating implementations for classes and interfaces.
     */
    public Implementor() {
    }

    /**
     * Updates the method map with a given method, preferring methods with more specific return types.
     *
     * @param methods the map of method signatures to {@link Method} objects to update
     * @param m the {@link Method} to add or compare
     */
    private static void updateMethodMap(Map<String, Method> methods, Method m) {
        String signature = m.getName() + Arrays.toString(m.getParameterTypes());
        methods.compute(signature, (_, oldMethod) ->
                oldMethod == null || oldMethod.getReturnType().isAssignableFrom(m.getReturnType()) ? m : oldMethod);
    }

    /**
     * Recursively collects all non-private, non-public methods from superclasses into a map.
     *
     * @param current the {@link Class} to process
     * @param methods the map of method signatures to {@link Method} objects to populate
     */
    private static void getAllMethodsFromSuperclasses(Class<?> current, Map<String, Method> methods) {
        Arrays.stream(current.getDeclaredMethods())
                .filter(m -> !Modifier.isPrivate(m.getModifiers()) && !Modifier.isPublic(m.getModifiers()))
                .forEach(m -> updateMethodMap(methods, m));

        if (current.getSuperclass() != null) {
            getAllMethodsFromSuperclasses(current.getSuperclass(), methods);
        }
    }

    /**
     * Retrieves all methods that need to be implemented for a given class or interface.
     *
     * @param current the {@link Class} to analyze
     * @return a {@link List} of {@link Method} objects to implement
     */
    private static List<Method> getAllMethodsToImplement(Class<?> current) {
        Map<String, Method> methods = new HashMap<>();
        getAllMethodsFromSuperclasses(current, methods);
        Arrays.stream(current.getMethods())
                .forEach(m -> updateMethodMap(methods, m));
        return new ArrayList<>(methods.values());
    }

    /**
     * Converts modifiers to a string representation, excluding abstract and transient modifiers.
     *
     * @param modifiers the integer representing modifiers from {@link Modifier}
     * @return a string representation of the modifiers
     */
    private static String parseModifiers(int modifiers) {
        return Modifier.toString(modifiers & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT);
    }

    /**
     * Generates a default return value for a given type.
     *
     * @param clazz the {@link Class} representing the return type
     * @return a string with the default value (e.g., "false", "0", "null")
     */
    private static String parseDefaultValue(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) {
                return " false";
            } else if (clazz != void.class) {
                return " 0";
            }
        } else if (clazz == String.class) {
            return " \"\"";
        } else {
            return " null";
        }
        return "";
    }

    /**
     * Generates argument names for an executable (method or constructor).
     *
     * @param e the {@link Executable} to process
     * @return an array of argument names (e.g., "arg0", "arg1")
     */
    private static String[] parseArgumentsNames(Executable e) {
        return IntStream.range(0, e.getParameterCount()).mapToObj(i -> "arg" + i).toArray(String[]::new);
    }

    /**
     * Generates a string representation of method or constructor arguments.
     *
     * @param e the {@link Executable} to process
     * @return a string with argument declarations (e.g., "(int arg0, String arg1)")
     */
    private static String parseArguments(Executable e) {
        Class<?>[] argsTypes = e.getParameterTypes();
        String[] argsNames = parseArgumentsNames(e);
        return IntStream.range(0, argsTypes.length)
                .mapToObj(i -> String.format("%s %s", argsTypes[i].getCanonicalName(), argsNames[i]))
                .collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Generates a string representation of exceptions thrown by an executable.
     *
     * @param e the {@link Executable} to process
     * @return a string with exception declarations (e.g., "throws IOException") or empty string
     */
    private static String parseExceptions(Executable e) {
        Class<?>[] exceptions = e.getExceptionTypes();
        return exceptions.length > 0 ?
                String.format("throws %s", Arrays.stream(exceptions)
                        .map(Class::getCanonicalName)
                        .collect(Collectors.joining(", "))) : "";
    }

    /**
     * Generates a string representation of an abstract method implementation.
     *
     * @param m the {@link Method} to process
     * @return a string with the method implementation, or empty string if not applicable
     */
    private static String parseMethod(Method m) {
        int modifiers = m.getModifiers();
        if (Modifier.isPrivate(modifiers) || Modifier.isStatic(modifiers) || !Modifier.isAbstract(modifiers)) {
            return "";
        }
        return String.format("%s@Override%n" +
                        "%s%s %s %s%s %s {%n" +
                        "%s%sreturn%s;%n" +
                        "%s}%n%n",
                TAB,
                TAB, parseModifiers(modifiers), m.getReturnType().getCanonicalName(), m.getName(),
                parseArguments(m), parseExceptions(m),
                TAB, TAB, parseDefaultValue(m.getReturnType()),
                TAB);
    }

    /**
     * Generates a string representation of a constructor for the implemented class.
     *
     * @param c the {@link Constructor} to process
     * @return a string with the constructor implementation
     */
    private static String parseConstructor(Constructor<?> c) {
        String className = c.getDeclaringClass().getSimpleName() + "Impl";
        return String.format("%s%s%s%s %s {%n" +
                        "%s%ssuper(%s);%n" +
                        "%s}%n",
                TAB,
                Modifier.isPublic(c.getModifiers()) ? "public " : "",
                className, parseArguments(c), parseExceptions(c),
                TAB, TAB, String.join(", ", parseArgumentsNames(c)),
                TAB);
    }

    /**
     * Generates constructor implementations for a class.
     *
     * @param clazz the {@link Class} to process
     * @return a string with constructor implementations
     * @throws ImplerException if no accessible constructors are found
     */
    private static String parseConstructors(Class<?> clazz) throws ImplerException {
        String result = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(c -> !Modifier.isPrivate(c.getModifiers()))
                .map(Implementor::parseConstructor)
                .collect(Collectors.joining());

        if (result.isEmpty()) {
            throw new ImplerException("No constructor found for " + clazz.getCanonicalName());
        }
        return result;
    }

    /**
     * Implements a given class or interface by generating its source code.
     *
     * @param token the {@link Class} to implement (interface or non-final class)
     * @param root the {@link Path} where the generated source file will be written
     * @throws ImplerException if implementation fails due to invalid input or I/O errors
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        int modifiers = token.getModifiers();
        if (token == Enum.class || token == Record.class || Modifier.isFinal(modifiers)) {
            throw new ImplerException(token.getName() + " is not an interface or not final class");
        }

        if (Modifier.isPrivate(modifiers)) {
            throw new ImplerException("Cannot implement private interface/class: " + token.getName());
        }

        String generated = String.format("package %s;%s%s%sclass %sImpl %s%s {%s%s%s}%s",
                token.getPackageName(), NEWLINE, NEWLINE,
                Modifier.isPublic(modifiers) ? "public " : "",
                token.getSimpleName(),
                token.isInterface() ? "implements " : "extends ", token.getCanonicalName(), NEWLINE,
                !token.isInterface() ? parseConstructors(token) : "",
                getAllMethodsToImplement(token).stream()
                        .map(Implementor::parseMethod)
                        .collect(Collectors.joining()),
                NEWLINE);

        try {
            root = root.resolve(token.getPackageName().replace(".", File.separator));
            Files.createDirectories(root);
            Files.writeString(root.resolve(token.getSimpleName() + "Impl.java"), toUnicode(generated));
        } catch (IOException e) {
            throw new ImplerException("IOException while writing file", e);
        }
    }

    /**
     * Converts a string to its Unicode escape sequence representation.
     *
     * @param input the input string to convert
     * @return a string with Unicode escape sequences for non-ASCII characters
     */
    public static String toUnicode(String input) {
        return input.chars()
                .mapToObj(ch -> ch > 127 ? String.format("\\u%04X", ch) : String.valueOf((char) ch))
                .collect(Collectors.joining());
    }

    /**
     * Implements a given class or interface and packages the result into a JAR file.
     *
     * @param token the {@link Class} to implement
     * @param jarFile the {@link Path} where the JAR file will be created
     * @throws ImplerException if implementation, compilation, or JAR creation fails
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(Path.of("."), "__implementor");
        } catch (IOException e) {
            throw new ImplerException("Failed to create temporary directory", e);
        }
        try {
            implement(token, tempDir);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new ImplerException("No Java compiler available");
            }

            Path sourceFile = tempDir.resolve(token.getPackageName().replace(".", File.separator))
                    .resolve(token.getSimpleName() + "Impl.java");
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
                String classPath;
                try {
                    URL location = token.getProtectionDomain().getCodeSource().getLocation();
                    classPath = location != null ? new File(location.toURI()).getAbsolutePath() : System.getProperty("java.class.path");
                } catch (URISyntaxException e) {
                    classPath = System.getProperty("java.class.path");
                }
                List<String> options = List.of("-cp", classPath, "-d", tempDir.toString(), "-encoding", "utf-8");
                boolean success = compiler.getTask(null, fileManager, null, options, null,
                        fileManager.getJavaFileObjects(sourceFile)).call();
                if (!success) {
                    throw new ImplerException("Compilation failed");
                }
            }
            try (JarOutputStream jarOut = new JarOutputStream(Files.newOutputStream(jarFile))) {
                String classFilePath = token.getPackageName().replace(".", "/") + "/" + token.getSimpleName() + "Impl.class";
                Path compiledClass = tempDir.resolve(classFilePath);
                jarOut.putNextEntry(new JarEntry(classFilePath));
                Files.copy(compiledClass, jarOut);
                jarOut.closeEntry();
            }
        } catch (IOException e) {
            throw new ImplerException("Error during JAR creation", e);
        } finally {
            try (Stream<Path> walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ignored) {
                            }
                        });
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Main entry point for running the implementor from the command line.
     *
     * @param args command-line arguments: either one class name or "-jar className output.jar"
     */
    public static void main(String[] args) {
        if (args == null || (args.length != 1 && args.length != 3)) {
            System.err.println("Invalid number of arguments. Expected one argument for class/interface name or three arguments for '-jar' option.");
            return;
        }

        Implementor implementor = new Implementor();
        try {
            if (args.length == 3 && "-jar".equals(args[0])) {
                Class<?> token = Class.forName(args[1]);
                Path jarFile = Path.of(args[2]);
                implementor.implementJar(token, jarFile);
            } else if (args.length == 1) {
                Class<?> token = Class.forName(args[0]);
                implementor.implement(token, Path.of(""));
            } else {
                System.err.println("Invalid format of arguments. Expected one argument with java class or three " +
                        "with -jar java-class output.jar");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + (args.length == 1 ? args[0] : args[1]));
        } catch (ImplerException e) {
            System.err.println("Implementation error: " + e.getMessage());
        }
    }
}