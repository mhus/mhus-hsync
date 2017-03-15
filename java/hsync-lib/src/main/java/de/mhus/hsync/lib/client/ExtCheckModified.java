package de.mhus.hsync.lib.client;

import java.io.File;

public class ExtCheckModified implements ClientExtension {

	@Override
	public boolean isNeedPull(SyncStructure remoteChild, File localChild) {
		return localChild.lastModified()/1000 != remoteChild.getModifyDate()/1000;
	}

	@Override
	public boolean isStopPull(SyncStructure remoteChild, File localChild) {
		return false;
	}

	@Override
	public void onPostPull(SyncStructure remoteChild, File localChild, boolean pull) {
	}

	@Override
	public boolean isStopDelete(File localChild) {
		return false;
	}

	@Override
	public void doInitialize(FileSync fileSync) {
		
	}

}
