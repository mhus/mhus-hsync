package de.mhus.hsync.lib.client;

import java.util.List;

public interface SyncStructure extends SyncProperties {
	boolean isFile();
	boolean isDirectory();
	String getName();
	long getModifyDate();
	List<SyncStructure> getChildren();
	String getPath();
	long getSize();
}
