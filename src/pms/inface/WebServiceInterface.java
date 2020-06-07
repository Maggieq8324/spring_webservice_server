package pms.inface;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(targetNamespace = "http://spring.webservice.server", name = "WebServiceInterface")
public interface WebServiceInterface {

	@WebMethod
    @WebResult(name = "result", targetNamespace = "http://spring.webservice.server")
	public String sayBye(@WebParam(name = "word", targetNamespace = "http://spring.webservice.server") String word);

}
