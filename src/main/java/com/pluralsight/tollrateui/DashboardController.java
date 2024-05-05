package com.pluralsight.tollrateui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Controller
public class DashboardController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
	private ReactiveCircuitBreakerFactory circuitBreakerFactory;

    @RequestMapping("/dashboard")
	public String GetTollRate(@RequestParam(defaultValue = "1000") Integer stationId, Model m) {

        //WebClient client = WebClient.create();

        ReactiveCircuitBreaker rcb = circuitBreakerFactory.create("tollrate-cb"); // circuit breaker

        Mono<Product> rate = rcb.run(webClientBuilder.build().get()
            .uri("http://tollrate-service/tollrateslow/" + stationId)
            .retrieve()
            .bodyToMono(Product.class)
            , throwable -> getDefaultRate());
		
		System.out.println("stationId: " + stationId);
		m.addAttribute("rate", rate.block());
		return "dashboard";
	}

    private Mono<Product> getDefaultRate() {

        System.out.println("Fallback method called");

        return Mono.just(new Product(0,2.00f,""));
        
    }
    
}
