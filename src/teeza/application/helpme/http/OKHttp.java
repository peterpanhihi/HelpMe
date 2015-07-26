package teeza.application.helpme.http;

import java.io.IOException;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class OKHttp {
	private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType
			.parse("text/x-markdown; charset=utf-8");
	private OkHttpClient client;
	private Request request;
	private Response response;

	public OKHttp() {
		client = new OkHttpClient();
	}

	public String GET(String url) throws IOException {
		request = new Request.Builder().url(url).build();
		response = client.newCall(request).execute();
		return response.body().string();
	}

	public String POST(String url, RequestBody body) throws IOException {
		request = new Request.Builder().url(url).post(body).build();
		response = client.newCall(request).execute();
		return response.body().string();
	}
}
