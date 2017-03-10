package de.mhus.hsync.lib.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Logger;

public class FileSync {

	private static Logger log = Logger.getLogger(FileSync.class.getCanonicalName());
	protected File root = null;
	protected boolean delete = false;
	protected boolean versions = false;
	protected boolean overwrite = true;
	private boolean test = true;
	private boolean hidden = false;
	private boolean checkSize = false;
	private boolean checkModified = true;
	private int version;
	
	public void doSync(SyncConnection con) throws IOException {
		if (root == null || !root.exists()) throw new IOException("root not found: " + root);
		
		SyncMetadata meta = con.getMetadata();
		if (meta == null) throw new IOException("Can't connect to remote: " + con);
		version = meta.getVersion();
		
		log.fine(">>> Start sync from " +  con + " to " + root);
		
		SyncStructure remoteRoot = con.getStructure(null, null, null);
		
		syncDirectory(con, remoteRoot, root);
		
		log.fine("<<< End");
		
	}
	
	private void syncDirectory(SyncConnection con, SyncStructure remote, File local) throws IOException {
		if (!remote.isDirectory()) {
			log.fine("*** Remote is not a directory: " + remote.getPath());
			return;
		}
		
		if (!local.exists()) {
			log.info("+ d " + remote);
			local.mkdirs();
		}
		
		if (!local.exists() || !local.isDirectory()) {
			log.fine("*** Local is not a directory: " + local);
			return;
		}
		
		LinkedList<String> localList = new LinkedList<>();
		if (delete) {
			for (String item : local.list()) {
				if (item.equals(".") || item.equals("..") || item.equals(".hsync.properties"))
					continue;
				if (!hidden  && item.startsWith("."))
					continue;
				localList.add(item);
			}
		}
		
		for (SyncStructure remoteChild : remote.getChildren()) {
			File localChild = new File(local,remoteChild.getName());
			localList.remove(remoteChild.getName());
			if (remoteChild.isDirectory()) {
				log.finer("--- Synchronize Directory: " + remoteChild);
				syncDirectory(con, remoteChild, localChild);
			} else {
				log.finer("--- Synchronize File: " + remoteChild);
				if (	overwrite || 
						!localChild.exists() || 
						(checkSize && localChild.length() != remoteChild.getSize() ) || 
						(checkModified && localChild.lastModified() != remoteChild.getModifyDate() ) 
					) {
					
					if (localChild.exists() && !localChild.isFile()) {
						log.fine("*** Can't update file, it's not a file: " + localChild);
					} else {
						log.info("+ f " + remoteChild);
						FileOutputStream os = new FileOutputStream(localChild);
						if (!con.getFile(remoteChild.getPath(), os)) {
							log.fine("*** Update failed: " + remoteChild);
						}
						os.close();
						localChild.setLastModified( remoteChild.getModifyDate() );
						if (localChild.length() != remoteChild.getSize()) {
							log.fine("*** Updated but different size: " + remoteChild);
						}
					}
					
				}
				
			}
		}
		
		if (delete) {
			for (String item : localList) {
				File localChild = new File(local,item);
				if (localChild.exists()) {
					log.info("- " + (localChild.isDirectory() ? "d" : "f") + " " + remote + "/" + item);
					delete(localChild);
				}
			}
		}
		
	}

	public static void delete(File local) {
		if (!local.exists()) return;
		if (local.isDirectory()) {
			for (File item : local.listFiles()) {
				if (item.getName().equals(".") || item.getName().equals(".."))
					continue;
				delete(item);
			}
		}
		local.delete();
	}

	public File getRoot() {
		return root;
	}


	public void setRoot(File root) {
		this.root = root;
	}


	public boolean isDelete() {
		return delete;
	}


	public void setDelete(boolean delete) {
		this.delete = delete;
	}


	public boolean isVersions() {
		return versions;
	}


	public void setVersions(boolean versions) {
		this.versions = versions;
	}


	public boolean isOverwriteAll() {
		return overwrite;
	}


	public void setOverwriteAll(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	public boolean isCheckSize() {
		return checkSize;
	}

	public void setCheckSize(boolean checkSize) {
		this.checkSize = checkSize;
	}

	public boolean isCheckModified() {
		return checkModified;
	}

	public void setCheckModified(boolean checkModified) {
		this.checkModified = checkModified;
	}
	
}
