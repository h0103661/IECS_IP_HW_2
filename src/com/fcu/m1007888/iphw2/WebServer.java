package com.fcu.m1007888.iphw2;

import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class WebServer
{
	/*
	 * ���պ��}
	 * 
	 * �]���S���F��, �ҥH���ӷ|�^��404
	 * http://localhost:6789/
	 * 
	 * ��ܧ@�~������
	 * http://localhost:6789/ip-hw2.htm
	 * 
	 * ��ܹϤ�
	 * http://localhost:6789/img.jpeg
	 * 
	 * ���gif
	 * http://localhost:6789/ip_hw_2.gif
	 * 
	 */
	
	public static void main(String argv[]) throws Exception
	{
		// Set the port number.
		int port = 6789;
		
		// Establish the listen socket.
		// �o��socket�u�������{���ɤ~�ݭn����,�[�o�Ӱ���ĵ�i
		@SuppressWarnings("resource")
		ServerSocket socket = new ServerSocket(port);
		
		// Process HTTP service requests in an infinite loop.
		while (true) {
			// Listen for a TCP connection request.
			
			// Construct an object to process the HTTP request message.
			// server socket �����쪺 client socket
			HttpRequest request = new HttpRequest(socket.accept());

			// Create a new thread to process the request.
			Thread thread = new Thread(request);

			// Start the thread.
			thread.start();
		}
	}
}

final class HttpRequest implements Runnable
{
	final static String CRLF = "\r\n";
	Socket socket;

	// Constructor
	public HttpRequest(Socket socket) throws Exception 
	{
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void processRequest() throws Exception
	{
		// Get a reference to the socket's input and output streams.
		// ���}socket��input��output
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		// Set up input stream filters.
		// Ū��socket��input 
		BufferedReader br =  new BufferedReader(new InputStreamReader(is));
		
		// Get the request line of the HTTP request message.
		String requestLine = br.readLine();

		// Display the request line.
		System.out.println();
		System.out.println(requestLine);
		
		// Extract the filename from the request line.
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken();  // skip over the method, which should be "GET"
		String fileName = tokens.nextToken();
		
		// Prepend a "." so that file request is within the current directory.
		fileName = "." + fileName;
		
		// Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}
		
		// Construct the response message.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists) {
			statusLine = "HTTP/1.1" + " " + "200" + " " + "OK"; // �^�Ǫ��A, �ѦҲĤG����v��p.35
			contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;
		} else {
			statusLine = "HTTP/1.1" + " " + "404" + " " + "Not Found";
			contentTypeLine = "Content-type: " + "text/html" + CRLF; // 404���|�^�ǭn�D���ɮצӬO��ܿ��~�T��������, �ҥH�o��type�令����
			entityBody = "<HTML>" + 
				"<HEAD><TITLE>Not Found</TITLE></HEAD>" +
				"<BODY>Not Found</BODY></HTML>";
		}
		
		// Send the status line.
		os.writeBytes(statusLine);

		// Send the content type line.
		os.writeBytes(contentTypeLine);

		// Send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);
		
		// Send the entity body.
		if (fileExists)	{
			sendBytes(fis, os);
			fis.close();
		} else {
			os.writeBytes(entityBody);
		}
		
		// Get and display the header lines.
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}
		
		// Close streams and socket.
		os.close();
		br.close();
		socket.close();
	}
	
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
	{
	     // Construct a 1K buffer to hold bytes on their way to the socket.
	     byte[] buffer = new byte[1024];
	     int bytes = 0;
	
	     // Copy requested file into the socket's output stream.
	     while((bytes = fis.read(buffer)) != -1 ) {
	        os.write(buffer, 0, bytes);
	     }
	}
	
	// �ھڧ@�~�����˼ƲĤG�q, �Ϥ����O image ����
	private static String contentType(String fileName)
	{
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if(fileName.endsWith(".gif")) { //GIF
			return "image/gif";
		}
		if(fileName.endsWith(".jpeg")) { //JPEG
			return "image/jpeg";
		}
		return "application/octet-stream";
	}
}
