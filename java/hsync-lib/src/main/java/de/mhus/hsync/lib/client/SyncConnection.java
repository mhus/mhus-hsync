package de.mhus.hsync.lib.client;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class SyncConnection {

	private String repository;
	private String hostUrl;
	private String username;
	private String password;
	private int status;
	private String statusMsg;
	private String body;

	public SyncConnection(String hostUrl, String username, String password, String repository){
		this(hostUrl,username,password, null, repository);
	}
	
	public SyncConnection(String hostUrl, String username, String password, HttpHost proxy, String repository){
		this.hostUrl = hostUrl;
		this.username = username;
		this.password = password;
		this.repository = repository;
		if (proxy != null) Unirest.setProxy(proxy);
	}

	public JsonNode getMetadata() {
		try {
			HashMap<String, Object> parameters = new HashMap<>();
			parameters.put("repository", repository);
			parameters.put("function", "metadata");
			HttpResponse<JsonNode> res = doPost(parameters);
			if (res == null) return null;
			return res.getBody();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public JsonNode getStructure(String path, Long modified, Integer depth) {
		try {
			HashMap<String, Object> parameters = new HashMap<>();
			parameters.put("repository", repository);
			parameters.put("function", "structure");
			if (path != null) parameters.put("path", path);
			if (modified != null) parameters.put("modified", modified);
			if (depth != null) parameters.put("depth", depth);
			
			HttpResponse<JsonNode> res = doPost(parameters);
			if (res == null) return null;
			return res.getBody();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
	
	public InputStream getFile(String path) {
		try {
			
			status = 0;
			statusMsg = null;
			body = null;

			HashMap<String, Object> parameters = new HashMap<>();
			parameters.put("repository", repository);
			parameters.put("function", "file");
			parameters.put("path", path);
			
			HttpResponse<InputStream> res = Unirest
					.post(hostUrl)
					.fields(parameters)
					.basicAuth(username, password)
					.asBinary();

			status = res.getStatus();
			statusMsg = res.getStatusText();
			if (res.getStatus() != 200) {
				InputStream is = res.getRawBody();
				StringBuffer o = new StringBuffer();
				while (true) {
					int i = is.read();
					if (i < 0) break;
					o.append((char)i);
				}
				body = o.toString();
				return null;
			}

			return res.getBody();
					
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
		
	}

	public InputStream getFiles(String ... path ) {
		try {
			
			status = 0;
			statusMsg = null;
			body = null;

			HashMap<String, Object> parameters = new HashMap<>();
			parameters.put("repository", repository);
			parameters.put("function", "files");
			for (int i = 0; i < path.length; i++)
				parameters.put("path" + i, path[i]);
			
			HttpResponse<InputStream> res = Unirest
					.post(hostUrl)
					.fields(parameters)
					.basicAuth(username, password)
					.asBinary();

			status = res.getStatus();
			statusMsg = res.getStatusText();
			if (res.getStatus() != 200) {
				InputStream is = res.getRawBody();
				StringBuffer o = new StringBuffer();
				while (true) {
					int i = is.read();
					if (i < 0) break;
					o.append((char)i);
				}
				body = o.toString();
				return null;
			}

			return res.getBody();
					
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
		
	}
	
	public HttpResponse<JsonNode> doPost(Map<String,Object> parameters) throws Exception{
		
		status = 0;
		statusMsg = null;
		body = null;

		HttpResponse<JsonNode> res = Unirest
				.post(hostUrl)
				.fields(parameters)
				.basicAuth(username, password)
				.asJson();
				
		status = res.getStatus();
		statusMsg = res.getStatusText();
		if (res.getStatus() != 200) {
			InputStream is = res.getRawBody();
			StringBuffer o = new StringBuffer();
			while (true) {
				int i = is.read();
				if (i < 0) break;
				o.append((char)i);
			}
			body = o.toString();
			return null;
		}
		return res;
	}

	public String getMessage() {
		return status + " " + statusMsg + (body == null ? "" : " " + body);
	}
}
