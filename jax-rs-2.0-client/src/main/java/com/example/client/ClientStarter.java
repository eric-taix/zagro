package com.example.client;

import java.util.Collection;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.config.RestServiceDetails;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceProvider;

public class ClientStarter {
	
	private static final int CLIENT_NUMBER = 100;
	private static String PEOPLE_SERVICE_NAME = "people";
	
	public static void main( final String[] args ) throws Exception {
		try( final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext( ClientConfig.class ) ) { 
			@SuppressWarnings("unchecked")
			
			// Create the service provider (by default use the RoundRobin strategy)
			final ServiceDiscovery< RestServiceDetails > discovery = context.getBean( ServiceDiscovery.class );
			final ServiceProvider<RestServiceDetails> serviceProvider = discovery.serviceProviderBuilder().serviceName(PEOPLE_SERVICE_NAME).build();
			serviceProvider.start();
			
			// Get the number of service (to adjust the thread count)
			Collection<?> services = discovery.queryForInstances(PEOPLE_SERVICE_NAME);
			
			// Create the REST client
			final Client client = ClientBuilder.newClient();
			
			// Create the callable list
			System.out.println("Creating a pool with " + services.size() + " threads");
	    	ExecutorService executorService = Executors.newFixedThreadPool(services.size());
	    	
	    	// Execute the request to the micro-service 'people'
	    	long start = System.currentTimeMillis();
	    	CompletionService<ClientResult> completionService = new ExecutorCompletionService<ClientResult>(executorService);
	    	for (int iLoop = 0; iLoop < CLIENT_NUMBER; iLoop++) {
	    		completionService.submit(new ClientCallable("Client "+iLoop, serviceProvider, client));
			}
	    	
	    	// Retrieve and display results 
	    	long max = 0;
	    	for (int iLoop = 0; iLoop < CLIENT_NUMBER; iLoop++) {
				ClientResult cr = completionService.take().get();
				long time = cr.getEndTime() - start;
				System.out.println(cr.getId()+" finished in "+time+"ms");
				if (time > max) max = time;
	    	}
			System.out.println("All client finished in "+max+" ms");
			executorService.shutdown();
		}
	}
}
