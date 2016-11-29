package com.yongshan.downloaddemo.infos;

import java.io.Serializable;

public class FileItemInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private String url;
	private String fileName;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	public FileItemInfo(String url, String fileName) {
		super();
		this.url = url;
		this.fileName = fileName;
	}

	public String getUrl() {
		return url;
	}

	public String getFileName() {
		return fileName;
	}

}
