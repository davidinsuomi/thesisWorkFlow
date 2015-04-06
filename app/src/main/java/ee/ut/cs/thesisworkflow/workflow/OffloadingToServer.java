package ee.ut.cs.thesisworkflow.workflow;


import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ding on 2/1/2015.
 */
public class OffloadingToServer {
    private static void copyStream(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public void PostBPELtoServer(String url, InputStream fileToOffloadingStream) {
//		String boundary = Long.toHexString(System.currentTimeMillis()); // get generate some random value
//		String CRLF = "\r\n"; //Line separator required by multipart/form-data.
//		URLConnection connection = new URL(url).openConnection();
//		connection.setDoOutput(true);
//		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//		OutputStream output = null;
//		PrintWriter writer = null;
//		File binaryFile = null;
//		try{
//			output = connection.getOutputStream();
//			writer = new PrintWriter(new OutputStreamWriter(output,"UTF-8"),true);
//			// Send binary file.
//			binaryFile = createFileFromInputStream(fileToOffloadingStream);
//		    writer.append("--" + boundary).append(CRLF);
//		    writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
//		    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
//		    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
//		    writer.append(CRLF).flush();
//		    copyStream(fileToOffloadingStream,output);
//		    output.flush(); // Important before continuing with writer!
//		    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
//
//		    // End of multipart/form-data.
//		    writer.append("--" + boundary + "--").append(CRLF).flush();
//		}finally {
//		    if (writer != null) writer.close();
//		}
//		// Connection is lazily executed whenever you request any status.
//		int responseCode = ((HttpURLConnection) connection).getResponseCode();
//		System.out.println(responseCode);
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String pathToOurFile = "/bpel.zip";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            URL mUrl = new URL(url);
            connection = (HttpURLConnection) mUrl.openConnection();
            // Allow Inputs &amp; Outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            // Set HTTP method to POST.
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileToOffloadingStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            //Read File
            bytesRead = fileToOffloadingStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileToOffloadingStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileToOffloadingStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            Log.e("TAG", serverResponseMessage);
            fileToOffloadingStream.close();
            outputStream.flush();
            outputStream.close();

        } catch (Exception ex) {
            //Exception handling
        }

    }

    private File createFileFromInputStream(InputStream inputStream) {
        try {
            File f = new File("taskToOffLoading.zip");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return f;
        } catch (IOException e) {

        }
        return null;
    }
}
