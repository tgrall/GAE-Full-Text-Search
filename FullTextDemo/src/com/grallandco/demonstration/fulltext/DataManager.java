package com.grallandco.demonstration.fulltext;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.rdbms.AppEngineDriver;
import static com.google.appengine.api.datastore.FetchOptions.Builder.*;


public class DataManager {
	
	// TODO : do not forget to put your Cloud SQL Instance here
	private final static String JDBC_URL = "jdbc:google:rdbms://[PUT YOUR INSTANCE HERE]/search_values";

	/**
	 * Create sample article in the datastore
	 */
	public void injectSampleArticles() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		int count = datastore.prepare(new Query("Article")).countEntities(withLimit(2));
		if (count == 0) {	
			this.putArticle(null,"MySQL Tutorial", "DBMS stands for DataBase ...");
			this.putArticle(null, "How To Use MySQL Well", "After you went through a ...");
			this.putArticle(null, "Optimizing MySQL", "In this tutorial we will show ...");
			this.putArticle(null, "Datastore Index Selection and Advanced Search", "Learn how recent improvements to the query planner have made exploding indexes a thing of the past and how to implement an \"advanced search\" function in your application");
			this.putArticle(null, "Datastore series: How Entities and Indexes are Stored", "Provides detailed descriptions of the Bigtables used to store entities and indexes");
			this.putArticle(null, "Datastore series: Handling Datastore Errors" , "Learn why datastore errors occur and how to deal with them");
			this.putArticle(null, "Datastore series: How Index Building Works" , "Explains the back-end processes used to build and delete composite indexes");
			this.putArticle(null, "Building an OpenSocial App with Google App Engine", "Need to add some server side logic or data storage to your OpenSocial app? Google App Engine provides a scalable solution for your backend and you still don\'t need to set up and maintain your own servers.");
			this.putArticle(null, "Datastore series: Updating Your Model's Schema", "Discusses strategies for updating datastore schemas");		
		}
	}

	/**
	 * Get all the article from the store
	 * @return All Articles
	 */
	public Iterable<Entity> getAllArticles() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Article");
		PreparedQuery pq = datastore.prepare(q);
		return pq.asList(FetchOptions.Builder.withDefaults());
	}

	/**
	 * Search the different article using a full text search
	 * @param query criteria to use, this will be send to the SQL instance
	 * @return list of entities (articles)
	 */
	public Iterable<Entity> searchEntity(String query) {

		List<Entity> results = new ArrayList<Entity>();
		
		if (query != null && !query.trim().isEmpty()) {
		Connection conn = null;
		try {
			DriverManager.registerDriver(new AppEngineDriver());
			conn = DriverManager.getConnection(JDBC_URL);
			String statement = "SELECT entity_key FROM articles WHERE MATCH (title,body) AGAINST (? WITH QUERY EXPANSION)";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, query);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String keyAsString = rs.getString(1);				
				Entity article = DatastoreServiceFactory.getDatastoreService().get( KeyFactory.stringToKey(keyAsString)  );
				results.add(article);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {}
		}
		} else {
			return this.getAllArticles();
		}
		
		return results;
	}
	
	
	/**
	 * Delete the Entity and related SQL entry
	 * @param key
	 */
	public void deleteArticle(String key) {
		DatastoreServiceFactory.getDatastoreService().delete(KeyFactory.stringToKey(key));
		Connection conn = null;
		try {
			DriverManager.registerDriver(new AppEngineDriver());
			conn = DriverManager.getConnection(JDBC_URL);
			String statement = "DELETE FROM articles WHERE entity_key = ? ";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1,  key );
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {}
		}		
		
	}
	
	/**
	 * Get an article based on the key
	 * 
	 * @param key to use to retrieve the entity.
	 * @return The entity
	 */
	public Entity getArticle(String key) {
		Entity article = null;
		
		try {
			article = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.stringToKey(key));
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		
		return article;
	}
	

	/**
	 * Create/Update the entity, and update the "search index"
	 * @param key
	 * @param title
	 * @param body
	 */
	public void putArticle( String key, String title, String body ) {	
		// if key is not null then update if null create
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity article = null;
		if (key != null) {
			try {
				article = datastore.get(  KeyFactory.stringToKey(key)  );
			} catch (EntityNotFoundException e) {
			}
		}
		if (article == null) {
			article = new Entity("Article");
		}
		article.setProperty("title", title );
		article.setProperty("body", body);
	
		datastore.put(article);			
		
		Connection conn = null;
		try {
			DriverManager.registerDriver(new AppEngineDriver());
			conn = DriverManager.getConnection(JDBC_URL);
			String statement = "REPLACE INTO articles (entity_key, title, body) VALUES( ? , ? , ? )";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1,  KeyFactory.keyToString(article.getKey())   );
			stmt.setString(2,  article.getProperty("title").toString() );
			stmt.setString(3,  article.getProperty("body").toString() );
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {}
		}
	}
	
	
	/**
	 * Utility method to update the index.
	 */
	public void synchronizeDatabaseValue() {

		Connection conn = null;
		try {
			DriverManager.registerDriver(new AppEngineDriver());
			conn = DriverManager.getConnection(JDBC_URL);
			conn.setAutoCommit(false);		
			String statement = "REPLACE INTO articles (entity_key, title, body) VALUES( ? , ? , ? )";
			PreparedStatement stmt = conn.prepareStatement(statement);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Query q = new Query("Article");			
			PreparedQuery pq = datastore.prepare(q);
			
			for (Entity result : pq.asIterable()) {
				stmt.setString(1,  KeyFactory.keyToString(result.getKey())   );
				stmt.setString(2,  result.getProperty("title").toString() );
				stmt.setString(3,  result.getProperty("body").toString() );
				stmt.executeUpdate();
				System.out.println( stmt.getFetchSize() );
				conn.commit();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {}
		}

	}

}
