package org.rams.sessionmanager.websocket;

import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import sf.ics.cc.sessionmanager.ctiservice.MessageDecoder;
import sf.ics.cc.sessionmanager.ctiservice.RequestHandler;
import sf.ics.cc.sessionmanager.util.SessionMessageHandler;

import com.google.gson.Gson;

@ServerEndpoint(value = "/sessionmanager", configurator = SpringConfigurator.class, decoders = MessageDecoder.class)
@Component
public class AgentWebSocketServer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentWebSocketServer.class);

	
	@Autowired
	private RequestHandler requestHandler;
	
	
	@Autowired
	private SessionMessageHandler sessionMessageHandler;
	

	/**
	 * 
	 * @param session
	 */
	@OnOpen
	public void handleOpen(Session session) {
		session.setMaxIdleTimeout(35000L);
		LOGGER.info("Client Socket is connected ...." + session.getId());
		LOGGER.info("default idle timeout ...." + session.getMaxIdleTimeout());
	}	
	
	
	
	/**
	 * 
	 * @param session
	 * @param closeReason
	 */
	@OnClose
	public void handleClose(Session session, CloseReason closeReason) {
		
		LOGGER.info("Closing reason: "+ closeReason.toString() + "  " + closeReason.getCloseCode());
		requestHandler.removeAgentSession(session);
	}

	
	/**
	 * 
	 * @param session
	 * @param throwable
	 */
	@OnError
	public void handleError(Session session, Throwable throwable) {
		LOGGER.info("Client Socket connection closed improperly...."+ session.getId());
	}
	

	/**
	 * 
	 * @param session
	 * @param request
	 */
	@OnMessage
	public void handleMessage(JSONObject message, Session session) {
		try {
			Map<String, Object> response = requestHandler.handleRequest(message, session);
			//ObjectMapper objectMapper = new ObjectMapper();
			Gson gson = new Gson(); 
			String responseStr = gson.toJson(response);
			sessionMessageHandler.sendMessageToClient(session, responseStr);
			//session.getBasicRemote().sendObject(response);
		} /*catch (IOException e) {
			LOGGER.error("ContactCenterSessionManagerService - An exception occurred while processing the request ", e);
		} catch (EncodeException e) {
			LOGGER.error("ContactCenterSessionManagerService - An exception occurred converting the response to string ", e);
		}*/ catch(Exception e) {
			LOGGER.error("An exception occurred  while handling message ", e);
		}
	}


	/**
	 * 
	 * @param requestHandler
	 */
	public void setRequestHandler(RequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	
	

}
