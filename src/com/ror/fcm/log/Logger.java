package com.ror.fcm.log;

import org.eclipse.swt.widgets.Text;

import com.ror.fcm.FcmApp;
import com.ror.fcm.IAction;

public class Logger {

	private static Text text;

	public static Text getText() {
		return text;
	}

	public static void setText(Text text) {
		Logger.text = text;
		info(System.getProperty("os.name"));
		info(System.getProperty("os.version"));
		info(System.getProperty("os.arch"));
	}

	public static void info(Object str) {
		writter(str, true);
	}

	public static void info(Object str, boolean newLine) {
		writter(str, newLine);
	}

	public static void debug(Object str) {
		writter(str, true);
	}

	public static void debug(Object str, boolean newLine) {
		writter(str, newLine);
	}

	public static void writter(final Object str, final boolean newLine) {
		FcmApp.refresh(new IAction() {
			@Override
			public void action() {
				text.append(str.toString() + (newLine ? '\n' : ""));

			}
		});
	}
}
