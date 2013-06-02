package com.ror.fcm.btn;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import com.ror.fcm.FcmApp;
import com.ror.fcm.log.Logger;
import com.ror.web.FCMQuery;

public class ActionBtn extends Button {

	private static String ACTION_TEXT = "開始截取資料";
	private static String STOP_TEXT = "停止截取資料";
	private Text pathText;

	public ActionBtn(Composite parent, int style, Text pathText) {
		super(parent, style);
		this.setText(ACTION_TEXT);
		this.pathText = pathText;
		this.addSelectionListener(new SelectListener(this));
	}

	private class SelectListener extends SelectionAdapter {
		private ActionBtn btn;

		public SelectListener(ActionBtn btn) {
			this.btn = btn;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (btn.getSelection()) {
				if (StringUtils.isEmpty(pathText.getText())) {
					MessageBox box = new MessageBox(btn.getShell());
					box.setMessage("請先選擇路徑!!");
					box.open();
					btn.reset();
				} else {
					FcmApp.isStop = false;
					btn.setText(STOP_TEXT);
					Logger.info(ACTION_TEXT);
					new FCMQuery(btn, pathText.getText()).start();
				}
			} else {
				FcmApp.isStop = true;
				btn.reset();
			}

		}
	}

	@Override
	protected void checkSubclass() {

	}

	public void reset() {
		Display.getDefault().asyncExec(new Runnable() {
			private Button btn;

			public Runnable setBtn(Button btn) {
				this.btn = btn;
				return this;
			}

			@Override
			public void run() {
				btn.setText(ACTION_TEXT);
				btn.setSelection(false);

			}
		}.setBtn(this));

	}
}
