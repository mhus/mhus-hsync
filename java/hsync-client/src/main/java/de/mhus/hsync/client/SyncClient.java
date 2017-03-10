package de.mhus.hsync.client;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.mhus.hsync.lib.client.FileSync;
import de.mhus.hsync.lib.client.SyncConnection;

public class SyncClient {

	public static void main(String[] args) throws IOException {
		
		FileSync sync = new FileSync();
		String username = null;
		String password = null;
		String repository = null;
		String url = null;
		boolean pull = true;

		Logger rootLog = Logger.getLogger("");
		rootLog.setLevel( Level.INFO );
		rootLog.getHandlers()[0].setLevel( Level.INFO ); // Default console handler

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
				username = args[i];
			} else
			if ("-p".equals(arg)) {
				i++;
				password = args[i];
			} else
			if ("-url".equals(arg)) {
				i++;
				url = args[i];
			} else
			if ("-r".equals(arg)) {
				i++;
				repository = args[i];
			} else
			if ("-d".equals(arg) || "-delete".equals(arg)) {
				sync.setDelete(true);
			} else
			if ("-versions".equals(arg)) {
				sync.setVersions(true);
			} else
			if ("-size".equals(arg)) {
				sync.setCheckSize(true);
			} else
			if ("-overwrite".equals(arg)) {
				sync.setOverwriteAll(true);
			} else
			if ("pull".equals(arg)) {
				pull = true;
			} else
			if (!arg.startsWith("-")) {
				sync.setRoot(new File(arg));
			}
		}
		
		if (!pull || sync.getRoot() == null) {
			System.out.println("Usage: hsync -url <url> -u <user> -p <password> -r <repository> [-delete|-d -size -overwrite -v -vv] pull <root dir>");
			return;
		}
		
		
		
		SyncConnection con = new SyncConnection(url, username, password, repository);
		sync.doSync(con);
		
	}

}
