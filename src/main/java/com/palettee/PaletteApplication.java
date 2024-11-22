package com.palettee;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.data.jpa.repository.config.*;

@EnableJpaAuditing
@SpringBootApplication
public class PaletteApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaletteApplication.class, args);
	}

}