package org.celllife.pconfig.model;
public enum FileType {
		PDF(".pdf"),
		XML(".xml"),
		CSV(".csv"),
		TXT(".txt"),
		HTML(".html");

		private final String extension;
		private FileType(String extension){
			this.extension = extension;
		}
		
		public String getExtension() {
			return extension;
		}

	}