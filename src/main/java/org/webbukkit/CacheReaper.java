package org.webbukkit;

import java.util.ArrayList;
import java.util.List;
import org.webbukkit.handler.Cacheable;

public class CacheReaper implements Runnable
{
	private final List<Cacheable> cacheables = new ArrayList<Cacheable>();

	public synchronized void addCacheable(Cacheable cacheable) {
		cacheables.add(cacheable);
	}

	public synchronized void removeCacheable(Cacheable cacheable) {
		cacheables.remove(cacheable);
	}

	@Override
	public synchronized void run() {
		for (final Cacheable cacheable : cacheables)
			cacheable.clearCache();
		System.gc();
	}
}
