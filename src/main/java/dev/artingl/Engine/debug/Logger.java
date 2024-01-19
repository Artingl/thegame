package dev.artingl.Engine.debug;

import dev.artingl.Engine.misc.Utils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Logger extends PrintStream {

    public static Logger create(String prefix) {
        return new Logger(prefix);
    }

    private static final OutputStream baseErrStream = System.err;
    private static final OutputStream baseOutStream = System.out;

    private final OutputStream errStream;

    private final OutputStream outStream;

    private String prefix;

    private Logger(String prefix) {
        super(System.out);

        this.prefix = prefix;
        this.errStream = baseErrStream;
        this.outStream = baseOutStream;
    }

    private PrintStream _log(LogLevel level, String fmt, Object... args) {
        PrintStream stream = getStream(level);
        String datetime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String caller = Utils.getCallerInfo(1);

        stream.printf("[%s] [%s/%s] [%s]: ", datetime, level.name, prefix, caller);
        stream.printf(fmt + "\n", args);

        return stream;
    }

    // This gets called when any print function from System.out is called
    private void systemPrint(String v) {
        LogLevel level = LogLevel.INFO;
        PrintStream stream = getStream(level);
        String datetime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String caller = Utils.getCallerInfo(1);

        stream.printf("[%s] [%s/%s] [%s]: %s", datetime, level.name, prefix, caller, v);
    }

    public void exception(Exception e, String fmt, Object... args) {
        String excpInfo = Utils.getExceptionDetails(e);
        _log(LogLevel.ERROR, fmt + "\n" + excpInfo, args);
    }

    public void log(LogLevel level, String fmt, Object... args) {
        _log(level, fmt, args);
    }

    public PrintStream printf(String fmt, Object... args) {
        return _log(LogLevel.INFO, fmt, args);
    }

    public PrintStream printf(Locale l, String fmt, Object... args) {
        return printf(fmt, args);
    }


    public void print(String v) {
        systemPrint(v);
    }

    public void print(boolean b) {
        systemPrint(String.valueOf(b));
    }

    public void print(char c) {
        systemPrint(String.valueOf(c));
    }

    public void print(int i) {
        systemPrint(String.valueOf(i));
    }

    public void print(long l) {
        systemPrint(String.valueOf(l));
    }

    public void print(float f) {
        systemPrint(String.valueOf(f));
    }

    public void print(double d) {
        systemPrint(String.valueOf(d));
    }

    public void print(char[] s) {
        systemPrint(String.valueOf(s));
    }

    public void print(Object obj) {
        systemPrint(String.valueOf(obj));
    }

    public void println(String x) {
        systemPrint(x + "\n");
    }

    public void println() {
        println("");
    }

    public void println(boolean x) {
        println(String.valueOf(x));
    }

    public void println(char x) {
        println(String.valueOf(x));
    }

    public void println(int x) {
        println(String.valueOf(x));
    }

    public void println(long x) {
        println(String.valueOf(x));
    }

    public void println(float x) {
        println(String.valueOf(x));
    }

    public void println(double x) {
        println(String.valueOf(x));
    }

    public void println(char[] x) {
        println(String.valueOf(x));
    }

    public void println(Object x) {
        println(String.valueOf(x));
    }


    public String getPrefix() {
        return prefix;
    }

    public PrintStream getStream(LogLevel level) {
        return new PrintStream(switch (level) {
            case INFO, WARNING -> outStream;
            case ERROR, UNIMPLEMENTED -> errStream;
        });
    }

    public PrintStream getErrStream() {
        return new PrintStream(errStream);
    }

    public PrintStream getOutStream() {
        return new PrintStream(outStream);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
