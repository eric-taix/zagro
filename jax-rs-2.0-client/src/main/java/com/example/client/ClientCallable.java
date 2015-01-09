package com.example.client;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.example.config.RestServiceDetails;
import com.netflix.curator.RetryPolicy;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.recipes.locks.InterProcessMutex;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.ServiceProvider;

public class ClientCallable implements Callable<ClientResult> {

	private ServiceProvider<RestServiceDetails> serviceProvider;
	private Client client;
	private String id;
	
	public ClientCallable(String id, ServiceProvider<RestServiceDetails> serviceProvider, Client client) {
		this.id = id;
		this.serviceProvider = serviceProvider;
		this.client = client;
	}
	
	@Override
	public ClientResult call() throws Exception {
		ServiceInstance<RestServiceDetails> serviceInstance;
		serviceInstance = serviceProvider.getInstance();
		String uri = serviceInstance.buildUriSpec();
		
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework clientCurator = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
		clientCurator.start();
		
	//	InterProcessMutex lock = new InterProcessMutex(clientCurator, "/"+serviceInstance.getPort());
		try {
		//	lock.acquire(5, TimeUnit.MINUTES);
		//	System.out.println("Lock "+"/"+serviceInstance.getPort()+" acquired");
			final Response response = client.target(uri).request(MediaType.APPLICATION_JSON).get();
	    	response.close();	
	    	return  new ClientResult(id, System.currentTimeMillis());
		}
		finally {
		//	System.out.println("Lock "+"/"+serviceInstance.getPort()+" released");
		//	lock.release();
		}
	}

}
