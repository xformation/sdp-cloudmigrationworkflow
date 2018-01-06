/**
 * 
 */
package com.synectiks.services.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Rajesh
 */
@SpringBootApplication
@ComponentScan("com.synectiks")
public class CloudMigrationApplication {

	private static final Logger logger = LoggerFactory
			.getLogger(CloudMigrationApplication.class);

	private static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication
				.run(CloudMigrationApplication.class, args);
		for (String bean : ctx.getBeanDefinitionNames()) {
			logger.info("Beans: " + bean);
		}
	}

}
