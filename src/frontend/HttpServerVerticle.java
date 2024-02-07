package frontend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;

import io.vertx.ext.bridge.PermittedOptions;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.templ.mvel.MVELTemplateEngine;

import static io.vertx.ext.web.handler.TemplateHandler.DEFAULT_CONTENT_TYPE;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class HttpServerVerticle extends AbstractVerticle{
	private static final Logger logger = LogManager.getLogger(HttpServerVerticle.class);
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		logger.info("HttpServerVerticle started!");

		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		MVELTemplateEngine engine = MVELTemplateEngine.create(vertx);

		TemplateHandler templateHandler = TemplateHandler.create(engine, "templates/mvel", DEFAULT_CONTENT_TYPE);
		//TemplateHandler templateHandler = TemplateHandler.create(engine);

		//SockJSBridgeOptions options = new SockJSBridgeOptions()
		//		.addOutboundPermitted(new PermittedOptions().setAddress("news-feed"));
	    //SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
	    router.route("/eventbus/*").subRouter(eventBusHandler());
	    router.route("/api/*").subRouter(eventBusHandlerAPI());

		router.route("/static/*").handler(StaticHandler.create("webroot"));
		//router.route("/static/*").handler(StaticHandler.create());

		router.route().handler(FaviconHandler.create(vertx, "webroot/favicon.ico"));

		router.route("/dynamic/*").handler(ctx -> {
			ctx.put("context", ctx);
			ctx.next();
		}).handler(templateHandler);

		router.get("/page2").handler(ctx -> {
			ctx.put("context", ctx);
			ctx.next();
		});

		router.get("/page3").handler(ctx -> {
			ctx.put("context", ctx);
			ctx.next();
		});

		router.get("/page4").handler(ctx -> {
			ctx.put("context", ctx);
			ctx.next();
		});

		// Serve the static pages
		router.route().handler(templateHandler);
		//router.route().handler(BodyHandler.create());

		server.requestHandler(router).listen(8080);


		AtomicInteger contador = new AtomicInteger(0);

		vertx.setPeriodic(1000, timer->{
			System.out.println("Publicando...");
			vertx.eventBus().publish("news-feed", "news from the server! "+ contador.addAndGet(1));
			vertx.eventBus().publish("api-message", "api message from server! "+ contador.addAndGet(1));

		});
		// TEST************************************************************************************************
		/*SockJSHandlerOptions options = new SockJSHandlerOptions()
				.setHeartbeatInterval(1000)
				//.setRegisterWriteHandler(true)
				;
		SockJSHandler sockJSHandlerApi = SockJSHandler.create(vertx, options);

		SockJSBridgeOptions sockJSBridgeOptions = new SockJSBridgeOptions()
		    		.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"))
		    		 .addOutboundPermitted(new PermittedOptions().setAddressRegex("api-message.*"))
		    		;
		router.route("/api/*").subRouter(sockJSHandlerApi.bridge(sockJSBridgeOptions, event->{
			if (event.type() == BridgeEventType.SOCKET_CREATED) {
				System.out.println("A socket to API was created");
			}
			event.complete(true);
		}));*/
		// TEST************************************************************************************************

	}
	private Router eventBusHandler() {
	    SockJSBridgeOptions options = new SockJSBridgeOptions()
	    		.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"))
	            .addOutboundPermitted(new PermittedOptions().setAddressRegex("news-feed.*"))
	    		//.addOutboundPermitted(new PermittedOptions().setAddressRegex("emulator-send\\.[0-9a-fA-F]+"))
	    		;
	    return SockJSHandler.create(vertx).bridge(options, event -> {
			if (event.type() == BridgeEventType.SOCKET_CREATED) {
				System.out.println("A socket was created");
			}
			event.complete(true);
		});
	}

	private Router eventBusHandlerAPI() {
	    SockJSBridgeOptions options = new SockJSBridgeOptions()
	    		.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"))
	            .addOutboundPermitted(new PermittedOptions().setAddressRegex("api-message.*"))
	            .setPingTimeout(5000)
	    		;
	    return SockJSHandler.create(vertx).bridge(options, event -> {
			if (event.type() == BridgeEventType.SOCKET_CREATED) {
				System.out.println("A socket to API was created");
			}
			event.complete(true);
		});
	}
}
