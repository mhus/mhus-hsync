package de.mhus.hsync.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map.Entry;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.mhus.hsync.lib.client.FileSync;
import de.mhus.hsync.lib.client.SyncConnection;
import de.mhus.hsync.lib.client.SyncMetadata;

public class SyncClient {

	static Logger log = Logger.getLogger(SyncClient.class.getName());
	
	public static void main(String[] args) throws IOException {
		
		String cmd = null;
		File root = null;
		String path = null;
		
		Logger rootLog = Logger.getLogger("");
		rootLog.setLevel( Level.INFO );
		rootLog.getHandlers()[0].setLevel( Level.INFO ); // Default console handler
		CustomRecordFormatter formatter = new CustomRecordFormatter();
		rootLog.getHandlers()[0].setFormatter(formatter);
		
		Properties props = new Properties();
		int freeCnt = 0;
		
		for (int i=0; i < args.length; i++) {
			String arg = args[i];
			if ("-vv".equals(arg)) {
				rootLog.setLevel( Level.FINER );
				rootLog.getHandlers()[0].setLevel( Level.FINER ); // Default console handler
			} else
			if ("-v".equals(arg)) {
				rootLog.setLevel( Level.FINE );
				rootLog.getHandlers()[0].setLevel( Level.FINE ); // Default console handler
			} else
			if ("-u".equals(arg)) {
				i++;
				props.setProperty("username", args[i]);
			} else
			if ("-p".equals(arg)) {
				i++;
				props.setProperty("password", args[i]);
			} else
			if ("-url".equals(arg)) {
				i++;
				props.setProperty("url", args[i]);
			} else
			if ("-r".equals(arg)) {
				i++;
				props.setProperty("repository", args[i]);
			} else
			if ("-d".equals(arg) || "-delete".equals(arg)) {
				props.setProperty("delete", "true");
			} else
			if ("-notsize".equals(arg)) {
				props.setProperty("checksize", "false");
			} else
			if ("-notmodified".equals(arg)) {
				props.setProperty("checkmodified", "false");
			} else
			if ("-overwrite".equals(arg)) {
				props.setProperty("overwrite", "true");
			} else {
				if (freeCnt == 0) {
					cmd = arg;
				} else
				if (freeCnt == 1) {
					root = new File(arg);
				} else {
					usage();
					return;
				}
				freeCnt++;
			}
		}
		
		if (root == null) {
			root = new File(".").getCanonicalFile();
		} else {
			root = root.getCanonicalFile();
		}
		
		File projectFile = findProject(root.getAbsoluteFile());
		if (projectFile != null) {
			path = root.getAbsolutePath().substring(projectFile.getAbsolutePath().length());
			File f = new File(projectFile, ".hsync.properties");
			Properties p = new Properties();
			FileInputStream fis = new FileInputStream(f);
			p.load(fis);
			fis.close();
			for (Entry<Object, Object> entry : p.entrySet()) {
				if (!props.containsKey(entry.getKey()))
					props.put(entry.getKey(), entry.getValue());
			}
		}
		
		if (cmd.equals("clone")) {
			if (projectFile != null) {
				System.out.println("Can't clone into other projects folder " + projectFile.getAbsolutePath());
				return;
			}
			root = new File(root, props.getProperty("repository"));
			if (root.exists()) {
				System.out.println("Root folder for repository already exists " + root.getAbsolutePath());
				return;
			}
			root.mkdir();
			
			File f = new File(root, ".hsync.properties");
			FileOutputStream fos = new FileOutputStream(f);
			props.save(fos, "");
			fos.close();
			
			cmd = "pull";
		}
		if (cmd.equals("pull")) {
			FileSync sync = new FileSync();
			fillSync(sync, props);
			SyncConnection con = createCon(props);
//			sync.setRoot(projectFile == null ? root : projectFile);
			sync.setRoot(root);
						
			sync.doPull(con, path);
			return;
		}
		if (cmd.equals("info")) {
			SyncConnection con = createCon(props);
			SyncMetadata meta = con.getMetadata();
			System.out.println("Repository  : " + meta.getName());
			System.out.println("Version     : " + meta.getVersion());
			System.out.println("Extensions  : " + Arrays.toString(meta.getExtensions()));
			System.out.println("Functions   : " + Arrays.toString(meta.getFunctions()));
			System.out.println("Description : " + meta.getDescription());
			if (projectFile != null) {
			System.out.println("Project Root: " + projectFile);
			System.out.println("Project Path: " + path);
			}
			return;
		}
		
		usage();
		
		
	}
	
	private static SyncConnection createCon(Properties props) {
		SyncConnection con = new SyncConnection(props.getProperty("url"), props.getProperty("username"), props.getProperty("password"), props.getProperty("repository"));
		return con;
	}

	private static void fillSync(FileSync sync, Properties props) {
		sync.setDelete(Boolean.valueOf(props.getProperty("delete", "false")));
		sync.setCheckModified(Boolean.valueOf(props.getProperty("checkmodified", "true")));
		sync.setCheckSize(Boolean.valueOf(props.getProperty("checksize", "true")));
		sync.setCreateLinks(Boolean.valueOf(props.getProperty("createlinks", "true")));
		sync.setOverwriteAll(Boolean.valueOf(props.getProperty("overwrite", "false")));
		sync.setTest(Boolean.valueOf(props.getProperty("test", "false")));
	}

	private static File findProject(File root) {
		log.fine("- search for project file in " + root);
		if (root == null) return null;
		File f = new File(root, ".hsync.properties");
		if (f.exists()) {
			log.fine("Found project file " + f);
			return root;
		}
		return findProject(root.getParentFile());
	}

	static void usage() {
		System.out.println("Usage: hsync -url <url> -u <user> -p <password> -r <repository> [-delete|-d -notsize -overwrite -v -vv] info|clone|pull [<root dir>]");
	}

	static class CustomRecordFormatter extends Formatter {
	    @Override
	    public String format(final LogRecord r) {
	        StringBuilder sb = new StringBuilder();
	        sb.append(formatMessage(r)).append(System.getProperty("line.separator"));
	        if (null != r.getThrown()) {
	            sb.append("Throwable occurred: "); //$NON-NLS-1$
	            Throwable t = r.getThrown();
	            PrintWriter pw = null;
	            try {
	                StringWriter sw = new StringWriter();
	                pw = new PrintWriter(sw);
	                t.printStackTrace(pw);
	                sb.append(sw.toString());
	            } finally {
	                if (pw != null) {
	                    try {
	                        pw.close();
	                    } catch (Exception e) {
	                        // ignore
	                    }
	                }
	            }
	        }
	        return sb.toString();
	    }
	}
}
