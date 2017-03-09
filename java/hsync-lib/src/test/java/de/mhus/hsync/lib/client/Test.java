package de.mhus.hsync.lib.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mashape.unirest.http.JsonNode;

public class Test {

	public static void main(String[] args) throws IOException {
		SyncConnection con = new SyncConnection("http://localhost/hsync/hsync.php", "test", "test", "test");
		
		JsonNode meta = con.getMetadata();
		System.out.println(meta);
		System.out.println(con.getMessage());
		
		JsonNode struct = con.getStructure(null, null, null);
		System.out.println(struct);
		System.out.println(con.getMessage());

		InputStream file = con.getFile("animal/links.txt");
		if (file != null) {
			StringBuffer o = new StringBuffer();
			while (true) {
				int i = file.read();
				if (i < 0) break;
				o.append((char)i);
			}
			System.out.println(o);
		}

		System.out.println(con.getMessage());

	}

}
