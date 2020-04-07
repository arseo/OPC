package com.client.pubSub;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.json.simple.JSONObject;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.examples.SampleClient;

import com.client.jpa.JPAserver;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Pub {

    private static final String EXCHANGE_NAME = "logs";

//    public static void main(String[] argv) throws Exception {
	public static void emitLogStart() throws Exception {	
		EntityManagerFactory entityFactory = Persistence.createEntityManagerFactory("jpatest");
        EntityManager manager = entityFactory.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        
        try {
            transaction.begin();
          	// url function
//            insertUrl(manager);
            String url = getUrl(manager);
            
            // rabbitMQ
            rabbitMQSend(url);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
        } finally {
            manager.close();
        }
        entityFactory.close();
    }

    public static void insertUrl(EntityManager manager) {
      JPAserver jpaserver = new JPAserver();
      jpaserver.setUrl("opc.tcp://192.168.56.1:53530/OPCUA/SimulationServer");
      manager.persist(jpaserver);
    }
    
    public static String getUrl(EntityManager manager) {
        String jpql = "select url from JPAserver";
        Query query = manager.createQuery(jpql);
		Object dbSsl = query.getSingleResult();
		
		return dbSsl.toString();
    }
    
    public static void rabbitMQSend(String url) throws Exception {
        // rabbitMQ
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        // opc client connect
        final Map<String, Object> returnMap = SampleClient.connectClient(url);
        
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
		        // rabbit connect
		        try (Connection connection = factory.newConnection();
		             Channel channel = connection.createChannel()) {
		            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		            // opc client tag value get
		            JSONObject tagJson = SampleClient.getTagValue((List<NodeId>) returnMap.get("nodeIdArray"), (SessionChannel) returnMap.get("mySession"));
		            //  message send
		            channel.basicPublish(EXCHANGE_NAME, "", null, tagJson.toJSONString().getBytes());
		        } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 

			}
		};
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(runnable, 0, 1000, TimeUnit.MILLISECONDS);


		
		
//        // rabbit connect
//        try (Connection connection = factory.newConnection();
//             Channel channel = connection.createChannel()) {
//            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//        
//            // opc client tag value get
//            JSONObject tagJson = SampleClient.getTagValue((List<NodeId>) returnMap.get("nodeIdArray"), (SessionChannel) returnMap.get("mySession"));
//            //  message send
//            channel.basicPublish(EXCHANGE_NAME, "", null, tagJson.toJSONString().getBytes());
//        }
        
        // opc client shutdown
//        SampleClient.shutdownClient((SessionChannel) returnMap.get("mySession"));
    }
}