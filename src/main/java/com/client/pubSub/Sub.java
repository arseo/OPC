package com.client.pubSub;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.client.jpa.JPAtag;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

public class Sub {
    private static final String EXCHANGE_NAME = "logs";
    
//    public static void main(String[] argv) throws Exception {
    public static void recvLogStart() throws Exception {
    	// rabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        EntityManagerFactory entityFactory = Persistence.createEntityManagerFactory("jpatest");
        EntityManager manager = entityFactory.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            try {
                transaction.begin();
                // rabbitMQ
                rabbitMQRecv(manager, delivery);
                
                transaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
            } 
//            finally {
//                manager.close();
//            }
//            entityFactory.close();
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });		
    }
    
    public static void rabbitMQRecv(EntityManager manager, Delivery delivery) throws Exception {
    	// rabbitMQ
        String message = new String(delivery.getBody());
     	try {
     		// message recv
     		JSONParser parser = new JSONParser();
			Object obj = parser.parse(message);
			JSONObject tagJson = (JSONObject) obj;
            
            // message insert
            JSONArray tagValueJson = (JSONArray) tagJson.get("tag");	            
            for (Object o : tagValueJson) {
            	JSONObject tag = (JSONObject) o;
            	JPAtag jpaTag = new JPAtag();
            	jpaTag.setServer(tag.get("server").toString());
            	jpaTag.setDisplayName(tag.get("displayName").toString());
            	jpaTag.setNodeId(tag.get("nodeId").toString());
            	jpaTag.setServerPicoseconds((Long) tag.get("serverPicoseconds"));
            	jpaTag.setSourcePicoseconds((Long) tag.get("sourcePicoseconds"));            	
            	Date serverDate = new Date((long) tag.get("serverTimestamp"));       	
            	Date sourceDate = new Date((long) tag.get("sourceTimestamp")); 
            	jpaTag.setServerTimestamp(serverDate);
            	jpaTag.setSourceTimestamp(sourceDate);
            	jpaTag.setStatusCode(((Long) tag.get("statusCode")).intValue());
            	if (tag.get("valueType").toString().indexOf("Double") > -1 ) {	// double
            		jpaTag.setValue((Double) tag.get("value"));
            		jpaTag.setValueType("Double");
            	} else {														// int
            		jpaTag.setValue((double) ((Long) tag.get("value")).intValue());
            		jpaTag.setValueType("int");
            	}	
            	Date insertTime = new Date(System.currentTimeMillis());
            	jpaTag.setInsertTime(insertTime);
            	manager.persist(jpaTag);
            }
            System.out.println("Insert");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}