package org.obiba.opal.rest.client.magma;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UriBuilder {

	private URI root;

	private List<String> pathSegments = new LinkedList<String>();

	private Map<String, String> query = new LinkedHashMap<String, String>();

	UriBuilder(URI root) {
		this.root = root;
	}

	UriBuilder(UriBuilder builder) {
		this.root = builder.root;
		this.pathSegments.addAll(builder.pathSegments);
		this.query.putAll(builder.query);
	}

	public UriBuilder segment(String... segments) {
		pathSegments.addAll(Arrays.asList(segments));
		return this;
	}

	public UriBuilder query(String... keyvalue) {
		for (int i = 0; i < keyvalue.length; i += 2) {
			query(keyvalue[i], keyvalue[i + 1]);
		}
		return this;
	}

	public UriBuilder query(String parameter, String value) {
		query.put(parameter, value);
		return this;
	}

	public UriBuilder newBuilder() {
		return new UriBuilder(this);
	}

	public URI build() {
		try {
			return root.resolve(new URI(null, null, path(),
					hasQuery() ? query() : null, null));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean hasQuery() {
		return this.query.size() > 0;
	}

	private String query() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : query.entrySet()) {
			sb.append(e.getKey()).append('=')
					.append(e.getValue() != null ? e.getValue() : "")
					.append('&');
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();

	}

	private String path() {
		StringBuilder sb = new StringBuilder(pathSegments.size() > 0 ? pathSegments.get(0) : "");
		for (int i = 1; i < this.pathSegments.size(); i++) {
			sb.append('/').append(pathSegments.get(i));
		}
		return sb.toString();
	}

}
