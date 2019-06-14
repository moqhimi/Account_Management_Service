package com.mobilab.accountservice.bussiness.process;

/**
 * a super class for processing nodes (gateway & business handler) that implements a 
 * shutdown hook to perform stop operations such as closing the connection and releasing resources 
 * when the process is terminating, for example 
 * by pressing CTRL + C in terminal
 */
public abstract class Handler{
	
	public Handler() {
		// stop, dispose and release resources when shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Handler.this.stop();
			}
		});
	}
	
	public abstract void start();
	
	public abstract void stop();

}
