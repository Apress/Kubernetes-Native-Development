package com.apress.kubdev.rss;

import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FeedItem {
	private String title;
	private String link;
	private String description;
	
	public FeedItem() {
		super();
	}
	
	public FeedItem(String title, String link, String description) {
		super();
		this.title = title;
		this.link = link;
		this.description = description;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public int hashCode() {
		return Objects.hash(description, link, title);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FeedItem other = (FeedItem) obj;
		return Objects.equals(description, other.description) && Objects.equals(link, other.link)
				&& Objects.equals(title, other.title);
	}
	@Override
	public String toString() {
		return String.format("FeedItem [title=%s, link=%s, description=%s]", title, link, description);
	}

}
