package org.obiba.opal.rest.client.magma;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import com.google.protobuf.Message;

/**
 * A Java client for Opal RESTful services.
 */
public class OpalJavaClient {

	private final URI opalURI;

	private final HttpClient client;

	private final BasicHttpContext ctx;

	private final Credentials credentials;

	public OpalJavaClient(String uri, String username, String password)
			throws URISyntaxException {
		if (uri == null)
			throw new IllegalArgumentException("uri cannot be null");
		if (username == null)
			throw new IllegalArgumentException("username cannot be null");
		if (password == null)
			throw new IllegalArgumentException("password cannot be null");

		this.opalURI = new URI(uri.endsWith("/") ? uri : uri + "/");

		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(
				AuthScope.ANY,
				credentials = new UsernamePasswordCredentials(username,
						password));
		httpClient.getParams().setParameter(
				"http.protocol.handle-authentication", Boolean.TRUE);
		httpClient.getParams().setParameter("http.auth.target-scheme-pref",
				Collections.singletonList(OpalAuthScheme.NAME));
		httpClient.getAuthSchemes().register(OpalAuthScheme.NAME,
				new OpalAuthScheme.Factory());
		ctx = new BasicHttpContext();
		ctx.setAttribute(ClientContext.AUTH_SCHEME_PREF,
				Collections.singletonList(OpalAuthScheme.NAME));
		ctx.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
		this.client = httpClient;

	}

	public UriBuilder newUri() {
		return new UriBuilder(this.opalURI);
	}

	public UriBuilder newUri(URI root) {
		String rootPath = root.getPath();
		if (rootPath.endsWith("/") == false) {
			try {
				return new UriBuilder(new URI(root.getScheme(), root.getHost() + ":"+ root.getPort(),
						rootPath + "/", root.getQuery(), root.getFragment()));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new UriBuilder(root);
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> List<T> getResources(Class<T> messageType,
			URI uri, Message.Builder builder) {
		ArrayList<T> resources = new ArrayList<T>();
		InputStream is = null;
		Message.Builder messageBuilder = builder;

		try {
			HttpResponse response = get(uri);
			is = response.getEntity().getContent();

			while (messageBuilder.mergeDelimitedFrom(is)) {
				T message = (T) messageBuilder.build();
				resources.add(message);
				messageBuilder = message.newBuilderForType();
			}
			return resources;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			closeQuietly(is);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> T getResource(Class<T> messageType, URI uri,
			Message.Builder builder) {
		InputStream is = null;
		try {
			HttpResponse response = get(uri);
			is = response.getEntity().getContent();
			return (T) builder.mergeFrom(is).build();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			closeQuietly(is);
		}
	}

	public HttpResponse put(URI uri, Message msg)
			throws ClientProtocolException, IOException {
		HttpPut put = new HttpPut(uri);
		put.setEntity(new ByteArrayEntity(asByteArray(msg)));
		return execute(put);
	}

	public HttpResponse post(URI uri, Message msg)
			throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(uri);
		ByteArrayEntity e = new ByteArrayEntity(asByteArray(msg));
		e.setContentType("application/x-protobuf");
		post.setEntity(e);
		return execute(post);
	}

	public HttpResponse post(URI uri, Iterable<? extends Message> msg)
			throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(uri);
		ByteArrayEntity e = new ByteArrayEntity(asByteArray(msg));
		e.setContentType("application/x-protobuf");
		post.setEntity(e);
		return execute(post);
	}


	public HttpResponse post(URI uri, String entity)
			throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(uri);
		post.setEntity(new StringEntity(entity));
		return execute(post);
	}

	public HttpResponse post(URI uri, File file)
			throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(uri);
		MultipartEntity me = new MultipartEntity();
		me.addPart("fileToUpload", new FileBody(file));
		post.setEntity(me);
		return execute(post);
	}

	public HttpResponse get(URI uri) throws ClientProtocolException,
			IOException {
		return execute(new HttpGet(uri));
	}

	private HttpResponse execute(HttpUriRequest msg)
			throws ClientProtocolException, IOException {
		msg.addHeader("Accept", "application/x-protobuf, text/html");
		authenticate(msg);
		return client.execute(msg, ctx);
	}

	private void authenticate(HttpMessage msg) {
		msg.addHeader(OpalAuthScheme.authenticate(credentials,
				AuthParams.getCredentialCharset(msg.getParams()), false));
	}

	private void closeQuietly(Closeable closable) {
		if (closable == null)
			return;
		try {
			closable.close();
		} catch (Throwable t) {
		}
	}

	private byte[] asByteArray(Message msg) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			msg.writeTo(baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			closeQuietly(baos);
		}
	}
	private byte[] asByteArray(Iterable<? extends Message> msgs) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			for(Message msg : msgs) {
				msg.writeDelimitedTo(baos);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			closeQuietly(baos);
		}
	}

}
