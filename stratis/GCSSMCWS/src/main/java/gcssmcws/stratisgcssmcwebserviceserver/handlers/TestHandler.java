package gcssmcws.stratisgcssmcwebserviceserver.handlers;

import java.util.Set;
import java.util.TreeSet;


import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;


public class TestHandler  implements SOAPHandler<SOAPMessageContext> {

  public boolean handleMessage(SOAPMessageContext context) {
    Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    if (outboundProperty.booleanValue()) {
        System.out.println("-------------> in TestHandler.handleMessage : outbound");
    }
    else {
      System.out.println("-------------> in TestHandler.handleMessage : inbound");
    }

    return true;
  }

  public Set<QName> getHeaders() {
    return new TreeSet();
  }

  public boolean handleFault(SOAPMessageContext context) {
    return false;
  }

  public void close(MessageContext context) {
    //
  }

}
