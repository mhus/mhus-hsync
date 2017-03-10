package de.mhus.hsync.lib.client;

import java.io.InputStream;

public interface FileCallback {

	void foundFile(String name, InputStream is);

}
