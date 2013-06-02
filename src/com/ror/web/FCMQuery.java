package com.ror.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jodd.io.FileUtil;
import jodd.lagarto.dom.jerry.Jerry;
import jodd.lagarto.dom.jerry.JerryFunction;

import org.apache.commons.lang.StringUtils;

import com.ror.constant.FCMConstants;
import com.ror.fcm.FcmApp;
import com.ror.fcm.btn.ActionBtn;
import com.ror.fcm.log.Logger;

public class FCMQuery {

	private HttpService httpService;
	private ActionBtn btn;
	private String exportPath;

	public FCMQuery(ActionBtn button, String exportPath) {
		httpService = new HttpService();
		this.btn = button;
		this.exportPath = exportPath;
	}

	public void start() {
		new Thread() {
			@Override
			public void run() {
				try {
					Logger.info("***************************期貨截取開始***************************");
					step1();
					Logger.info("***************************期貨截取結束***************************");
					Logger.info("**************************選擇權截取開始**************************");
					step2();
					Logger.info("**************************選擇權截取結束**************************");
				} catch (Exception e) {
					if (e.getMessage().equals("stop")) {
						Logger.debug("停止!!");
					} else {
						Logger.debug("程式執行錯誤!!!!!" + e.getMessage());
						e.printStackTrace();
					}
				} finally {
					btn.reset();
				}
			}

		}.start();
	}

	// 取得所有期貨option
	private void step1() throws Exception {

		Jerry doc = Jerry.jerry(queryData(FCMConstants.URL_1, null));
		Logger.info("取得所有期貨選單");

		Map<String, String> commodity_idt = new LinkedHashMap<String, String>();

		Map<String, String> commodity_id2t = new LinkedHashMap<String, String>();

		// 取得所有 契約 代號
		doc.$("#commodity_idt > option").each(new JerryFunction() {

			private Map<String, String> nameMap;

			public JerryFunction setCollection(Map<String, String> map) {
				this.nameMap = map;
				return this;
			}

			@Override
			public boolean onNode(Jerry jerry, int index) {
				if (StringUtils.isNotEmpty(jerry.attr("value"))) {
					nameMap.put(jerry.attr("value"), jerry.text());

				}
				return true;
			}

		}.setCollection(commodity_idt));

		// 取得股票類契約
		doc.$("#commodity_id2t > option").each(new JerryFunction() {
			private Map<String, String> nameMap;

			public JerryFunction setCollection(Map<String, String> map) {
				this.nameMap = map;
				return this;
			}

			@Override
			public boolean onNode(Jerry jerry, int index) {
				if (StringUtils.isNotEmpty(jerry.attr("value"))) {
					nameMap.put(jerry.attr("value"), jerry.text());
				}
				return true;
			}

		}.setCollection(commodity_id2t));

		// 依契約取得各月份
		for (Entry<String, String> entry : commodity_idt.entrySet()) {
			List<String> monthList;
			String key = entry.getKey();

			File direct = new File(exportPath + "/"
					+ FCMConstants.EXPORT_DIRECT_1);
			if (!direct.exists()) {
				FileUtil.mkdir(direct);
			}
			// 非股票類
			if (!StringUtils.equals(key, "STF")) {
				Logger.info("取得「" + entry.getValue() + "」資料…");
				Jerry subDoc = Jerry.jerry(queryData(
						FCMConstants.URL_1,
						createParams(key, null, null, null, null, null, null,
								null, "1", "1")));
				Logger.info("取得「" + entry.getValue() + "」各月份資料…");
				// 取得所有月份
				monthList = getMonthList(subDoc);
				Map<String, String> defaultParams = createParams(key, "", key,
						"", entry.getValue(), null, null, null, "2", null);
				File file;
				if (!monthList.isEmpty()) {
					// 產生檔案
					for (String month : monthList) {

						file = new File(direct.getAbsolutePath() + "/"
								+ entry.getValue() + "_" + month + ".csv");
						FileUtil.writeString(file, StringUtils.EMPTY,
								FCMConstants.EXPORT_ENCODE);
						Logger.info("取得「" + month + "」日期資料…", false);
						exportDetailData(month, null, 0, defaultParams, file,
								entry.getValue(), FCMConstants.URL_1);
						Logger.info("寫入「" + month + "」日期資料完成!!");
					}

				} else {
					Logger.info("「" + entry.getValue() + "」無任何資料…");
				}

			}
			// 股票類
			else if (StringUtils.equals(key, "STF")) {
				direct = new File(direct.getAbsoluteFile() + "/"
						+ FCMConstants.EXPORT_DIRECT_1_1);
				if (!direct.exists()) {
					FileUtil.mkdir(direct);
				}

				for (Entry<String, String> entry2 : commodity_id2t.entrySet()) {
					Logger.info("取得「" + entry.getValue() + " - "
							+ entry2.getValue() + "」資料…");
					String key2 = entry2.getKey();
					Jerry subDoc = Jerry.jerry(queryData(
							FCMConstants.URL_1,
							createParams(key, key2, null, key2, null, null,
									null, null, "1", "1")));
					Logger.info("取得「" + entry.getValue() + " - "
							+ entry2.getValue() + "」各月份資料…");
					// 取得所有月份
					monthList = getMonthList(subDoc);
					Map<String, String> defaultParams = createParams(key, key2,
							key, key2, entry2.getValue(), null, null, null,
							"2", null);
					File file;
					if (!monthList.isEmpty()) {
						// 產生檔案
						for (String month : monthList) {
							Logger.info("取得「" + month + "」日期資料…", false);
							file = new File(direct.getAbsolutePath() + "/"
									+ entry.getValue() + " - "
									+ entry2.getValue() + "_" + month + ".csv");
							FileUtil.writeString(file, StringUtils.EMPTY,
									FCMConstants.EXPORT_ENCODE);
							exportDetailData(
									month,
									null,
									0,
									defaultParams,
									file,
									entry.getValue() + " - "
											+ entry2.getValue(),
									FCMConstants.URL_1);
							Logger.info("寫入「" + month + "」日期資料完成!!");
						}

					} else {
						Logger.info("\t「" + entry.getValue() + " - "
								+ entry2.getValue() + "」無任何資料…");
					}
				}
			}
		}

	}

