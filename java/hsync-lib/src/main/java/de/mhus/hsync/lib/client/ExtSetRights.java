package de.mhus.hsync.lib.client;

import java.io.File;

public class ExtSetRights implements ClientExtension {

	@Override
	public boolean isNeedPull(FileSync fileSync, SyncStructure remoteChild, File localChild) {
		return false;
	}

	@Override
	public boolean isStopPull(FileSync fileSync, SyncStructure remoteChild, File localChild) {
		return false;
	}

	@Override
	public void onPostPull(FileSync fileSync, SyncStructure remoteChild, File localChild, boolean pull) {
		String rights = (String)remoteChild.get("rights");
		if (rights != null) {
			// check and update
		}
	}

	@Override
	public boolean isStopDelete(FileSync fileSync, File localChild) {
		return false;
	}

}
