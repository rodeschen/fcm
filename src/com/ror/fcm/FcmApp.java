package com.ror.fcm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ror.fcm.btn.ActionBtn;
import com.ror.fcm.btn.PathBtn;
import com.ror.fcm.log.Logger;

public class FcmApp {

	protected Shell shlFcm;
	private Text logViewer;
	private Text exportPath;
	private Button pathBtn;
	private Button actionBtn;
	private static FcmApp fcmApp;
	public static boolean isStop = false;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FcmApp.getFcmApp();
	}

	public static FcmApp getFcmApp() {
		if (fcmApp == null) {
			try {
				fcmApp = new FcmApp();
				fcmApp.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return fcmApp;
	}

	/**
	 * Open the window.
	 */
	public void open() {
		// Display display = Display.getDefault();
		shlFcm = new Shell();
		shlFcm.setSize(611, 472);
		shlFcm.setText("FCM");
		createContents();
		shlFcm.open();
		shlFcm.layout();
		while (!shlFcm.isDisposed()) {
			if (!Display.getDefault().readAndDispatch()) {
				Display.getDefault().sleep();
			}
		}

	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {

		logViewer = new Text(shlFcm, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		logViewer.setBounds(10, 51, 591, 389);
		Logger.setText(logViewer);
		exportPath = new Text(shlFcm, SWT.BORDER | SWT.READ_ONLY);
		exportPath.setBounds(10, 14, 291, 19);

		pathBtn = new PathBtn(shlFcm, SWT.NONE, exportPath);
		pathBtn.setBounds(307, 10, 111, 28);
		actionBtn = new ActionBtn(shlFcm, SWT.BORDER | SWT.TOGGLE, exportPath);
		actionBtn.setBounds(460, 10, 141, 28);

	}

	public static void refresh(final IAction action) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				action.action();
			}
		});
	}

}
