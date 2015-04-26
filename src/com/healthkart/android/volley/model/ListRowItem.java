package com.healthkart.android.volley.model;

public class ListRowItem {
	private String title, thumbnailUrl;
	private String MRP;
	private String discount;
	private String price;

	public ListRowItem() {
	}

	public ListRowItem(String title, String thumbnailUrl, String MRP, String discount,
			String price) {
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
		this.MRP = MRP;
		this.discount = discount;
		this.price = price;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getMRP() {
		return MRP;
	}

	public void setMRP(String MRP) {
		this.MRP = MRP;
	}

	public String getDiscount() {
		return discount;
	}

	public void setDiscount(String discount) {
		this.discount = discount;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}
}
