package lemon.elastic.query4j.util;

import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.common.lang3.StringUtils;

import com.google.common.base.Preconditions;

/**
 * Miscellaneous class utility methods. Mainly for internal use within the
 * framework; consider
 * <a href="http://commons.apache.org/lang/" target="_blank">Apache Commons Lang</a>
 * for a more comprehensive suite of class utilities.
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 1.1
 * @see ReflectionUtils
 */
public abstract class ClassUtils {

    /** Suffix for array class names: "[]" */
    public static final String ARRAY_SUFFIX = "[]";

    /** Prefix for internal array class names: "[" */
    private static final String INTERNAL_ARRAY_PREFIX = "[";

    /** Prefix for internal non-primitive array class names: "[L" */
    private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

    /** The package separator character '.' */
    private static final char PACKAGE_SEPARATOR = '.';

    /** The inner class separator character '$' */
    private static final char INNER_CLASS_SEPARATOR = '$';

    /** The CGLIB class separator character "$$" */
    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    /** The ".class" file suffix */
    public static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * Map with primitive wrapper type as key and corresponding primitive
     * type as value, for example: Integer.class -> int.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(8);

    /**
     * Map with primitive type as key and corresponding wrapper
     * type as value, for example: int.class -> Integer.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new HashMap<Class<?>, Class<?>>(8);

    /**
     * Map with primitive type name as key and corresponding primitive
     * type as value, for example: "int" -> "int.class".
     */
    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(32);