	private List<String> getMonthList(Jerry Jerry) {
		List<String> monthList = new ArrayList<String>();
		Jerry.$("#settlemon > option").each(new JerryFunction() {
			public JerryFunction setList(List<String> list) {
				this.list = list;
				return this;
			}

			private List<String> list;

			@Override
			public boolean onNode(Jerry jerry, int index) {
				if (StringUtils.isNotEmpty(jerry.attr("value"))) {
					list.add(jerry.attr("value"));
				}
				return true;
			}

		}.setList(monthList));
		return monthList;
	}

	private void exportDetailData(String month, String page, int totalPage,
			Map<String, String> params, File file, String comment, String URL)
			throws Exception {
		StringBuffer sb = new StringBuffer();
		params.put("settlemon", month);
		params.put("curpage", page);
		Jerry queryDoc = Jerry.jerry(queryData(URL, params));
		// 有title 則動作
		Jerry docTitle = queryDoc.$("table:eq(2)");
		if (docTitle.size() == 1) {
			if (page == null) {
				sb.append(getFirstLine(docTitle)).append(FCMConstants.NEW_LINE)
						.append(FCMConstants.NEW_LINE);
			}
			Jerry docData = queryDoc.$("table:eq(3)");
			if (docData.size() == 1) {
				// 查詢頁碼
				List<String> pagger = null;
				if (page == null) {
					pagger = getPagger(queryDoc);
					totalPage = pagger.size() + 1;
					Logger.info("共" + totalPage + "頁");
				}
				// 如果沒有頁數則不需要title
				sb.append(getDetailAllLine(docData.find("tr"), page == null));
				FileUtil.appendString(file, sb.toString(),
						FCMConstants.EXPORT_ENCODE);
				Logger.info("寫入「" + comment + "-" + month + "」日期資料…第"
						+ (StringUtils.isEmpty(page) ? "1" : page) + "/"
						+ totalPage + "頁完成");
				while (pagger != null && !pagger.isEmpty()) {
					exportDetailData(month, pagger.remove(0), totalPage,
							params, file, comment, URL);
				}
			} else {
				Logger.info("無內容");
			}
		} else {
			Logger.info("查無資料");
		}
	}

	private String queryData(String url, Map<String, String> params)
			throws Exception {
		if (FcmApp.isStop) {
			throw new Exception("stop");
		}
		String res = null;
		if (params != null) {
			httpService.setSendData(params);
		}
		httpService.initConnection();
		httpService.setProperty(FCMConstants.HOST_URI, url);

		httpService.execute();
		res = httpService.getReceiveData();
		String resStr = res;//new String(res.getBytes("UTF-8"), FCMConstants.EXPORT_ENCODE);
		return resStr;
	}

