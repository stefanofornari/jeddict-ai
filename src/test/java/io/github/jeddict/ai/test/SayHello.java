package io.github.jeddict.ai.test;

import jakarta.ws.rs.Path;

@Path("/greetings")  // to test create rest endpoint hint
public class SayHello {
    public void sayHello(String name) {
        System.out.println("Hello, " + name + "!");
    }
}
