package com.huoyun.idp.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(locations = "classpath:/META-INF/SMTP.properties", prefix = "smtp")
@Configuration
public class SmtpProperties {

	private String server;
	private Integer port;
	private String userName;
	private String password;
	private String from;

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

}
