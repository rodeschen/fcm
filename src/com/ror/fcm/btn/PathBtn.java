package com.ror.fcm.btn;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;

public class PathBtn extends Button {

	private static String BTN_TEXT = "選擇輸出路徑";
	private Text pathText;

	public PathBtn(Composite parent, int style, Text pathText) {
		super(parent, style);
		this.setText(BTN_TEXT);
		this.addSelectionListener(new SelectListener(this));
		this.pathText = pathText;
	}

	private class SelectListener extends SelectionAdapter {
		private Button btn;

		public SelectListener(Button btn) {
			this.btn = btn;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			String path = new DirectoryDialog(btn.getShell()).open();
			if (StringUtils.isNotEmpty(path)) {
				pathText.setText(path);
			}
		}
	}

	@Override
	protected void checkSubclass() {

	}

}
