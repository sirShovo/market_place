package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"application", "domain", "adapter", "config", "shared"})
public class MarketPlaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketPlaceApplication.class, args);
	}

}
