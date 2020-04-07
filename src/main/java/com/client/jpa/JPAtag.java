package com.client.jpa;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.opcfoundation.ua.builtintypes.DateTime;

@Entity
@Table(name = "opctag")
public class JPAtag {
	@Id @GeneratedValue
	private Long id;
	
	private String displayName;

	private String server;
	
	private String nodeId;
	
	private Long serverPicoseconds;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date serverTimestamp;
	
	private Long sourcePicoseconds;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date sourceTimestamp;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date insertTime;

	private int statusCode;
	
	@Column(precision = 16, scale = 8, columnDefinition="number(16, 8)", name = "value")
	private Double value;
	
	private String valueType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Long getServerPicoseconds() {
		return serverPicoseconds;
	}

	public void setServerPicoseconds(Long serverPicoseconds) {
		this.serverPicoseconds = serverPicoseconds;
	}

	public Date getServerTimestamp() {
		return serverTimestamp;
	}

	public void setServerTimestamp(Date serverTimestamp) {
		this.serverTimestamp = serverTimestamp;
	}

	public Long getSourcePicoseconds() {
		return sourcePicoseconds;
	}

	public void setSourcePicoseconds(Long sourcePicoseconds) {
		this.sourcePicoseconds = sourcePicoseconds;
	}

	public Date getSourceTimestamp() {
		return sourceTimestamp;
	}

	public void setSourceTimestamp(Date sourceTimestamp) {
		this.sourceTimestamp = sourceTimestamp;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
	
}
