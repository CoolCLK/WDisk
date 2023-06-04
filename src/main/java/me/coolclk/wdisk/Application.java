package me.coolclk.wdisk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {
	public static Logger LOGGER = LoggerFactory.getILoggerFactory().getLogger("");

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public static String replaceLast(String string, String match, String replace) {
		StringBuilder sBuilder = new StringBuilder(string);
		int lastIndexOf = sBuilder.lastIndexOf(match);
		if (-1 == lastIndexOf) {
			return string;
		}

		return sBuilder.replace(lastIndexOf, lastIndexOf + match.length(), replace).toString();
	}
}
