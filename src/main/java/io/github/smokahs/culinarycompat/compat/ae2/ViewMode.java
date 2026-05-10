package io.github.smokahs.culinarycompat.compat.ae2;

// cosmetic only!
public enum ViewMode {
	NORMAL, OFF;

	public ViewMode cycle() {
		return this == NORMAL ? OFF : NORMAL;
	}

	public static ViewMode fromName(String n) {
		try {
			return ViewMode.valueOf(n);
		} catch (Exception e) {
			return NORMAL;
		}
	}
}
