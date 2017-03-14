package de.mhus.hsync.lib.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ExtUnixPerms implements ClientExtension {

	static Logger log = Logger.getLogger(ExtUnixPerms.class.getName());

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
		String perms = (String)remoteChild.get("perms");
		if (perms != null && perms.length() == 10) {
			// check and update
			try {
				Set<PosixFilePermission> localPerms = Files.getPosixFilePermissions(localChild.toPath());
				Set<PosixFilePermission> newPerms = new HashSet<>();
				boolean diff = false;
				
				if (perms.charAt(1) == 'r') {
					newPerms.add(PosixFilePermission.OTHERS_READ);
				}
				if (perms.charAt(2) == 'w') {
					newPerms.add(PosixFilePermission.OTHERS_WRITE);
				}
				if (perms.charAt(3) == 'x') {
					newPerms.add(PosixFilePermission.OTHERS_EXECUTE);
				}

				if (perms.charAt(4) == 'r') {
					newPerms.add(PosixFilePermission.GROUP_READ);
				}
				if (perms.charAt(5) == 'w') {
					newPerms.add(PosixFilePermission.GROUP_WRITE);
				}
				if (perms.charAt(6) == 'x') {
					newPerms.add(PosixFilePermission.GROUP_EXECUTE);
				}

				if (perms.charAt(7) == 'r') {
					newPerms.add(PosixFilePermission.OWNER_READ);
				}
				if (perms.charAt(8) == 'w') {
					newPerms.add(PosixFilePermission.OWNER_WRITE);
				}
				if (perms.charAt(9) == 'x') {
					newPerms.add(PosixFilePermission.OWNER_EXECUTE);
				}

				if (localPerms.containsAll(newPerms) && newPerms.containsAll(localPerms))
					return;
				
				log.info("x " + perms + " " + remoteChild);
				Files.setPosixFilePermissions(localChild.toPath(), newPerms);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isStopDelete(FileSync fileSync, File localChild) {
		return false;
	}

}
