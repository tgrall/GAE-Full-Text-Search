package com.grallandco.demonstration.fulltext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class DataInjectorListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		DataManager dm = new DataManager();
		dm.injectSampleArticles();
	}
	

}
