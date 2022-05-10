package gcssmcws.stratisgcssmcwebserviceserver;

import exmlservice.EXMLService;
import exmlservice.I112.SalesorderOutCollection;
import exmlservice.I112UnMarshaller;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.JNDIUtils;
import oracle.sql.CLOB;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;

@Slf4j
@WebService(name = "StratisGCSSMCWebService", targetNamespace = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl", serviceName = "StratisGCSSMCWebService", portName = "StratisGCSSMCWebServiceSoapHttpPort", wsdlLocation = "/WEB-INF/wsdl/StratisGCSSMCWebService.wsdl")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({ObjectFactory.class})
public class StratisGCSSMCWebServiceImpl {

  private static Connection conn = null;

  static {
    SLF4JBridgeHandler.install();
  }

  public StratisGCSSMCWebServiceImpl() {
    log.info("STRATIS build info: {}", JNDIUtils.getInstance().getProperty("STRATBUILDINFO"));
  }

  @WebResult(name = "importDASFCompressedResponseElement", partName = "parameters", targetNamespace = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl")
  @WebMethod(action = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl/importDASFCompressed")
  public ImportDASFCompressedResponseElement importDASFCompressed(@WebParam(name = "importDASFCompressedElement", partName = "parameters", targetNamespace = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl")
                                                                      ImportDASFCompressedElement parameters) {
    log.info("In importDASFCompressed");
    ImportDASFCompressedResponseElement returnElement = new ImportDASFCompressedResponseElement();
    StratisXmlStatus stat = new StratisXmlStatus();
    try {

      byte[] dasfRecord = parameters.getDASFRecord();
      //* convert binary to XML using eXML
      EXMLService factory = new EXMLService();
      String xmlStr = factory.decompress(dasfRecord);
      if (xmlStr == null || xmlStr.length() <= 0) {
        log.warn("Error Decompressing");
      }
      //* insert XML into database
      int result = importGCSSMC("DASF", xmlStr);
      log.info("Import GCSS result {}", result);
      stat.setInterfaceId("I111");
      Date d = new Date(System.currentTimeMillis());
      stat.setTimestamp(d.toString());
      stat.setErrorCode(String.valueOf(result));
      stat.setErrorMessage(convertErrorCode(result));
    }
    catch (Exception e) {
      stat.setErrorMessage(e.toString());
      log.warn("EXCEPTION", e);
    }
    returnElement.setResult(stat);
    log.info("Exiting importDASFCompressed");
    return returnElement;
  }

  @WebResult(name = "importGABFCompressedResponseElement", partName = "parameters", targetNamespace = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl")
  @WebMethod(action = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl/importGABFCompressed")
  public ImportGABFCompressedResponseElement importGABFCompressed(@WebParam(name = "importGABFCompressedElement", partName = "parameters", targetNamespace = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl")
                                                                      ImportGABFCompressedElement parameters) {
    log.info("In importGABFCompressed");
    ImportGABFCompressedResponseElement returnElement = new ImportGABFCompressedResponseElement();
    StratisXmlStatus stat = new StratisXmlStatus();
    try {
      byte[] gabfRecord = parameters.getGABFRecord();
      //* convert binary to XML using eXML
      EXMLService factory = new EXMLService();
      String xmlStr = factory.decompress(gabfRecord);
      if (xmlStr == null || xmlStr.length() <= 0) {
        log.warn("Error Decompressing");
      }
      //* insert XML into database
      int result = importGCSSMC("GABF", xmlStr);
      log.info("Import GCSS result {}", result);

      stat.setInterfaceId("I033");
      Date d = new Date(System.currentTimeMillis());
      stat.setTimestamp(d.toString());
      stat.setErrorCode(String.valueOf(result));
      stat.setErrorMessage(convertErrorCode(result));
    }
    catch (Exception e) {
      stat.setErrorMessage(e.toString());
      log.warn("EXCEPTION", e);
    }
    returnElement.setResult(stat);
    log.info("Exiting importGABFCompressed");
    return returnElement;
  }

  @WebResult(name = "importMATSandGBOFCompressedResponseElement", partName = "parameters", targetNamespace = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl")
  @WebMethod(action = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl/importMATSandGBOFCompressed")
  public ImportMATSandGBOFCompressedResponseElement importMATSandGBOFCompressed(@WebParam(name = "importMATSandGBOFCompressedElement", partName = "parameters", targetNamespace = "http://gcssmcws/StratisGCSSMCWebServiceServer.wsdl")
                                                                                    ImportMATSandGBOFCompressedElement parameters) {
    log.info("In ImportMATSandGBOFCompressedResponseElement");
    StratisXmlStatus stat = new StratisXmlStatus();
    ImportMATSandGBOFCompressedResponseElement returnElement = new ImportMATSandGBOFCompressedResponseElement();
    try {
      byte[] matsAndGBOFRecord = parameters.getMATSandGBOFRecord();
      //* convert binary to XML using eXML
      EXMLService factory = new EXMLService();
      String xmlStr = factory.decompress(matsAndGBOFRecord);

      stat.setInterfaceId("I112");
      Date d = new Date(System.currentTimeMillis());
      stat.setTimestamp(d.toString());
      if (xmlStr == null || xmlStr.length() <= 0) {
        stat.setErrorCode("-2");
        stat.setErrorMessage("Could not Decompress the input.");
        returnElement.setResult(stat);
        log.warn("Error Decompressing");
        return returnElement;
      }
      ByteArrayInputStream ins = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
      BufferedInputStream bf = new BufferedInputStream(ins);

      //* prior to insert XML, determining if this is GBOF or MATS
      SalesorderOutCollection i112Collection = I112UnMarshaller.unmarshall(bf);
      if (i112Collection == null) {
        stat.setErrorCode("-777");
        stat.setErrorMessage("a critical error unmarshalling i112 - unable to insert, resend");
        returnElement.setResult(stat);
        log.warn("a critical error unmarshalling i112 - unable to insert");
        return returnElement;
      }

      List<SalesorderOutCollection.SalesorderOutRecord> matsgbofList = i112Collection.getSalesorderOutRecord();
      if (!matsgbofList.isEmpty()) {
        //* only need to get one to determine if mats or gbof
        SalesorderOutCollection.SalesorderOutRecord rec = matsgbofList.get(0);
        String dic = rec.getDIC();
        if (dic != null && dic.equals("RBF")) stat.setInterfaceId("GBOF");
        else stat.setInterfaceId("MATS");
      }

      //* insert XML into database
      int result = importGCSSMC(stat.getInterfaceId(), xmlStr);
      log.info("Import GCSS result {}", result);

      stat.setErrorCode(String.valueOf(result));
      stat.setErrorMessage(convertErrorCode(result));
    }
    catch (Exception e) {
      stat.setErrorMessage(e.toString());
      log.warn("EXCEPTION", e);
    }
    returnElement.setResult(stat);
    log.info("Exiting ImportMATSandGBOFCompressedResponseElement");
    return returnElement;
  }

  private int importGCSSMC(String interfaceName, String recData) {
    int result = -1;
    PreparedStatement pstmt = null;
    try {
      if (recData == null || recData.length() <= 0)
        return -2;
      dbConnect();
      pstmt = conn.prepareStatement("insert into gcssmc_imports_data (interface_name, status, rec_data, created_by, created_date " + ") values (?,'READY',?,1,sysdate)");
      pstmt.setString(1, interfaceName);
      oracle.sql.CLOB c = oracle.sql.CLOB.createTemporary(pstmt.getConnection(), false, CLOB.DURATION_SESSION);
      c.setString(1, recData);
      pstmt.setClob(2, c);
      pstmt.executeUpdate();
      conn.commit();
      c.freeTemporary();
      result = 0;
    }
    catch (Exception e) {
      log.error("Exception in importGCSSMC", e);
    }
    finally {
      if (pstmt != null) {
        try {
          pstmt.close();
        }
        catch (Exception e2) {
          log.error("Error closing statement", e2);
        }
      }
      dbDisconnect();
    }
    return result;
  }

  private String convertErrorCode(int errCode) {
    String errMsg;
    switch (errCode) {
      case -1:
        errMsg = "Could not Download the input to the STRATIS database.";
        break;
      case -2:
        errMsg = "Could not Decompress the input.";
        break;
      case 0:
        errMsg = "Successfully downloaded to the STRATIS database for further processing.";
        break;
      default:
        errMsg = "";
    }

    return errMsg;
  }

  private void dbConnect() {
    try {
      InitialContext ctx = new InitialContext();
      DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/STRATDBSVRDS");
      conn = ds.getConnection();
      conn.setAutoCommit(false);
    }
    catch (Exception e) {
      log.warn("Exception in dbConnect", e);
    }
  }

  private void dbDisconnect() {
    try {
      if ((conn != null) && (!conn.isClosed())) {
        conn.close();
        conn = null;
      }
    }
    catch (Exception e) {
      log.warn("Exception in dbDisconnect", e);
    }
  }
}
