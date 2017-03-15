package de.mhus.hsync.lib.client;

import java.io.File;

public interface ClientExtension {

	void doInitialize(FileSync fileSync);
	
	boolean isNeedPull(SyncStructure remoteChild, File localChild);

	boolean isStopPull(SyncStructure remoteChild, File localChild);
	
	void onPostPull(SyncStructure remoteChild, File localChild, boolean pull);

	boolean isStopDelete(File localChild);

}
