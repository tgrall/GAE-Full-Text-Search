package com.grallandco.demonstration.fulltext;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import com.google.appengine.api.datastore.Entity;


@SuppressWarnings("serial")
public class FullTextDemoServlet extends HttpServlet {

	public void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		DataManager dm = new DataManager();
		
		boolean sendToView = true;

		Iterable<Entity> articles = null;

		// just to manually create content
		if (request.getParameter("inject") != null) {
			dm.injectSampleArticles();
		}
		
		// to synchronize the DB and Datastore
		if (request.getParameter("sycndb") != null) {
			dm.synchronizeDatabaseValue();
		}		

		
		
		// manage content
		if (request.getParameter("query") != null) {
			String query = request.getParameter("query");
			articles =  dm.searchEntity(query);
			request.setAttribute("query", query);
		} 
		
		if (request.getParameter("title") != null ) {
			dm.putArticle( request.getParameter("key") , request.getParameter("title"), request.getParameter("body"));
			response.sendRedirect("/fulltextdemo");
			sendToView = false;
		}
		
		if (request.getParameter("action") != null && request.getParameter("action").equalsIgnoreCase("delete")) {
			dm.deleteArticle(request.getParameter("key"));
			response.sendRedirect("/fulltextdemo");
			sendToView = false;
		}
		
		
		if (request.getParameter("action") != null && request.getParameter("action").equalsIgnoreCase("edit")) {
			Entity articleToUpdate = dm.getArticle(request.getParameter("key"));
			if (articleToUpdate != null) {
				request.setAttribute("keyToUpdate", request.getParameter("key"));
				request.setAttribute("titleToUpdate", articleToUpdate.getProperty("title"));
				request.setAttribute("bodyToUpdate", articleToUpdate.getProperty("body"));
			}
		}
		
		
		
		if (articles == null) {
			articles = dm.getAllArticles();
		}

		request.setAttribute("articles",  articles );
		
		try {
			if (sendToView) { 
			request.getRequestDispatcher("/WEB-INF/view/articles.jsp").forward(
					request, response);
			}
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		processRequest(request, response);
	}

}
