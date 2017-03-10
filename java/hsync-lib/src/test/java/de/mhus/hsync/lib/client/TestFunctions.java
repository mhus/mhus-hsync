package de.mhus.hsync.lib.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mashape.unirest.http.JsonNode;

public class TestFunctions {

	public static void main(String[] args) throws IOException {
		SyncConnection con = new SyncConnection("http://localhost/hsync/hsync.php", "test", "test", "test");
		
		SyncMetadata meta = con.getMetadata();
		System.out.println(meta.getData());
		System.out.println(con.getMessage());
		
		SyncStructure struct = con.getStructure(null, null, null);
		System.out.println(struct.getData());
		System.out.println(con.getMessage());

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if (con.getFile("animal/links.txt", os)) {
			System.out.println(new String(os.toByteArray()));
		} else {
			System.out.println("Error reading file");
		}

		System.out.println(con.getMessage());

		
		
		if (con.getFiles(new FileCallback() {
			
			@Override
			public void foundFile(String name, InputStream is) {
				System.out.println("Zip: " + name);
			}
		}, "animal/links.txt" )) {
			System.out.println("Zip loaded");
		} else {
			System.out.println("Zip error");
		}

		System.out.println(con.getMessage());
		
	}

}
