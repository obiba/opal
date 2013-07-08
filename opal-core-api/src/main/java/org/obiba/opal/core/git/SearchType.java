package org.obiba.opal.core.git;

/**
 * Enumeration of the search types.
	 */
	public enum SearchType {
		AUTHOR, COMMITTER, COMMIT;
	
		public static SearchType forName(String name) {
			for (SearchType type : values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
			return COMMIT;
		}
	
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}