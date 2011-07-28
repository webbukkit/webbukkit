package org.webbukkit.handler;

import java.util.HashMap;
import java.util.Map;
import org.webbukkit.http.HttpContext;
import org.webbukkit.http.HttpHandler;

public abstract class CachedHttpHandler implements HttpHandler, Cacheable
{
	private final Map<String, ContextCache> cache = new HashMap<String, ContextCache>();
	private final int expires;

	/**
	 * @param expires
	 * Time in milliseconds as long the cache will be used instead of regenerating
	 */
	public CachedHttpHandler(int expires) {
		this.expires = expires;
	}

	@Override
	public void clearCache() {
		final long stale = System.currentTimeMillis() - expires;
		for (final String path : cache.keySet())
			if (cache.get(path).age < stale)
				cache.remove(path);
	}

	@Override
	public void handle(String path, HttpContext context) throws Exception {
		final ContextCache c = cache.get(path);
		if (c == null) {
			rebuildCache(path, context);
			cache.put(path, new ContextCache(context));
		} else if (c.age + expires < System.currentTimeMillis()) {
			rebuildCache(path, context);
			c.context = context;
			c.age = System.currentTimeMillis();
		} else
			context = c.context;
	}

	abstract void rebuildCache(String path, HttpContext context) throws Exception;

	private static class ContextCache
	{
		private long age = 0;
		private HttpContext context;

		ContextCache(HttpContext context) {
			this.context = context;
			age = System.currentTimeMillis();
		}
	}
}