	private Map<String, String> createParams(String commodity_idt,
			String commodity_id2t, String commodity_id, String commodity_id2,
			String commodity_name, String downloadflag, String qflag,
			String settlemon, String qtype, String curpage) {
		Map<String, String> map = new HashMap<String, String>();
		putParmas(map, "commodity_idt", commodity_idt);
		putParmas(map, "commodity_id2t", commodity_id2t);
		putParmas(map, "commodity_id", commodity_id);
		putParmas(map, "commodity_id2", commodity_id2);
		putParmas(map, "commodity_name", commodity_name);
		putParmas(map, "downloadflag", downloadflag);
		putParmas(map, "qflag", qflag);
		putParmas(map, "settlemon", settlemon);
		putParmas(map, "qtype", qtype);
		putParmas(map, "curpage", curpage);
		return map;
	}

	private void putParmas(Map<String, String> m, String key, String value) {
		m.put(key, StringUtils.isEmpty(value) ? StringUtils.EMPTY : value);
	}

	private List<String> getPagger(Jerry jerry) {
		List<String> pagger = new ArrayList<String>();
		jerry.$("#pagenum > option").each(new JerryFunction() {

			private List<String> list;

			public JerryFunction setList(List<String> l) {
				list = l;
				return this;
			}

			@Override
			public boolean onNode(Jerry jerry, int index) {
				list.add(jerry.attr("value"));
				return true;
			}
		}.setList(pagger));
		if (pagger.size() >= 1) {
			pagger.remove(0);
		}
		return pagger;
	}

	private StringBuffer getFirstLine(Jerry jerry) {
		return writterLine(jerry.find("tr").children());
	}

	private StringBuffer getDetailAllLine(Jerry jerry,
			final boolean getFirstLine) {
		StringBuffer sb = new StringBuffer();
		jerry.each(new JerryFunction() {
			private StringBuffer sb;

			public JerryFunction setSb(StringBuffer sb) {
				this.sb = sb;
				return this;
			}

			@Override
			public boolean onNode(Jerry jerry, int index) {
				if (index != 0 || (index == 0 && getFirstLine)) {
					sb.append(writterLine(jerry.children())).append(
							FCMConstants.NEW_LINE);
				}
				return true;
			}
		}.setSb(sb));
		return sb;
	}

	private StringBuffer writterLine(Jerry jerry) {
		StringBuffer sb = new StringBuffer();
		jerry.each(new JerryFunction() {

			private StringBuffer sb;

			public JerryFunction setSb(StringBuffer sb) {
				this.sb = sb;
				return this;
			}

			@Override
			public boolean onNode(Jerry jerry, int index) {
				if ("合計".equals(jerry.text().trim())) {
					sb.append(FCMConstants.COMMA).append(FCMConstants.COMMA);
				} else if ("合計：".equals(jerry.text().trim())) {
					sb.append(FCMConstants.COMMA);
				}
				sb.append(
						jerry.text().trim()
								.replace(FCMConstants.COMMA, StringUtils.EMPTY))
						.append(FCMConstants.COMMA);
				return true;
			}
		}.setSb(sb));
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb;
	}

