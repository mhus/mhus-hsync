package de.mhus.hsync.lib.client;

import java.io.File;

public interface ClientExtension {

	boolean isNeedPull(FileSync fileSync, SyncStructure remoteChild, File localChild);

	boolean isStopPull(FileSync fileSync, SyncStructure remoteChild, File localChild);
	
	void onPostPull(FileSync fileSync, SyncStructure remoteChild, File localChild, boolean pull);

	boolean isStopDelete(FileSync fileSync, File localChild);

}
