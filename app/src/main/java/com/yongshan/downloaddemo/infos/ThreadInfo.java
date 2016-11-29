package com.yongshan.downloaddemo.infos;

/**
 * 创建一个下载信息的实体类
 */
public class ThreadInfo {
	private int threadId;// 下载器id
	private long startPos;// 开始点
	private long endPos;// 结束点
	private long downloadedSize;// 完成度
	private String url;// 下载器网络标识

	public ThreadInfo(int threadId, long startPos, long endPos, long downloadedSize, String url) {
		this.threadId = threadId;
		this.startPos = startPos;
		this.endPos = endPos;
		this.downloadedSize = downloadedSize;
		this.url = url;
	}

	public ThreadInfo() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public long getStartPos() {
		return startPos;
	}

	public void setStartPos(long startPos) {
		this.startPos = startPos;
	}

	public long getEndPos() {
		return endPos;
	}

	public void setEndPos(long endPos) {
		this.endPos = endPos;
	}

	public long getDownloadedSize() {
		return downloadedSize;
	}

	public void setDownloadedSize(long downloadedSize) {
		this.downloadedSize = downloadedSize;
	}

	@Override
	public String toString() {
		return "DownloadInfo [threadId=" + threadId + ", startPos=" + startPos + ", endPos=" + endPos
				+ ", downloaded_size=" + downloadedSize + "]";
	}
}