	// 取得所有期貨option
	private void step2() throws Exception {

		Jerry doc = Jerry.jerry(queryData(FCMConstants.URL_2, null));
		Logger.info("取得所有選擇權選單");

		Map<String, String> commodity_idt = new LinkedHashMap<String, String>();

		Map<String, String> commodity_id2t = new LinkedHashMap<String, String>();

		// 取得所有 契約 代號
		doc.$("#commodity_idt > option").each(new JerryFunction() {

			private Map<String, String> nameMap;

			public JerryFunction setCollection(Map<String, String> map) {
				this.nameMap = map;
				return this;
			}

			@Override
			public boolean onNode(Jerry jerry, int index) {
				if (StringUtils.isNotEmpty(jerry.attr("value"))) {
					nameMap.put(jerry.attr("value"), jerry.text());

				}
				return true;
			}

		}.setCollection(commodity_idt));

		// 取得股票類契約
		doc.$("#commodity_id2t > option").each(new JerryFunction() {
			private Map<String, String> nameMap;

			public JerryFunction setCollection(Map<String, String> map) {
				this.nameMap = map;
				return this;
			}

			@Override
			public boolean onNode(Jerry jerry, int index) {
				if (StringUtils.isNotEmpty(jerry.attr("value"))) {
					nameMap.put(jerry.attr("value"), jerry.text());
				}
				return true;
			}

		}.setCollection(commodity_id2t));

		// 依契約取得各月份
		for (Entry<String, String> entry : commodity_idt.entrySet()) {
			List<String> monthList;
			String key = entry.getKey();

			File direct = new File(exportPath + "/"
					+ FCMConstants.EXPORT_DIRECT_2);
			if (!direct.exists()) {
				FileUtil.mkdir(direct);
			}
			// 非股票類
			if (!StringUtils.equals(key, "STO")) {
				Logger.info("取得「" + entry.getValue() + "」資料…");
				Jerry subDoc = Jerry.jerry(queryData(
						FCMConstants.URL_2,
						createParams(key, null, null, null, null, null, null,
								null, "1", "1")));
				Logger.info("取得「" + entry.getValue() + "」各月份資料…");
				// 取得所有月份
				monthList = getMonthList(subDoc);
				Map<String, String> defaultParams = createParams(key, "", key,
						"", entry.getValue(), null, null, null, "2", null);
				File file;
				if (!monthList.isEmpty()) {
					// 產生檔案
					for (String month : monthList) {

						defaultParams.put("pccode", "C");// 買權
						Logger.info("取得「買權-" + month + "」日期資料…", false);
						file = new File(direct.getAbsolutePath() + "/"
								+ entry.getValue() + "_" + month + "_買權.csv");
						FileUtil.writeString(file, StringUtils.EMPTY,
								FCMConstants.EXPORT_ENCODE);
						exportDetailData(month, null, 0, defaultParams, file,
								entry.getValue() + "-買權", FCMConstants.URL_2);

						defaultParams.put("pccode", "P");// 賣權
						Logger.info("取得「賣權-" + month + "」日期資料…", false);
						file = new File(direct.getAbsolutePath() + "/"
								+ entry.getValue() + "_" + month + "_賣權.csv");
						FileUtil.writeString(file, StringUtils.EMPTY,
								FCMConstants.EXPORT_ENCODE);
						exportDetailData(month, null, 0, defaultParams, file,
								entry.getValue() + "-賣權", FCMConstants.URL_2);
						Logger.info("寫入「" + month + "」日期資料完成!!");
					}

				} else {
					Logger.info("「" + entry.getValue() + "」無任何資料…");
				}

			}
			// 股票類
			else if (StringUtils.equals(key, "STO")) {
				direct = new File(direct.getAbsoluteFile() + "/"
						+ FCMConstants.EXPORT_DIRECT_2_1);
				if (!direct.exists()) {
					FileUtil.mkdir(direct);
				}

				for (Entry<String, String> entry2 : commodity_id2t.entrySet()) {
					Logger.info("取得「" + entry.getValue() + " - "
							+ entry2.getValue() + "」資料…");
					String key2 = entry2.getKey();
					Jerry subDoc = Jerry.jerry(queryData(
							FCMConstants.URL_2,
							createParams(key, key2, null, key2, null, null,
									null, null, "1", "1")));
					Logger.info("取得「" + entry.getValue() + " - "
							+ entry2.getValue() + "」各月份資料…");
					// 取得所有月份
					monthList = getMonthList(subDoc);
					Map<String, String> defaultParams = createParams(key, key2,
							key, key2, entry2.getValue(), null, null, null,
							"2", null);
					File file;
					if (!monthList.isEmpty()) {
						// 產生檔案
						for (String month : monthList) {

							defaultParams.put("pccode", "C");// 買權
							Logger.info("取得「買權-" + month + "」日期資料…", false);
							file = new File(direct.getAbsolutePath() + "/"
									+ entry.getValue() + " - "
									+ entry2.getValue() + "_" + month
									+ "_買權.csv");
							FileUtil.writeString(file, StringUtils.EMPTY,
									FCMConstants.EXPORT_ENCODE);
							exportDetailData(
									month,
									null,
									0,
									defaultParams,
									file,
									entry.getValue() + " - "
											+ entry2.getValue(),
									FCMConstants.URL_2);

							defaultParams.put("pccode", "P");// 賣權
							Logger.info("取得「賣權-" + month + "」日期資料…", false);
							file = new File(direct.getAbsolutePath() + "/"
									+ entry.getValue() + " - "
									+ entry2.getValue() + "_" + month
									+ "_賣權.csv");
							FileUtil.writeString(file, StringUtils.EMPTY,
									FCMConstants.EXPORT_ENCODE);
							exportDetailData(
									month,
									null,
									0,
									defaultParams,
									file,
									entry.getValue() + " - "
											+ entry2.getValue(),
									FCMConstants.URL_2);
							Logger.info("寫入「" + month + "」日期資料完成!!");
						}

					} else {
						Logger.info("「" + entry.getValue() + " - "
								+ entry2.getValue() + "」無任何資料…");
					}
				}
			}
		}

	}

}
