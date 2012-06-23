package teampg199;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
	private static Logger inst;
	private static DateFormat formatter;

	private final PrintStream out;

	static {
		inst = new Logger();
		formatter = new SimpleDateFormat("yyyy MMM dd, HH:mm:ss z");
	}

	private Logger() {
		out = System.out;
	}

	public static void log(String msg) {
		inst.out.println(formatter.format(Calendar.getInstance().getTime()) + "|||" + msg);
	}
}
