package de.mhus.hsync.lib.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.http.HttpHost;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class SyncConnection {

	private static Logger log = Logger.getLogger(SyncConnection.class.getName());
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

	public SyncMetadata getMetadata() {
		try {
			HashMap<String, Object> parameters = new HashMap<>();
			parameters.put("repository", repository);
			parameters.put("function", "metadata");
			HttpResponse<JsonNode> res = doPost(parameters);
			if (res == null) return null;
			return new IntMetadata(res.getBody());
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public SyncStructure getStructure(String path, Long modified, Integer depth) {
		try {
			HashMap<String, Object> parameters = new HashMap<>();
			parameters.put("repository", repository);
			parameters.put("function", "structure");
			if (path != null) parameters.put("path", path);
			if (modified != null) parameters.put("modified", modified);
			if (depth != null) parameters.put("depth", depth);
			
			HttpResponse<JsonNode> res = doPost(parameters);
			if (res == null) return null;
			return new IntStructure(res.getBody() );
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
	
	public boolean getFile(String path, OutputStream os) {
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
				return false;
			}

			InputStream is = res.getBody();
			// TODO Optimize
			while (true) {
				int i = is.read();
				if (i < 0) break;
				os.write(i);
			}
			is.close();
			
			return true;
					
		} catch (Throwable t) {
			log.warning(t.toString());
			// t.printStackTrace();
			return false;
		}
		
	}

	public boolean getFiles(FileCallback callback, String ... path ) {
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
				return false;
			}
			
			File tmp = File.createTempFile("sync", ".zip");
			FileOutputStream os = new FileOutputStream(tmp);

			InputStream is = res.getBody();
			// TODO Optimize
			while (true) {
				int i = is.read();
				if (i < 0) break;
				os.write(i);
			}
			is.close();
			os.close();
			
			ZipFile zip = new ZipFile(tmp);
			for (Enumeration<? extends ZipEntry> enu = zip.entries(); enu.hasMoreElements();) {
				ZipEntry entry = enu.nextElement();
				InputStream eis = zip.getInputStream(entry);
				try {
					callback.foundFile(entry.getName(), eis);
				} catch (Throwable t) {
					log.info(t.toString());
				}
				eis.close();
			}
			zip.close();
			tmp.delete();
			
			return true;
					
		} catch (Throwable t) {
			log.warning(t.toString());
//			t.printStackTrace();
			return false;
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
	
	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getHostUrl() {
		return hostUrl;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	
	public String toString() {
		return hostUrl + ":" + repository;
	}
	
	private static class IntMetadata implements SyncMetadata {

		private JSONObject data;

		public IntMetadata(JsonNode data) {
			this.data = data.getObject();
		}

		@Override
		public String getName() {
			return data.getString("name");
		}

		@Override
		public String getDescription() {
			return data.getString("description");
		}

		@Override
		public String[] getExtensions() {
			return data.getString("extensions").split(",");
		}

		@Override
		public String[] getFunctions() {
			return data.getString("functions").split(",");
		}

		@Override
		public int getVersion() {
			return data.getInt("version");
		}

		@Override
		public Object get(String name) {
			return data.get(name);
		}
		
		public String toString() {
			return getName() + ":" + getVersion();
		}

		@Override
		public String getData() {
			return data.toString();
		}
	}
	
	private static class IntStructure implements SyncStructure {

		private JSONObject data;
		private String path;

		public IntStructure(JsonNode body) {
			data = body.getObject();
			path = "";
		}

		public IntStructure(JSONObject data, String parentPath) {
			this.data = data;
			this.path = parentPath + "/" + getName();
		}

		@Override
		public Object get(String name) {
			return data.get(name);
		}

		@Override
		public boolean isFile() {
			if (!data.has("type")) return false;
			return "f".equals(data.getString("type"));
		}

		@Override
		public boolean isDirectory() {
			if (!data.has("type")) return false;
			return "d".equals(data.getString("type"));
		}

		@Override
		public String getName() {
			return data.getString("name");
		}

		@Override
		public long getModifyDate() {
			if (!data.has("modified")) return 0;
			return data.getLong("modified");
		}

		@Override
		public List<SyncStructure> getChildren() {
			LinkedList<SyncStructure> out = new LinkedList<>();
			if (data.has("nodes")) {
				JSONArray children = data.getJSONArray("nodes");
				for (int i = 0; i < children.length(); i++) { 
					JSONObject item = children.getJSONObject(i);
					out.add(new IntStructure(item, path));
				}
			}
			return out;
		}
		
		public String getData() {
			return data.toString();
		}
		
		public String toString() {
			return path;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public long getSize() {
			if (!data.has("size")) return 0;
			return data.getLong("size");
		}

		@Override
		public boolean isLink() {
			return data.has("target");
		}

		@Override
		public String getLinkTaget() {
			if (!data.has("target")) return null;
			return data.getString("target");
		}

		@Override
		public boolean hasChildren() {
			return data.has("nodes");
		}

	}
}
