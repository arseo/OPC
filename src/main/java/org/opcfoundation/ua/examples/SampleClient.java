/*
 * ======================================================================== Copyright (c) 2005-2015
 * The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Foundation MIT License 1.00
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The complete license agreement can be found here: http://opcfoundation.org/License/MIT/1.00/
 * ======================================================================
 */

package org.opcfoundation.ua.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opcfoundation.ua.application.Application;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.cert.CertificateCheck;
import org.opcfoundation.ua.cert.DefaultCertificateValidator;
import org.opcfoundation.ua.cert.DefaultCertificateValidatorListener;
import org.opcfoundation.ua.cert.PkiDirectoryCertificateStore;
import org.opcfoundation.ua.cert.ValidationResult;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.BrowseResult;
import org.opcfoundation.ua.core.BrowseResultMask;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.examples.certs.ExampleKeys;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.HttpsSecurityPolicy;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.utils.CertificateUtils;
import org.opcfoundation.ua.utils.EndpointUtil;

/**
 * Sample client creates a connection to OPC UA Server (1st arg), browses and
 * reads a boolean value. It is configured to work against NanoServer example,
 * using the address opc.tcp://localhost:8666/
 * 
 * NOTE: Does not work against SeverExample1, since it does not support Browse
 */
public class SampleClient {

	private static class MyValidationListener implements DefaultCertificateValidatorListener {

		public ValidationResult onValidate(Cert certificate, ApplicationDescription applicationDescription,
				EnumSet<CertificateCheck> passedChecks) {
			System.out.println("Validating Server Certificate...");
			if (passedChecks.containsAll(CertificateCheck.COMPULSORY)) {
				System.out.println("Server Certificate is valid and trusted, accepting certificate!");
				return ValidationResult.AcceptPermanently;
			} else {
				System.out.println("Certificate Details: " + certificate.getCertificate().toString());
				System.out.println("Do you want to accept this certificate?\n" + " (A=Always, Y=Yes, this time, N=No)");
				while (true) {
					try {
						char c;
						c = Character.toLowerCase((char) System.in.read());
						if (c == 'a') {
							return ValidationResult.AcceptPermanently;
						}
						if (c == 'y') {
							return ValidationResult.AcceptOnce;
						}
						if (c == 'n') {
							return ValidationResult.Reject;
						}
					} catch (IOException e) {
						System.out.println("Error reading input! Not accepting certificate.");
						return ValidationResult.Reject;
					}
				}
			}
		}

	}

	public static Map<String, Object> connectClient(String url) throws Exception {
//  public static void main(String[] args) throws Exception {

//		String url = "opc.tcp://192.168.30.175:53530/OPCUA/SimulationServer";
		System.out.print("SampleClient: Connecting to " + url + " .. ");

		Application myClientApplication = new Application();
		Client myClient = new Client(myClientApplication);

		EndpointDescription[] endpoints = myClient.discoverEndpoints(url);
		// 위의 EndpointDescription[] 을 모두 출력 해 보거라.
		// 이것은 서버가 지원하는 모든 인증 모드를 보여 줄 것이야.
		// 아래 처럼 하면 endpoints 를 아래의 것들만 재정의를 하는 것 같다. EndpointUtil 이 말이야.

		endpoints = EndpointUtil.selectByProtocol(endpoints, "opc.tcp");
		endpoints = EndpointUtil.selectByMessageSecurityMode(endpoints, MessageSecurityMode.None);

		myClient.getApplication().addLocale(Locale.ENGLISH);
		myClient.getApplication().setApplicationName(new LocalizedText("Java Sample Client", Locale.ENGLISH));
		myClient.getApplication().setProductUri("urn:JavaSampleClient");

		// 위에서 endpoints array 는 opc.tcp 프로토콜 및 MessageSecurityMode.None 만으로 재정의된 것을

		// 나열하니까, 1개 정도가 들어 있을 것이다.

		EndpointDescription endpoint = endpoints[endpoints.length - 1];
		endpoint.setEndpointUrl(url);
		SessionChannel mySession = myClient.createSessionChannel(endpoint);

		// 이후 코드는 같다.

//		CertificateUtils.setKeySize(2048);
//
//		final KeyPair pair = ExampleKeys.getCert("SampleClient");
//
//		final Client myClient = Client.createClientApplication(pair);
//
//		myClient.getApplication().addLocale(Locale.ENGLISH);
//		myClient.getApplication().setApplicationName(new LocalizedText("Java Sample Client", Locale.ENGLISH));
//		myClient.getApplication().setProductUri("urn:JavaSampleClient");
//
//		final PkiDirectoryCertificateStore myCertStore = new PkiDirectoryCertificateStore("SampleClientPKI/CA");
//		final DefaultCertificateValidator myValidator = new DefaultCertificateValidator(myCertStore);
//		final MyValidationListener myValidationListener = new MyValidationListener();
//		myValidator.setValidationListener(myValidationListener);
//
//		myClient.getApplication().getOpctcpSettings().setCertificateValidator(myValidator);
//		myClient.getApplication().getHttpsSettings().setCertificateValidator(myValidator);
//
//		myClient.getApplication().getHttpsSettings().setHttpsSecurityPolicies(HttpsSecurityPolicy.ALL_104);
//
//		KeyPair myHttpsCertificate = ExampleKeys.getHttpsCert("SampleClient");
//		myClient.getApplication().getHttpsSettings().setKeyPair(myHttpsCertificate);
//
//		SessionChannel mySession = myClient.createSessionChannel(url);
		mySession.activate();
		//////////////////////////////////////

		///////////// EXECUTE //////////////
		// Browse Root
		BrowseDescription browse = new BrowseDescription();
		browse.setNodeId(Identifiers.RootFolder);
		browse.setBrowseDirection(BrowseDirection.Forward);
		browse.setIncludeSubtypes(true);
		browse.setNodeClassMask(NodeClass.Object, NodeClass.Variable);
		browse.setResultMask(BrowseResultMask.All);
		BrowseResponse res3 = mySession.Browse(null, null, null, browse);

		///////////////////// Make Function CallTag //////////////////////////////////

		List<NodeId> nodeIdArray = callTag(res3, mySession, browse);
		System.out.println(nodeIdArray);
		//////////////////////////////////////////////////////////////////////

		// Read Namespace Array
		ReadResponse res5 = mySession.Read(null, null, TimestampsToReturn.Neither,
				new ReadValueId(Identifiers.Server_NamespaceArray, Attributes.Value, null, null));
		String[] namespaceArray = (String[]) res5.getResults()[0].getValue().getValue();

		Map<String, Object> returnMap = new HashMap<>();
		
		returnMap.put("nodeIdArray", nodeIdArray);
		returnMap.put("mySession", mySession);
		return returnMap;
	}
	
