package com.radhika.payflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.TimeZone;

@SpringBootApplication
@EnableAspectJAutoProxy
public class PayflowApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(PayflowApplication.class, args);
	}

}
