package de.mhus.hsync.lib.client;

import java.io.File;
import java.util.logging.Logger;

public class ExtUpdateModifyDate implements ClientExtension {
	
	static Logger log = Logger.getLogger(ExtUpdateModifyDate.class.getName());
	
	@Override
	public boolean isNeedPull(SyncStructure remoteChild, File localChild) {
		return false;
	}

	@Override
	public boolean isStopPull(SyncStructure remoteChild, File localChild) {
		return false;
	}

	@Override
	public void onPostPull(SyncStructure remoteChild, File localChild, boolean pull) {
		if (remoteChild.getModifyDate() / 1000 != localChild.lastModified() / 1000) {
			log.info("m " + remoteChild);
		}
	}

	@Override
	public boolean isStopDelete(File localChild) {
		return false;
	}

	@Override
	public void doInitialize(FileSync fileSync) {
		
	}

}
