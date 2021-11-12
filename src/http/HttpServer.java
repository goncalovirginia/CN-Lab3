
package http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Super simple incomplete HTTP Server
 */
public class HttpServer {

	private static final int PORT = 8080;
	private static final int BUF_SIZE = 1024;

	private static final String GET = "GET";
	private static final String POST = "POST";

	/**
	 * Returns an input stream with an error message "Not Implemented"
	 */
	static InputStream notImplementedPageStream() {
		final String page = "<HTML><BODY>Request not implemented...</BODY></HTML>";
		int length = page.length();
		StringBuilder reply = new StringBuilder("HTTP/1.0 501 Not Implemented\r\n");
		reply.append("Date: " + new Date().toString() + "\r\n");
		reply.append("Server: " + "The tiny server (v0.1)" + "\r\n");
		reply.append("Content-Length: " + String.valueOf(length) + "\r\n\r\n");
		reply.append(page);
		return new ByteArrayInputStream(reply.toString().getBytes());
	}

	/**
	 * getFile: sends the requested file resource to the client
	 * 
	 */
	static void getFile(String fileName, OutputStream out) throws IOException {
		try {
			FileInputStream fis = new FileInputStream(fileName.substring(1));
			out.write("HTTP/1.0 200 OK\r\n".getBytes());
			out.write("Content-Type: image/jpg\r\n".getBytes());
			out.write("\r\n".getBytes());
			
			int n;
			byte[] buf = new byte[BUF_SIZE];
			
			while ((n = fis.read(buf)) > 0) {
				out.write(buf, 0, n);
			}
		}
		catch (FileNotFoundException e) {
			out.write("HTTP/1.0 404 NOT FOUND".getBytes());
		}
	}

	/**
	 * Receives the requested file resources from the client
	 * 
	 */
	static void postFile(String fileName, InputStream in, OutputStream out) throws IOException {
		// TODO
	}

	/**
	 * Handles one HTTP request
	 * @param in  - stream from client
	 * @param out - stream to client
	 */
	private static void processHttpRequest(InputStream in, OutputStream out) throws IOException {
		StringBuffer request = new StringBuffer();
		String buff;
		
		while (!(buff = new String(in.readNBytes(1))).equals("\n")) {
			request.append(buff);
		}
		
		request.deleteCharAt(request.length()-1);

		System.out.println("received: " + request);

		String[] requestParts = Http.parseHttpRequest(request.toString());
		
		assert requestParts != null;
		String method = requestParts[0].toUpperCase();
		
		switch (method) {
			case GET -> getFile(requestParts[1], out);
			case POST -> postFile(requestParts[1], in, out);
			default -> Http.dumpStream(notImplementedPageStream(), out);
		}
	}

	/**
	 * Accepts and handles client connections
	 */
	public static void main(String[] args) {

		try (ServerSocket ss = new ServerSocket(PORT)) {
			for (;;) {
				System.out.println("Server ready at " + PORT);
				try (Socket clientS = ss.accept()) {
					InputStream in = clientS.getInputStream();
					OutputStream out = clientS.getOutputStream();
					processHttpRequest(in, out);
				} catch (IOException x) {
					x.printStackTrace();
				}
			}
		} catch (IOException x) {
			x.printStackTrace();
		}
	}
}
