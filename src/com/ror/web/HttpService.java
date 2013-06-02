package com.ror.web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import com.ror.constant.FCMConstants;
import com.ror.fcm.log.Logger;

/**
 * <pre>
 * HTTP UTIL
 * </pre>
 * 
 * @since 2011/11/10
 * @author rodeschen
 * @version <ul>
 *          <li>2011/11/10,rodeschen,new
 *          </ul>
 */
public class HttpService {

	private DefaultHttpClient httpClient;
	private HttpPost httpPost;
	private int httpStatus;
	private String responseData;
	private String defaultEncode = HTTP.UTF_8;
	private Object sendData;
	private Map<String, Object> properties;

	public void setProperties(Map<String, Object> property) throws Exception {
		if (property == null) {
			throw new Exception("http property error");
		}
		if (this.properties == null) {
			this.properties = (Map<String, Object>) property;
		} else {
			this.properties.putAll((Map<String, Object>) property);
		}
	}

	public void setProperty(String name, Object value) {
		if (properties == null) {
			properties = new HashMap<String, Object>();
		}
		properties.put(name, value);

	}

	@SuppressWarnings("unchecked")
	public <T> T getProperty(Object name) {
		return (T) (properties != null && properties.containsKey(name) ? properties
				.get(name) : null);
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public String getReceiveData() throws Exception {
		return responseData;
	}

	public void setSendData(Object data) throws Exception {
		this.sendData = data;
	}

	public Object getSendData() {
		return this.sendData;
	}

	public void setRequestBody(String body) throws UnsupportedEncodingException {
		httpPost.setEntity(new StringEntity(body, defaultEncode));
	}

	/**
	 * set http request key-value data
	 * 
	 * @param map
	 *            parameter
	 * @throws UnsupportedEncodingException
	 */
	public void setRequestParams(Map<String, String> map)
			throws UnsupportedEncodingException {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (String key : map.keySet()) {
			nvps.add(new BasicNameValuePair(key, map.get(key)));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, defaultEncode));
	}

	private void excuteHttp() throws Exception {
		String uri = getProperty(FCMConstants.HOST_URI);
		httpPost.setURI(new URI(uri));
		// long st = System.currentTimeMillis();
		// Logger.debug("host URI:" + uri);
		// if (this.sendData != null) {
		// Logger.debug("host send data:"
		// + this.sendData.toString().replace("\n", ""));
		// }
		// Logger.info("查詢資料中...", false);
		HttpResponse httpResponse = httpClient.execute(httpPost);
		httpStatus = httpResponse.getStatusLine().getStatusCode();
		responseData = StringUtils.join(IOUtils.readLines(
				httpResponse.getEntity().getContent()).toArray());
		// Logger.info((System.currentTimeMillis() - st) + "ms");
	}

	public void execute() throws Exception {
		excuteHttp();
	}

	@SuppressWarnings("unchecked")
	public void initConnection() {
		httpPost = new HttpPost();
		try {
			if (this.sendData instanceof Map) {
				setRequestParams((Map<String, String>) this.sendData);
			} else if (this.sendData instanceof String
					&& !StringUtils.isEmpty((String) this.sendData)) {
				setRequestBody((String) this.sendData);
			}
		} catch (UnsupportedEncodingException e) {
			Logger.debug(e.getMessage());
		}
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();

			String encode = getProperty(HTTP.TRANSFER_ENCODING);

			httpClient.getParams().setParameter(
					HttpConnectionParams.CONNECTION_TIMEOUT, 60000);
			httpClient.getParams().setParameter(
					HttpConnectionParams.SO_TIMEOUT, 60000);
			defaultEncode = encode != null ? encode : defaultEncode;
		}
	}

}