    /**
     * Map with common "java.lang" class name as key and corresponding Class as value.
     * Primarily for efficient deserialization of remote invocations.
     */
    private static final Map<String, Class<?>> commonClassCache = new HashMap<String, Class<?>>(32);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);

        for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
            primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
            registerCommonClasses(entry.getKey());
        }

        Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(32);
        primitiveTypes.addAll(primitiveWrapperTypeMap.values());
        primitiveTypes.addAll(Arrays.asList(boolean[].class, byte[].class, char[].class, double[].class, float[].class, int[].class, long[].class, short[].class));
        primitiveTypes.add(void.class);
        for (Class<?> primitiveType : primitiveTypes) {
            primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
        }

        registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class, Float[].class, Integer[].class, Long[].class, Short[].class);
        registerCommonClasses(Number.class, Number[].class, String.class, String[].class, Object.class, Object[].class, Class.class, Class[].class);
        registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class, Error.class, StackTraceElement.class, StackTraceElement[].class);
    }

    /**
     * Register the given common classes with the ClassUtils cache.
     */
    private static void registerCommonClasses(Class<?>... commonClasses) {
        for (Class<?> clazz : commonClasses) {
            commonClassCache.put(clazz.getName(), clazz);
        }
    }

    /**
     * Return the default ClassLoader to use: typically the thread context
     * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
     * class will be used as fallback.
     * <p>Call this method if you intend to use the thread context ClassLoader
     * in a scenario where you absolutely need a non-null ClassLoader reference:
     * for example, for class path resource loading (but not necessarily for
     * <code>Class.forName</code>, which accepts a <code>null</code> ClassLoader
     * reference as well).
     * @return the default ClassLoader (never <code>null</code>)
     * @see java.lang.Thread#getContextClassLoader()
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
        }
        return cl;
    }

    /**
     * Override the thread context ClassLoader with the environment's bean ClassLoader
     * if necessary, i.e. if the bean ClassLoader is not equivalent to the thread
     * context ClassLoader already.
     * @param classLoaderToUse the actual ClassLoader to use for the thread context
     * @return the original thread context ClassLoader, or <code>null</code> if not overridden
     */
    public static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse) {
        Thread currentThread = Thread.currentThread();
        ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
        if (classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)) {
            currentThread.setContextClassLoader(classLoaderToUse);
            return threadContextClassLoader;
        } else {
            return null;
        }
    }

    /**
     * Replacement for <code>Class.forName()</code> that also returns Class instances
     * for primitives (like "int") and array class names (like "String[]").
     * <p>Always uses the default class loader: that is, preferably the thread context
     * class loader, or the ClassLoader that loaded the ClassUtils class as fallback.
     * @param name the name of the Class
     * @return Class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     * @see #getDefaultClassLoader()
     * @deprecated as of Spring 3.0, in favor of specifying a ClassLoader explicitly:
     * see {@link #forName(String, ClassLoader)}
     */
    @Deprecated
    public static Class<?> forName(String name) throws ClassNotFoundException, LinkageError {
        return forName(name, getDefaultClassLoader());
    }

    /**
     * Replacement for <code>Class.forName()</code> that also returns Class instances
     * for primitives (e.g."int") and array class names (e.g. "String[]").
     * Furthermore, it is also capable of resolving inner class names in Java source
     * style (e.g. "java.lang.Thread.State" instead of "java.lang.Thread$State").
     * @param name the name of the Class
     * @param classLoader the class loader to use
     * (may be <code>null</code>, which indicates the default class loader)
     * @return Class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {
        Preconditions.checkNotNull(name, "Name must not be null");
        Preconditions.checkNotNull(name, "Name must not be null");

        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz == null) {
            clazz = commonClassCache.get(name);
        }
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
            String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
            String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = getDefaultClassLoader();
        }
        try {
            return classLoaderToUse.loadClass(name);
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String innerClassName = name.substring(0, lastDotIndex) + '$' + name.substring(lastDotIndex + 1);
                try {
                    return classLoaderToUse.loadClass(innerClassName);
                } catch (ClassNotFoundException ex2) {
                    // swallow - let original exception get through
                }
            }
            throw ex;
        }
    }

    /**
     * Resolve the given class name into a Class instance. Supports
     * primitives (like "int") and array class names (like "String[]").
     * <p>This is effectively equivalent to the <code>forName</code>
     * method with the same arguments, with the only difference being
     * the exceptions thrown in case of class loading failure.
     * @param className the name of the Class
     * @param classLoader the class loader to use
     * (may be <code>null</code>, which indicates the default class loader)
     * @return Class instance for the supplied name
     * @throws IllegalArgumentException if the class name was not resolvable
     * (that is, the class could not be found or the class file could not be loaded)
     * @see #forName(String, ClassLoader)
     */
    public static Class<?> resolveClassName(String className, ClassLoader classLoader) throws IllegalArgumentException {
        try {
            return forName(className, classLoader);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Cannot find class [" + className + "]", ex);
        } catch (LinkageError ex) {
            throw new IllegalArgumentException("Error loading class [" + className + "]: problem with class file or dependent class.", ex);
        }
    }

    /**
     * Resolve the given class name as primitive class, if appropriate,
     * according to the JVM's naming rules for primitive classes.
     * <p>Also supports the JVM's internal class names for primitive arrays.
     * Does <i>not</i> support the "[]" suffix notation for primitive arrays;
     * this is only supported by {@link #forName(String, ClassLoader)}.
     * @param name the name of the potentially primitive class
     * @return the primitive class, or <code>null</code> if the name does not denote
     * a primitive class or primitive array class
     */
    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = primitiveTypeNameMap.get(name);
        }
        return result;
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return <code>false</code> if either the class or
     * one of its dependencies is not present or cannot be loaded.
     * @param className the name of the class to check
     * @return whether the specified class is present
     * @deprecated as of Spring 2.5, in favor of {@link #isPresent(String, ClassLoader)}
     */
    @Deprecated
    public static boolean isPresent(String className) {
        return isPresent(className, getDefaultClassLoader());
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return <code>false</code> if either the class or
     * one of its dependencies is not present or cannot be loaded.
     * @param className the name of the class to check
     * @param classLoader the class loader to use
     * (may be <code>null</code>, which indicates the default class loader)
     * @return whether the specified class is present
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            forName(className, classLoader);
            return true;
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Return the user-defined class for the given instance: usually simply
     * the class of the given instance, but the original class in case of a
     * CGLIB-generated subclass.
     * @param instance the instance to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Object instance) {
        Preconditions.checkNotNull(instance, "Instance must not be null");
        return getUserClass(instance.getClass());
    }

    /**
     * Return the user-defined class for the given class: usually simply the given
     * class, but the original class in case of a CGLIB-generated subclass.
     * @param clazz the class to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                return superClass;
            }
        }
        return clazz;
    }

    /**
     * Check whether the given class is cache-safe in the given context,
     * i.e. whether it is loaded by the given ClassLoader or a parent of it.
     * @param clazz the class to analyze
     * @param classLoader the ClassLoader to potentially cache metadata in
     */
    public static boolean isCacheSafe(Class<?> clazz, ClassLoader classLoader) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        ClassLoader target = clazz.getClassLoader();
        if (target == null) {
            return false;
        }
        ClassLoader cur = classLoader;
        if (cur == target) {
            return true;
        }
        while (cur != null) {
            cur = cur.getParent();
            if (cur == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the class name without the qualified package name.
     * @param className the className to get the short name for
     * @return the class name of the class without the package name
     * @throws IllegalArgumentException if the className is empty
     */
    public static String getShortName(String className) {
        //        Assert.hasLength(className, "Class name must not be empty");
        Preconditions.checkState(StringUtils.isNotBlank(className), "Class name must not be empty");
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
        if (nameEndIndex == -1) {
            nameEndIndex = className.length();
        }
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
        return shortName;
    }

    /**
     * Get the class name without the qualified package name.
     * @param clazz the class to get the short name for
     * @return the class name of the class without the package name
     */
    public static String getShortName(Class<?> clazz) {
        return getShortName(getQualifiedName(clazz));
    }

    /**
     * Return the short string name of a Java class in uncapitalized JavaBeans
     * property format. Strips the outer class name in case of an inner class.
     * @param clazz the class
     * @return the short name rendered in a standard JavaBeans property format
     * @see java.beans.Introspector#decapitalize(String)
     */
    public static String getShortNameAsProperty(Class<?> clazz) {
        String shortName = ClassUtils.getShortName(clazz);
        int dotIndex = shortName.lastIndexOf('.');
        shortName = (dotIndex != -1 ? shortName.substring(dotIndex + 1) : shortName);
        return Introspector.decapitalize(shortName);
    }

    /**
     * Determine the name of the class file, relative to the containing
     * package: e.g. "String.class"
     * @param clazz the class
     * @return the file name of the ".class" file
     */
    public static String getClassFileName(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
    }

    /**
     * Determine the name of the package of the given class:
     * e.g. "java.lang" for the <code>java.lang.String</code> class.
     * @param clazz the class
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "");
    }

    /**
     * Return the qualified name of the given class: usually simply
     * the class name, but component type class name + "[]" for arrays.
     * @param clazz the class
     * @return the qualified name of the class
     */
    public static String getQualifiedName(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        if (clazz.isArray()) {
            return getQualifiedNameForArray(clazz);
        } else {
            return clazz.getName();
        }
    }

    /**
     * Build a nice qualified name for an array:
     * component type class name + "[]".
     * @param clazz the array class
     * @return a qualified name for the array class
     */
    private static String getQualifiedNameForArray(Class<?> clazz) {
        StringBuilder result = new StringBuilder();
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
            result.append(ClassUtils.ARRAY_SUFFIX);
        }
        result.insert(0, clazz.getName());
        return result.toString();
    }

    /**
     * Return the qualified name of the given method, consisting of
     * fully qualified interface/class name + "." + method name.
     * @param method the method
     * @return the qualified name of the method
     */
    public static String getQualifiedMethodName(Method method) {
        Preconditions.checkNotNull(method, "Method must not be null");
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    /**
     * Return a descriptive name for the given object's type: usually simply
     * the class name, but component type class name + "[]" for arrays,
     * and an appended list of implemented interfaces for JDK proxies.
     * @param value the value to introspect
     * @return the qualified name of the class
     */
    public static String getDescriptiveType(Object value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (Proxy.isProxyClass(clazz)) {
            StringBuilder result = new StringBuilder(clazz.getName());
            result.append(" implementing ");
            Class<?>[] ifcs = clazz.getInterfaces();
            for (int i = 0; i < ifcs.length; i++) {
                result.append(ifcs[i].getName());
                if (i < ifcs.length - 1) {
                    result.append(',');
                }
            }
            return result.toString();
        } else if (clazz.isArray()) {
            return getQualifiedNameForArray(clazz);
        } else {
            return clazz.getName();
        }
    }

    /**
     * Check whether the given class matches the user-specified type name.
     * @param clazz the class to check
     * @param typeName the type name to match
     */
    public static boolean matchesTypeName(Class<?> clazz, String typeName) {
        return (typeName != null && (typeName.equals(clazz.getName()) || typeName.equals(clazz.getSimpleName()) || (clazz.isArray() && typeName.equals(getQualifiedNameForArray(clazz)))));
    }

    /**
     * Determine whether the given class has a public constructor with the given signature.
     * <p>Essentially translates <code>NoSuchMethodException</code> to "false".
     * @param clazz	the clazz to analyze
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding constructor
     * @see java.lang.Class#getMethod
     */
    public static boolean hasConstructor(Class<?> clazz, Class<?>... paramTypes) {
        return (getConstructorIfAvailable(clazz, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a public constructor with the given signature,
     * and return it if available (else return <code>null</code>).
     * <p>Essentially translates <code>NoSuchMethodException</code> to <code>null</code>.
     * @param clazz	the clazz to analyze
     * @param paramTypes the parameter types of the method
     * @return the constructor, or <code>null</code> if not found
     * @see java.lang.Class#getConstructor
     */
    public static <T> Constructor<T> getConstructorIfAvailable(Class<T> clazz, Class<?>... paramTypes) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Determine whether the given class has a method with the given signature.
     * <p>Essentially translates <code>NoSuchMethodException</code> to "false".
     * @param clazz	the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding method
     * @see java.lang.Class#getMethod
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a method with the given signature,
     * and return it if available (else throws an <code>IllegalStateException</code>).
     * <p>Essentially translates <code>NoSuchMethodException</code> to <code>IllegalStateException</code>.
     * @param clazz	the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     * @return the method (never <code>null</code>)
     * @throws IllegalStateException if the method has not been found
     * @see java.lang.Class#getMethod
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        Preconditions.checkNotNull(methodName, "Method name must not be null");
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Expected method not found: " + ex);
        }
    }

    /**
     * Determine whether the given class has a method with the given signature,
     * and return it if available (else return <code>null</code>).
     * <p>Essentially translates <code>NoSuchMethodException</code> to <code>null</code>.
     * @param clazz	the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     * @return the method, or <code>null</code> if not found
     * @see java.lang.Class#getMethod
     */
    public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        Preconditions.checkNotNull(methodName, "Method name must not be null");
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Return the number of methods with a given name (with any argument types),
     * for the given class and/or its superclasses. Includes non-public methods.
     * @param clazz	the clazz to check
     * @param methodName the name of the method
     * @return the number of methods with the given name
     */
    public static int getMethodCountForName(Class<?> clazz, String methodName) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        Preconditions.checkNotNull(methodName, "Method name must not be null");
        int count = 0;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (methodName.equals(method.getName())) {
                count++;
            }
        }
        Class<?>[] ifcs = clazz.getInterfaces();
        for (Class<?> ifc : ifcs) {
            count += getMethodCountForName(ifc, methodName);
        }
        if (clazz.getSuperclass() != null) {
            count += getMethodCountForName(clazz.getSuperclass(), methodName);
        }
        return count;
    }

    /**
     * Does the given class or one of its superclasses at least have one or more
     * methods with the supplied name (with any argument types)?
     * Includes non-public methods.
     * @param clazz	the clazz to check
     * @param methodName the name of the method
     * @return whether there is at least one method with the given name
     */
    public static boolean hasAtLeastOneMethodWithName(Class<?> clazz, String methodName) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        Preconditions.checkNotNull(methodName, "Method name must not be null");
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        Class<?>[] ifcs = clazz.getInterfaces();
        for (Class<?> ifc : ifcs) {
            if (hasAtLeastOneMethodWithName(ifc, methodName)) {
                return true;
            }
        }
        return (clazz.getSuperclass() != null && hasAtLeastOneMethodWithName(clazz.getSuperclass(), methodName));
    }

    /**
     * Given a method, which may come from an interface, and a target class used
     * in the current reflective invocation, find the corresponding target method
     * if there is one. E.g. the method may be <code>IFoo.bar()</code> and the
     * target class may be <code>DefaultFoo</code>. In this case, the method may be
     * <code>DefaultFoo.bar()</code>. This enables attributes on that method to be found.
     * <p><b>NOTE:</b> In contrast to {@link org.springframework.aop.support.AopUtils#getMostSpecificMethod},
     * this method does <i>not</i> resolve Java 5 bridge methods automatically.
     * Call {@link org.springframework.core.BridgeMethodResolver#findBridgedMethod}
     * if bridge method resolution is desirable (e.g. for obtaining metadata from
     * the original method definition).
     * <p><b>NOTE:</b>Since Spring 3.1.1, if java security settings disallow reflective
     * access (e.g. calls to {@code Class#getDeclaredMethods} etc, this implementation
     * will fall back to returning the originally provided method.
     * @param method the method to be invoked, which may come from an interface
     * @param targetClass the target class for the current invocation.
     * May be <code>null</code> or may not even implement the method.
     * @return the specific target method, or the original method if the
     * <code>targetClass</code> doesn't implement it or is <code>null</code>
     */
    public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        Method specificMethod = null;
        if (method != null && isOverridable(method, targetClass) && targetClass != null && !targetClass.equals(method.getDeclaringClass())) {
            try {
                specificMethod = lemon.elastic.query4j.util.ReflectionUtils.findMethod(targetClass, method.getName(), method.getParameterTypes());
            } catch (AccessControlException ex) {
                // security settings are disallowing reflective access; leave
                // 'specificMethod' null and fall back to 'method' below
            }
        }
        return (specificMethod != null ? specificMethod : method);
    }

    /**
     * Determine whether the given method is overridable in the given target class.
     * @param method the method to check
     * @param targetClass the target class to check against
     */
    private static boolean isOverridable(Method method, Class targetClass) {
        if (Modifier.isPrivate(method.getModifiers())) {
            return false;
        }
        if (Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) {
            return true;
        }
        return getPackageName(method.getDeclaringClass()).equals(getPackageName(targetClass));
    }

    /**
     * Return a public static method of a class.
     * @param methodName the static method name
     * @param clazz	the class which defines the method
     * @param args the parameter types to the method
     * @return the static method, or <code>null</code> if no static method was found
     * @throws IllegalArgumentException if the method name is blank or the clazz is null
     */
    public static Method getStaticMethod(Class<?> clazz, String methodName, Class<?>... args) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        Preconditions.checkNotNull(methodName, "Method name must not be null");
        try {
            Method method = clazz.getMethod(methodName, args);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Check if the given class represents a primitive wrapper,
     * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper class
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        return primitiveWrapperTypeMap.containsKey(clazz);
    }

    /**
     * Check if the given class represents a primitive (i.e. boolean, byte,
     * char, short, int, long, float, or double) or a primitive wrapper
     * (i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double).
     * @param clazz the class to check
     * @return whether the given class is a primitive or primitive wrapper class
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * Check if the given class represents an array of primitives,
     * i.e. boolean, byte, char, short, int, long, float, or double.
     * @param clazz the class to check
     * @return whether the given class is a primitive array class
     */
    public static boolean isPrimitiveArray(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        return (clazz.isArray() && clazz.getComponentType().isPrimitive());
    }

    /**
     * Check if the given class represents an array of primitive wrappers,
     * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper array class
     */
    public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
    }

    /**
     * Resolve the given class if it is a primitive class,
     * returning the corresponding primitive wrapper type instead.
     * @param clazz the class to check
     * @return the original class, or a primitive wrapper for the original primitive type
     */
    public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() && clazz != void.class ? primitiveTypeToWrapperMap.get(clazz) : clazz);
    }

    /**
     * Check if the right-hand side type may be assigned to the left-hand side
     * type, assuming setting by reflection. Considers primitive wrapper
     * classes as assignable to the corresponding primitive types.
     * @param lhsType the target type
     * @param rhsType the value type that should be assigned to the target type
     * @return if the target type is assignable from the value type
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        Preconditions.checkNotNull(lhsType, "Left-hand side type must not be null");
        Preconditions.checkNotNull(rhsType, "Right-hand side type must not be null");
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isPrimitive()) {
            Class resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
            if (resolvedPrimitive != null && lhsType.equals(resolvedPrimitive)) {
                return true;
            }
        } else {
            Class resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
            if (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if the given type is assignable from the given value,
     * assuming setting by reflection. Considers primitive wrapper classes
     * as assignable to the corresponding primitive types.
     * @param type the target type
     * @param value the value that should be assigned to the type
     * @return if the type is assignable from the value
     */
    public static boolean isAssignableValue(Class<?> type, Object value) {
        Preconditions.checkNotNull(type, "Type must not be null");
        return (value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive());
    }

    /**
     * Convert a "/"-based resource path to a "."-based fully qualified class name.
     * @param resourcePath the resource path pointing to a class
     * @return the corresponding fully qualified class name
     */
    public static String convertResourcePathToClassName(String resourcePath) {
        Preconditions.checkNotNull(resourcePath, "Resource path must not be null");
        return resourcePath.replace('/', '.');
    }

    /**
     * Convert a "."-based fully qualified class name to a "/"-based resource path.
     * @param className the fully qualified class name
     * @return the corresponding resource path, pointing to the class
     */
    public static String convertClassNameToResourcePath(String className) {
        Preconditions.checkNotNull(className, "Class name must not be null");
        return className.replace('.', '/');
    }

    /**
     * Return a path suitable for use with <code>ClassLoader.getResource</code>
     * (also suitable for use with <code>Class.getResource</code> by prepending a
     * slash ('/') to the return value). Built by taking the package of the specified
     * class file, converting all dots ('.') to slashes ('/'), adding a trailing slash
     * if necessary, and concatenating the specified resource name to this.
     * <br/>As such, this function may be used to build a path suitable for
     * loading a resource file that is in the same package as a class file,
     * although {@link org.springframework.core.io.ClassPathResource} is usually
     * even more convenient.
     * @param clazz	the Class whose package will be used as the base
     * @param resourceName the resource name to append. A leading slash is optional.
     * @return the built-up resource path
     * @see java.lang.ClassLoader#getResource
     * @see java.lang.Class#getResource
     */
    public static String addResourcePathToPackagePath(Class<?> clazz, String resourceName) {
        Preconditions.checkNotNull(resourceName, "Resource name must not be null");
        if (!resourceName.startsWith("/")) {
            return classPackageAsResourcePath(clazz) + "/" + resourceName;
        }
        return classPackageAsResourcePath(clazz) + resourceName;
    }

    /**
     * Given an input class object, return a string which consists of the
     * class's package name as a pathname, i.e., all dots ('.') are replaced by
     * slashes ('/'). Neither a leading nor trailing slash is added. The result
     * could be concatenated with a slash and the name of a resource and fed
     * directly to <code>ClassLoader.getResource()</code>. For it to be fed to
     * <code>Class.getResource</code> instead, a leading slash would also have
     * to be prepended to the returned value.
     * @param clazz the input class. A <code>null</code> value or the default
     * (empty) package will result in an empty string ("") being returned.
     * @return a path which represents the package name
     * @see ClassLoader#getResource
     * @see Class#getResource
     */
    public static String classPackageAsResourcePath(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        String className = clazz.getName();
        int packageEndIndex = className.lastIndexOf('.');
        if (packageEndIndex == -1) {
            return "";
        }
        String packageName = className.substring(0, packageEndIndex);
        return packageName.replace('.', '/');
    }

    /**
     * Build a String that consists of the names of the classes/interfaces
     * in the given array.
     * <p>Basically like <code>AbstractCollection.toString()</code>, but stripping
     * the "class "/"interface " prefix before every class name.
     * @param classes a Collection of Class objects (may be <code>null</code>)
     * @return a String of form "[com.foo.Bar, com.foo.Baz]"
     * @see java.util.AbstractCollection#toString()
     */
    public static String classNamesToString(Class... classes) {
        return classNamesToString(Arrays.asList(classes));
    }

    /**
     * Build a String that consists of the names of the classes/interfaces
     * in the given collection.
     * <p>Basically like <code>AbstractCollection.toString()</code>, but stripping
     * the "class "/"interface " prefix before every class name.
     * @param classes a Collection of Class objects (may be <code>null</code>)
     * @return a String of form "[com.foo.Bar, com.foo.Baz]"
     * @see java.util.AbstractCollection#toString()
     */
    public static String classNamesToString(Collection<Class> classes) {
        if (CollectionUtils.isEmpty(classes)) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (Iterator<Class> it = classes.iterator(); it.hasNext();) {
            Class clazz = it.next();
            sb.append(clazz.getName());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Copy the given Collection into a Class array.
     * The Collection must contain Class elements only.
     * @param collection the Collection to copy
     * @return the Class array (<code>null</code> if the passed-in
     * Collection was <code>null</code>)
     */
    public static Class<?>[] toClassArray(Collection<Class<?>> collection) {
        if (collection == null) {
            return null;
        }
        return collection.toArray(new Class<?>[collection.size()]);
    }

    /**
     * Return all interfaces that the given instance implements as array,
     * including ones implemented by superclasses.
     * @param instance the instance to analyze for interfaces
     * @return all interfaces that the given instance implements as array
     */
    public static Class[] getAllInterfaces(Object instance) {
        Preconditions.checkNotNull(instance, "Instance must not be null");
        return getAllInterfacesForClass(instance.getClass());
    }

    /**
     * Return all interfaces that the given class implements as array,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     * @param clazz the class to analyze for interfaces
     * @return all interfaces that the given object implements as array
     */
    public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
        return getAllInterfacesForClass(clazz, null);
    }

    /**
     * Return all interfaces that the given class implements as array,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     * @param clazz the class to analyze for interfaces
     * @param classLoader the ClassLoader that the interfaces need to be visible in
     * (may be <code>null</code> when accepting all declared interfaces)
     * @return all interfaces that the given object implements as array
     */
    public static Class<?>[] getAllInterfacesForClass(Class<?> clazz, ClassLoader classLoader) {
        Set<Class> ifcs = getAllInterfacesForClassAsSet(clazz, classLoader);
        return ifcs.toArray(new Class[ifcs.size()]);
    }

    /**
     * Return all interfaces that the given instance implements as Set,
     * including ones implemented by superclasses.
     * @param instance the instance to analyze for interfaces
     * @return all interfaces that the given instance implements as Set
     */
    public static Set<Class> getAllInterfacesAsSet(Object instance) {
        Preconditions.checkNotNull(instance, "Instance must not be null");
        return getAllInterfacesForClassAsSet(instance.getClass());
    }

    /**
     * Return all interfaces that the given class implements as Set,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     * @param clazz the class to analyze for interfaces
     * @return all interfaces that the given object implements as Set
     */
    public static Set<Class> getAllInterfacesForClassAsSet(Class clazz) {
        return getAllInterfacesForClassAsSet(clazz, null);
    }

    /**
     * Return all interfaces that the given class implements as Set,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     * @param clazz the class to analyze for interfaces
     * @param classLoader the ClassLoader that the interfaces need to be visible in
     * (may be <code>null</code> when accepting all declared interfaces)
     * @return all interfaces that the given object implements as Set
     */
    public static Set<Class> getAllInterfacesForClassAsSet(Class clazz, ClassLoader classLoader) {
        Preconditions.checkNotNull(clazz, "Class must not be null");
        if (clazz.isInterface() && isVisible(clazz, classLoader)) {
            return Collections.singleton(clazz);
        }
        Set<Class> interfaces = new LinkedHashSet<Class>();
        while (clazz != null) {
            Class<?>[] ifcs = clazz.getInterfaces();
            for (Class<?> ifc : ifcs) {
                interfaces.addAll(getAllInterfacesForClassAsSet(ifc, classLoader));
            }
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    /**
     * Create a composite interface Class for the given interfaces,
     * implementing the given interfaces in one single Class.
     * <p>This implementation builds a JDK proxy class for the given interfaces.
     * @param interfaces the interfaces to merge
     * @param classLoader the ClassLoader to create the composite Class in
     * @return the merged interface as Class
     * @see java.lang.reflect.Proxy#getProxyClass
     */
    public static Class<?> createCompositeInterface(Class<?>[] interfaces, ClassLoader classLoader) {
        //        Assert.notEmpty(interfaces, "Interfaces must not be empty");
        Preconditions.checkNotNull(interfaces, "Interfaces must not be empty");
        Preconditions.checkNotNull(classLoader, "ClassLoader must not be null");
        return Proxy.getProxyClass(classLoader, interfaces);
    }

    /**
     * Check whether the given class is visible in the given ClassLoader.
     * @param clazz the class to check (typically an interface)
     * @param classLoader the ClassLoader to check against (may be <code>null</code>,
     * in which case this method will always return <code>true</code>)
     */
    public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
        if (classLoader == null) {
            return true;
        }
        try {
            Class<?> actualClass = classLoader.loadClass(clazz.getName());
            return (clazz == actualClass);
            // Else: different interface class found...
        } catch (ClassNotFoundException ex) {
            // No interface class found...
            return false;
        }
    }

    /**
     * Check whether the given object is a CGLIB proxy.
     * @param object the object to check
     * @see org.springframework.aop.support.AopUtils#isCglibProxy(Object)
     */
    public static boolean isCglibProxy(Object object) {
        return ClassUtils.isCglibProxyClass(object.getClass());
    }

    /**
     * Check whether the specified class is a CGLIB-generated class.
     * @param clazz the class to check
     */
    public static boolean isCglibProxyClass(Class<?> clazz) {
        return (clazz != null && isCglibProxyClassName(clazz.getName()));
    }

    /**
     * Check whether the specified class name is a CGLIB-generated class.
     * @param className the class name to check
     */
    public static boolean isCglibProxyClassName(String className) {
        return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
    }

}