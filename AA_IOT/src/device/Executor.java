package device;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executor {
	public static final String INS_GML = "./AA_IOT/gml/IowaStatewideFiberMap.gml";
	public static final String PALMETTO_GML = "./AA_IOT/gml/Palmetto.gml";
	/*public static final String INS_GML = "/home/mnetlab/eclipse-workspace/AA_IOT/AA_IOT/gml/IowaStatewideFiberMap.gml";
	public static final String PALMETTO_GML = "/home/mnetlab/eclipse-workspace/AA_IOT/AA_IOT/gml/Palmetto.gml";*/
	
	public static void main(String[] args) {
		
		//Network3 task1 = new Network3("task 1", INS_GML, 800, 1000, 20, 15, 0.5, 15, 0.8);
		//Network3 task1 = new Network3("task 1", PALMETTO_GML, 800, 1000, 20, 15, 0.5, 15, 0.8);
		
		/*Network2 task1 = new Network2("task 1", INS_GML, 800, 1000, 20, 15, 0.5, 15, 0.7);
		Network2 task2 = new Network2("task 2", PALMETTO_GML, 800, 1000, 20, 15, 0.5, 15, 0.7);*/
		
		/*Network2 task3 = new Network2("task 3", INS_GML, 800, 1000, 20, 15, 0.5, 15, 0.7);
		Network2 task4 = new Network2("task 4", PALMETTO_GML, 800, 1000, 20, 15, 0.5, 15, 0.7);*/
		
		
		/*Network2 task1 = new Network2("task 1", INS_GML, 1200, 1000, 20, 15, 0.5, 15, 0.6);
		Network2 task2 = new Network2("task 2", INS_GML, 1200, 1000, 20, 15, 0.5, 15, 0.7);
		Network2 task3 = new Network2("task 3", INS_GML, 1200, 1000, 20, 15, 0.5, 15, 0.8);
		Network2 task4 = new Network2("task 4", INS_GML, 1200, 1000, 20, 15, 0.5, 15, 0.9);
		Network2 task5 = new Network2("task 5", INS_GML, 1200, 1000, 20, 15, 0.5, 15, 1.0);*/
		
		/*Network2 task1 = new Network2("task 1", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 15, 0.6);
		Network2 task2 = new Network2("task 2", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 15, 0.7);
		Network2 task3 = new Network2("task 3", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 15, 0.8);
		Network2 task4 = new Network2("task 4", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 15, 0.9);
		Network2 task5 = new Network2("task 5", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 15, 1.0);*/
		
		
		/*Network2 task1 = new Network2("task 1", INS_GML, 1200, 1000, 20, 15, 0.5, 15, 0.7);
		Network2 task2 = new Network2("task 2", INS_GML, 1200, 1000, 20, 15, 0.5, 18, 0.7);
		Network2 task3 = new Network2("task 3", INS_GML, 1200, 1000, 20, 15, 0.5, 21, 0.7);
		Network2 task4 = new Network2("task 4", INS_GML, 1200, 1000, 20, 15, 0.5, 40, 0.7);*/
		
		/*Network2 task1 = new Network2("task 1", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 15, 0.7);
		Network2 task2 = new Network2("task 2", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 18, 0.7);
		Network2 task3 = new Network2("task 3", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 21, 0.7);
		Network2 task4 = new Network2("task 4", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 24, 0.7);*/
		
		
		/*Network2 task1 = new Network2("task 1", INS_GML, 1200, 1000, 12, 15, 0.5, 15, 0.6);
		Network2 task2 = new Network2("task 2", INS_GML, 1200, 1000, 14, 15, 0.5, 15, 0.6);
		Network2 task3 = new Network2("task 3", INS_GML, 1200, 1000, 16, 15, 0.5, 15, 0.6);
		Network2 task4 = new Network2("task 4", INS_GML, 1200, 1000, 18, 15, 0.5, 15, 0.6);
		Network2 task5 = new Network2("task 5", INS_GML, 1200, 1000, 20, 15, 0.5, 15, 0.6);*/
		
		Network2 task1 = new Network2("task 1", PALMETTO_GML, 1200, 1000, 12, 15, 0.5, 15, 0.6);
		Network2 task2 = new Network2("task 2", PALMETTO_GML, 1200, 1000, 14, 15, 0.5, 15, 0.6);
		Network2 task3 = new Network2("task 3", PALMETTO_GML, 1200, 1000, 16, 15, 0.5, 15, 0.6);
		Network2 task4 = new Network2("task 4", PALMETTO_GML, 1200, 1000, 18, 15, 0.5, 15, 0.6);
		Network2 task5 = new Network2("task 5", PALMETTO_GML, 1200, 1000, 20, 15, 0.5, 15, 0.6);
		
		
		
		System.out.println("Starting Executor:");
		
		ExecutorService threadExecutor = Executors.newCachedThreadPool();
		threadExecutor.execute(task1);
		threadExecutor.execute(task2);
		threadExecutor.execute(task3);
		threadExecutor.execute(task4);
		threadExecutor.execute(task5);
		
		/*threadExecutor.execute(task6);
		threadExecutor.execute(task7);
		threadExecutor.execute(task8);
		threadExecutor.execute(task9);
		threadExecutor.execute(task10);
		
		threadExecutor.execute(task11);
		threadExecutor.execute(task12);*/
		

		threadExecutor.shutdown();
		System.out.println("Tasks started, main ends");
		
	} // end main

}