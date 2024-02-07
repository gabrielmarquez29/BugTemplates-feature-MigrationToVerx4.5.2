package main;


import frontend.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MainVerticle extends AbstractVerticle{
	private static final Logger logger = LogManager.getLogger(MainVerticle.class);
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		vertx.deployVerticle(new HttpServerVerticle(), new DeploymentOptions().setWorker(true), ar->{
			if (ar.succeeded()) {
				logger.debug(HttpServerVerticle.class.getSimpleName() + " " + "deployed successfully!");
				System.out.println(HttpServerVerticle.class.getSimpleName() + " " + "deployed successfully!");
			}else {
				logger.debug("failed to deploy "+ HttpServerVerticle.class.getSimpleName() + " " + ar.cause());
			}

		});

	}

}
