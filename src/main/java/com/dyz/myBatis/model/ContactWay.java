package com.dyz.myBatis.model;

public class ContactWay {
    private Integer id;

    private String content;
    
    private Integer jiaguo;
    
    

    public Integer getJiaguo() {
		return jiaguo;
	}

	public void setJiaguo(Integer jiaguo) {
		this.jiaguo = jiaguo;
	}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }
}