package com.example.finalproject;

public class Topic
{
	private int topicId;
	private int accId;
	private int comments;
	private String username;
	private String topicTitle;
	private String topicBody;
	private String dateTime;

	public Topic()
	{

	}

	public Topic(int topicId, int accId, String topicTitle, String topicBody, String dateTime)
	{
		this.topicId = topicId;
		this.accId = accId;
		this.topicTitle = topicTitle;
		this.topicBody = topicBody;
		this.dateTime = dateTime;
	}

	public Topic(int topicId, int accId, int comments, String username, String topicTitle, String topicBody, String dateTime)
	{
		this.topicId = topicId;
		this.accId = accId;
		this.comments = comments;
		this.username = username;
		this.topicTitle = topicTitle;
		this.topicBody = topicBody;
		this.dateTime = dateTime;
	}

	public int getTopicId()
	{
		return topicId;
	}

	public void setTopicId(int topicId)
	{
		this.topicId = topicId;
	}

	public int getAccId()
	{
		return accId;
	}

	public void setAccId(int accId)
	{
		this.accId = accId;
	}

	public int getComments()
	{
		return comments;
	}

	public void setComments(int comments)
	{
		this.comments = comments;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getTopicTitle()
	{
		return topicTitle;
	}

	public void setTopicTitle(String topicTitle)
	{
		this.topicTitle = topicTitle;
	}

	public String getTopicBody()
	{
		return topicBody;
	}

	public void setTopicBody(String topicBody)
	{
		this.topicBody = topicBody;
	}

	public String getDateTime()
	{
		return dateTime;
	}

	public void setDateTime(String dateTime)
	{
		this.dateTime = dateTime;
	}

	public String toString()
	{
		return "" + topicId + ", " + accId + ", " + comments + ", " + topicTitle + ", " + topicBody + ", " + dateTime;
	}
}
