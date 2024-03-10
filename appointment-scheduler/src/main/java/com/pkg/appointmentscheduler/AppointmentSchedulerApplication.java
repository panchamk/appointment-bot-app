package com.pkg.appointmentscheduler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.pkg.appointmentscheduler.module.rest.exception.RestResponseEntityExceptionHandler;

@Slf4j
@ServletComponentScan
@EnableJpaRepositories
@EnableJpaAuditing
@EnableFeignClients
@Import(RestResponseEntityExceptionHandler.class)
@SpringBootApplication(scanBasePackages = { "com.pkg" })
public class AppointmentSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentSchedulerApplication.class, args);
	}

}
