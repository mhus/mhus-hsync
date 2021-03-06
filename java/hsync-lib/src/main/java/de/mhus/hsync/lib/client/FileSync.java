package de.mhus.hsync.lib.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Logger;

public class FileSync {

	private static Logger log = Logger.getLogger(FileSync.class.getCanonicalName());
	protected File root = null;
	protected boolean delete = false;
	protected boolean overwriteAll = false;
	private boolean test = true;
	private boolean hidden = false;
	private boolean createLinks = true;
	private int version;
	private long bytesPulled = 0;
	private long bytesPushed = 0;
	private long bytesDeleted = 0;
	private long filesPulled = 0;
	private long filesPushed = 0;
	private long filesDeleted = 0;
	
	private LinkedList<ClientExtension> extensions = new LinkedList<>();
	private Properties properties;
	
	
	public void doPull(SyncConnection con, String path) throws IOException {
		if (root == null || !root.exists()) throw new IOException("root not found: " + root);
		
		SyncMetadata meta = con.getMetadata();
		if (meta == null) throw new IOException("Can't connect to remote: " + con);
		version = meta.getVersion();
		
		log.fine(">>> Start sync from " +  con + " to " + root);
		
		SyncStructure remoteRoot = con.getStructure(path, null, null);
		
		pullDirectory(path, con, remoteRoot, root);
		
		log.info("Pulled : " + filesPulled + ", " + bytesPulled + " Bytes");
		log.info("Deleted: " + filesDeleted + ", " + bytesDeleted + " Bytes");
		log.fine("<<< End");
		
	}
	
	private void pullDirectory(String path, SyncConnection con, SyncStructure remote, File local) throws IOException {
		if (!remote.isDirectory()) {
			log.fine("*** Remote is not a directory: " + remote.getPath());
			return;
		}
		
		if (createLinks && remote.isLink()) {
			if (local.exists()) {
				if (Files.isSymbolicLink(local.toPath())) {
					Path localTarget = Files.readSymbolicLink(local.toPath());
					if (!localTarget.toString().equals(remote.getLinkTaget())) {
						log.info("- l 0 " + remote.getPath());
						local.delete();
					} else
						return; // do not look insinde the linked directory
				} else
				if (delete) {
					log.info("- " + (local.isDirectory() ? "d 0 " : "f " + local.length() + " ") + remote.getPath());
					delete(local);
				} else {
					log.info("*** Can't change link: " + local);
					return;
				}
			}
			if (!local.exists()) {
				log.info("+ l 0 " + remote.getPath() + " -> " + remote.getLinkTaget());
				Files.createSymbolicLink(local.toPath(), new File( remote.getLinkTaget() ).toPath() );
				return;
			}
		}
		
		
		if (!local.exists()) {
			log.info("+ d 0 " + remote);
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
				pullDirectory(path, con, remoteChild, localChild);
			} else {
				log.finer("--- Synchronize File: " + remoteChild);
				
				boolean pull = false;
				for (ClientExtension ext : extensions) {
					if (ext.isNeedPull(remoteChild, localChild)) {
						log.finer("need pull: " + ext);
						pull = true;
						break;
					}
				}
				
				if ( overwriteAll || pull) {
					pull = true;
					if (overwriteAll)
						log.finer("pull by overwrite");
				}

				if (pull) {
					for (ClientExtension ext : extensions) {
						if (ext.isStopPull(remoteChild, localChild)) {
							log.finer("stop pull: " + ext);
							pull = false;
							break;
						}
					}
				}
				
				if (pull) {
										
					if (localChild.exists() && !localChild.isFile()) {
						log.fine("*** Can't update file, it's not a file: " + localChild);
					} else {
						log.info("+ f " + remoteChild.getSize() + " " + remoteChild.getPath());
						log.fine("Save to " + localChild.getAbsolutePath());
						bytesPulled+=remoteChild.getSize();
						filesPulled++;
						FileOutputStream os = new FileOutputStream(localChild);
						if (!con.getFile( (path == null ? "" : path) + remoteChild.getPath(), os)) {
							log.warning("*** Update failed: " + remoteChild);
						}
						os.close();
						localChild.setLastModified( remoteChild.getModifyDate() );
						if (localChild.length() != remoteChild.getSize()) {
							log.warning("*** Updated but different size: " + remoteChild + " R: " + remoteChild.getSize() + " L: " + localChild.length() + " " + localChild.getAbsolutePath());
						}
					}
				
					pull = true;
				}
				
				for (ClientExtension ext : extensions) {
					ext.onPostPull(remoteChild,localChild, pull);
				}
				
			}
		}
		
		if (delete) {
			for (String item : localList) {
				File localChild = new File(local,item);
				if (localChild.exists()) {

					for (ClientExtension ext : extensions) {
						if (ext.isStopDelete(localChild)) {
							log.finer("stop delete: " + ext);
							continue;
						}
					}
					
					log.info("- " + (localChild.isDirectory() ? "d 0 " : "f " + localChild.length() + " ") + remote + "/" + item);
					delete(localChild);
				}
			}
		}
		
	}

	public void delete(File local) {
		if (!local.exists()) return;
		if (local.isDirectory()) {
			for (File item : local.listFiles()) {
				if (item.getName().equals(".") || item.getName().equals(".."))
					continue;
				delete(item);
			}
		}
		if (local.isFile()) {
			bytesDeleted+=local.length();
			filesDeleted++;
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

	public boolean isOverwriteAll() {
		return overwriteAll;
	}


	public void setOverwriteAll(boolean overwrite) {
		this.overwriteAll = overwrite;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	public boolean isCreateLinks() {
		return createLinks;
	}

	public void setCreateLinks(boolean createLinks) {
		this.createLinks = createLinks;
	}
	
	public void addExtension(ClientExtension extension) {
		extensions.add(extension);
	}
	
	public void doInitialize(Properties props) {
		this.properties = props;
		log.fine("Initialize with: " + props);
		setDelete(Boolean.valueOf(props.getProperty("delete", "false")));
		setCreateLinks(Boolean.valueOf(props.getProperty("createlinks", "true")));
		setOverwriteAll(Boolean.valueOf(props.getProperty("overwrite", "false")));
		setTest(Boolean.valueOf(props.getProperty("test", "false")));

		for (ClientExtension ext : extensions) {
			log.fine("Initialize Extension: " + ext.getClass());
			ext.doInitialize(this);
		}

	}

	public Properties getProperties() {
		return properties;
	}
	
}
