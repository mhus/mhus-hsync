package de.mhus.hsync.lib.client;

import java.io.File;

public class ExtCheckSize implements ClientExtension {

	@Override
	public boolean isNeedPull(FileSync fileSync, SyncStructure remoteChild, File localChild) {
		return localChild.length() != remoteChild.getSize();
	}

	@Override
	public boolean isStopPull(FileSync fileSync, SyncStructure remoteChild, File localChild) {
		return false;
	}

	@Override
	public void onPostPull(FileSync fileSync, SyncStructure remoteChild, File localChild, boolean pull) {
		
	}

	@Override
	public boolean isStopDelete(FileSync fileSync, File localChild) {
		return false;
	}

}
