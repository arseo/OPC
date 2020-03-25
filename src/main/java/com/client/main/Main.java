package com.client.main;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.client.pubSub.Pub;
import com.client.pubSub.Sub;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub		
		Sub.recvLogStart();
		Pub.emitLogStart();
	}

}