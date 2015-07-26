package teeza.application.helpme.http;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.StrictMode;
import android.util.Log;

public class HttpFileUpload implements Runnable {
	String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK)
			.format(new Date());
	URL connectURL;
	String responseString;
	String Title;
	String eclaim_detail;
	byte[] dataToServer;
	FileInputStream fileInputStream = null;
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	public HttpFileUpload(String urlString, String vTitle, String vDesc) {
		try {
			connectURL = new URL(urlString);
			Title = vTitle;
			eclaim_detail = vDesc;
		} catch (Exception ex) {
			Log.i("HttpFileUpload", "URL Malformatted");
		}
	}

	public int Send_Now(FileInputStream fStream, String ID) {
		fileInputStream = fStream;
		int success = Sending(ID);
		return success;
	}

	int Sending(String ID) {
		StrictMode.setThreadPolicy(policy);
		String iFileName = "_" + ID + "_" + timeStamp + ".png";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String Tag = "fSnd";
		try {
			Log.e(Tag, "Starting Http File Sending to URL");

			// Open a HTTP connection to the URL
			HttpURLConnection conn = (HttpURLConnection) connectURL
					.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");

			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"title\""
					+ lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes(Title);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);

			dos.writeBytes("Content-Disposition: form-data; name=\"description\""
					+ lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes(eclaim_detail);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);

			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
					+ iFileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			Log.e(Tag, "File are written");

			// create a buffer of maximum size
			int bytesAvailable = fileInputStream.available();

			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];

			// read file and write it into form...
			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			fileInputStream.close();

			dos.flush();

			Log.e(Tag,
					"File Sended, Response: "
							+ String.valueOf(conn.getResponseCode()));

			InputStream is = conn.getInputStream();

			// retrieve the response from server
			int ch;

			StringBuffer b = new StringBuffer();
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			String s = b.toString();
			Log.i("Response", s);
			dos.close();
			return 1;
		} catch (MalformedURLException ex) {
			Log.e(Tag, "URL error: " + ex.getMessage(), ex);
			return 0;
		}

		catch (IOException ioe) {
			Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
			return 0;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}
}