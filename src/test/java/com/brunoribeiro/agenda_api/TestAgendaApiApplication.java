package com.brunoribeiro.agenda_api;

import com.brunoribeiro.AgendaApiApplication;
import org.springframework.boot.SpringApplication;

public class TestAgendaApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AgendaApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
