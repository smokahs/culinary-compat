package com.hoshihoku.culinarycompat.bridges;

import java.util.Set;

public enum BridgeKind {
	CUTTINGBOARD("cuttingboard", Set.of("pam_cuttingboard", "fd_cutting")), SKILLET("skillet",
			Set.of("pam_skillet")), OVEN("oven", Set.of("campfire_oven")), POT("pot",
					Set.of("pam_pot", "fd_cooking")), BAKEWARE("bakeware", Set.of("pam_bakeware"));

	public final String path;
	public final Set<String> sources;

	BridgeKind(String path, Set<String> sources) {
		this.path = path;
		this.sources = sources;
	}

	public static BridgeKind fromSource(String source) {
		for (BridgeKind k : values()) {
			if (k.sources.contains(source))
				return k;
		}
		return null;
	}
}
