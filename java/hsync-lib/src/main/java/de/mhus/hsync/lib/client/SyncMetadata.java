package de.mhus.hsync.lib.client;

public interface SyncMetadata extends SyncProperties {

	String getName();
	String getDescription();
	String[] getExtensions();
	String[] getFunctions();
	int getVersion();
}
