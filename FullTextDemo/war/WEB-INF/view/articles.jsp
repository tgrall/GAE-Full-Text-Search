<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>    
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>GAE FullText Search Demonstration</title>
<meta name="Description" content="Sample Full Text Search Application on GAE" />
<meta name="Keywords" content="GAE, Cloud SQL, fulltext" />


<style>
body {
    font-family: Verdana, Helvetica, sans-serif;
    background-color: #FFFFCC;
}
</style>

</head>
<body>

<h2>List of Articles</h2>

    <form  method="get">
      <div><input type="text" name="query" value="${query}" /> <input type="submit" value="Search" /></div>      
    </form>


<% 
List<Entity> articles =  (List)request.getAttribute("articles");
for (Entity article : articles) {
%>
  <div>
    <div><h3><%= article.getProperty("title")   %></h3></div>
  <div style="margin-bottom: 5px">
  <%= article.getProperty("body")   %>
  
  </div>
    <div style="font-size: small;">
     - <a href="?action=edit&key=<%=  KeyFactory.keyToString(article.getKey()) %>">Edit</a> - <a href="?action=delete&key=<%=  KeyFactory.keyToString(article.getKey()) %>">Delete</a>
    </div>
  </div>
  <hr/>
<% 
}
%>


<div style="margin-top: 10px">
     <h2>Add/Edit Article</h2>
    <form action="#" method="post">
      <div><input type="text" name="title" value="${titleToUpdate}" /></div>
      <div><textarea name="body" rows="3" cols="60">${bodyToUpdate}</textarea></div>
      <div><input type="submit" value="Post New Article" /></div>
      <input type="hidden" name="key" value="${keyToUpdate}"
    </form>
</div>


</body>
</html>