	public static JSONObject getTagValue(List<NodeId> nodeIdArray, SessionChannel mySession) throws Exception {
		JSONObject tagJson = new JSONObject();
		JSONArray tagValueJson = new JSONArray();
		for (NodeId n : nodeIdArray) {
			ReadResponse res4 = mySession.Read(null, 500.0, TimestampsToReturn.Both,
					new ReadValueId(n, Attributes.Value, null, null));
//			System.out.println("Tag>>> " + n.getValue());
//			System.out.println(res4);

			JSONObject valueJson = new JSONObject();
			valueJson.put("server", "Simulator");
			valueJson.put("nodeId", n.toString());
			valueJson.put("displayName", n.getValue());
			valueJson.put("serverPicoseconds", res4.getResults()[0].getServerPicoseconds());
			valueJson.put("serverTimestamp", res4.getResults()[0].getServerTimestamp().getUtcCalendar().getTimeInMillis());
			valueJson.put("sourcePicoseconds", res4.getResults()[0].getSourcePicoseconds());
			valueJson.put("sourceTimestamp", res4.getResults()[0].getSourceTimestamp().getUtcCalendar().getTimeInMillis());
			valueJson.put("statusCode", res4.getResults()[0].getStatusCode().getValue());
			valueJson.put("value", res4.getResults()[0].getValue());
			valueJson.put("valueType", res4.getResults()[0].getValue().getCompositeClass().toString());
			tagValueJson.add(valueJson);
		}
		tagJson.put("tag", tagValueJson);
		
		return tagJson;
	}
	
	public static void shutdownClient(SessionChannel mySession) throws Exception {
		mySession.close();
		mySession.closeAsync();
	}
	
	public static List<NodeId> callTag(BrowseResponse res3, SessionChannel mySession, BrowseDescription browse)
			throws Exception {
		List<NodeId> nodeIdArray = new ArrayList<NodeId>();

		BrowseResult[] objectBR = res3.getResults();
		ReferenceDescription[] objectRD = objectBR[0].getReferences();

		int value = Integer.parseInt(objectRD[0].getNodeId().getValue().toString());
		int namespaceIndex = objectRD[0].getNodeId().getNamespaceIndex();
		NodeId nodeId = new NodeId(namespaceIndex, value);

		browse.setNodeId(nodeId);
		res3 = mySession.Browse(null, null, null, browse);

		BrowseResult[] simulatorBR = res3.getResults();
		ReferenceDescription[] simulatorRD = simulatorBR[0].getReferences();

		String values = simulatorRD[1].getNodeId().getValue().toString();
		namespaceIndex = simulatorRD[1].getNodeId().getNamespaceIndex();
		nodeId = new NodeId(namespaceIndex, values);

		browse.setNodeId(nodeId);
		res3 = mySession.Browse(null, null, null, browse);

		BrowseResult[] tagBR = res3.getResults();
		ReferenceDescription[] tagRD = tagBR[0].getReferences();

		for (ReferenceDescription rd : tagRD) {
			String values2 = rd.getNodeId().getValue().toString();
			namespaceIndex = rd.getNodeId().getNamespaceIndex();
			nodeId = new NodeId(namespaceIndex, values2);
			nodeIdArray.add(nodeId);
		}

		return nodeIdArray;
	}

}
