package io.github.smokahs.culinarycompat.compat.ae2;

// direction the ME kitchen station bridges items between an AE2 grid and a CFB kitchen multiblock
public enum Mode {
	// AE2 grid items visible/usable inside CFB kitchen recipes (original
	// AppliedCooking behavior)
	AE_TO_CFB,
	// CFB kitchen storage exposed as inventory on the linked AE2 grid (storage-bus
	// style)
	CFB_TO_AE,
	// both active
	BIDIRECTIONAL;

	public boolean allowsAEToCFB() {
		return this == AE_TO_CFB || this == BIDIRECTIONAL;
	}

	public boolean allowsCFBToAE() {
		return this == CFB_TO_AE || this == BIDIRECTIONAL;
	}

	public Mode cycle() {
		return switch (this) {
			case AE_TO_CFB -> CFB_TO_AE;
			case CFB_TO_AE -> BIDIRECTIONAL;
			case BIDIRECTIONAL -> AE_TO_CFB;
		};
	}

	public static Mode fromName(String name) {
		try {
			return Mode.valueOf(name);
		} catch (Exception e) {
			return BIDIRECTIONAL;
		}
	}
}
