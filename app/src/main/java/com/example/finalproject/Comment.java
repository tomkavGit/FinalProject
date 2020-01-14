package com.example.finalproject;

public class Comment
{
	int postId;
	int topicId;
	int accId;
	String postBody;
	String dateTime;
	int likes; 
	
	public Comment()
	{
		
	}
	
	public Comment(int postId, int topicId, int accId, String postBody, String dateTime, int likes)
	{
		this.postId = postId;
		this.topicId = topicId;
		this.postBody = postBody;
		this.accId = accId;
		this.dateTime = dateTime;
		this.likes = likes;
	}
	
	public String toString()
	{
		return "" + postId + ", " + topicId + ", " + accId + ", " + postBody + ", " + dateTime + ", " + likes;
	}
}
