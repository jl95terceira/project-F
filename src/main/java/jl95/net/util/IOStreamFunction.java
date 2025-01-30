package jl95.net.util;

@FunctionalInterface
public interface IOStreamFunction<T> {

    T apply(java.io.InputStream in, java.io.OutputStream out);
}
