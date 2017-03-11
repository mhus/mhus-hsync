package de.mhus.hsync.lib.client;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestFileSync {

	public static void main(String[] args) throws IOException {
		
		Logger rootLog = Logger.getLogger("");
		rootLog.setLevel( Level.FINE );
		rootLog.getHandlers()[0].setLevel( Level.FINE ); // Default console handler
		
		SyncConnection con = new SyncConnection("http://localhost/hsync/hsync.php", "test", "test", "test");
		FileSync sync = new FileSync();
		
		File root = new File("target/test");
		delete(root);
		root.mkdirs();
		sync.setRoot(root);
		
		sync.doSync(con);

		sync.doSync(con);

	}

	public static void delete(File local) {
		if (!local.exists()) return;
		if (local.isDirectory()) {
			for (File item : local.listFiles()) {
				if (item.getName().equals(".") || item.getName().equals(".."))
					continue;
				delete(item);
			}
		}
		local.delete();
	}

}
