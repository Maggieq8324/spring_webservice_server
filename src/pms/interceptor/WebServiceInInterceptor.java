package pms.interceptor;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pms.support.Sm3Utils;
import pms.support.StringUtils;

/**
 * WebService的输入拦截器
 * @author coisini
 * @date May 2020, 13
 *
 */
public class WebServiceInInterceptor extends AbstractPhaseInterceptor<SoapMessage> {
	
	private static final String USERNAME = "admin";
    private static final String PASSWORD = "P@ssw0rd";
    
    /**
     * 允许访问的IP
     */
    private static final String ALLOWIP = "127.0.0.1;XXX.XXX.XXX.XXX";

	public WebServiceInInterceptor() {
		/*
		 * 拦截器链有多个阶段，每个阶段都有多个拦截器，拦截器在拦截器链的哪个阶段起作用，可以在拦截器的构造函数中声明
		 * RECEIVE 接收阶段，传输层处理
		 * (PRE/USER/POST)_STREAM 流处理/转换阶段
		 * READ SOAPHeader读取 
		 * (PRE/USER/POST)_PROTOCOL 协议处理阶段，例如JAX-WS的Handler处理 
		 * UNMARSHAL SOAP请求解码阶段 
		 * (PRE/USER/POST)_LOGICAL SOAP请求解码处理阶段 
		 * PRE_INVOKE 调用业务处理之前进入该阶段 
		 * INVOKE 调用业务阶段 
		 * POST_INVOKE 提交业务处理结果，并触发输入连接器
		 */
		super(Phase.PRE_INVOKE);
//		super(Phase.RECEIVE);
		
	}

	/**
	  * 客户端传来的 soap 消息先进入拦截器这里进行处理，客户端的账目与密码消息放在 soap 的消息头<security></security>中，
	  * 类似如下：
     * <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
     * <soap:Header><security><username>admin</username><password>P@ssw0rd</password></security></soap:Header>
     * <soap:Body></soap:Body></soap:Envelope>
     * 现在只需要解析其中的 <head></head>标签，如果解析验证成功，则放行，否则这里直接抛出异常，
     * 服务端不会再往后运行，客户端也会跟着抛出异常，得不到正确结果
     *
     * @param message
     * @throws Fault
     */
	@Override
    public void handleMessage(SoapMessage message) throws Fault {
		System.out.println("PRE_INVOKE");
		
		HttpServletRequest request = (HttpServletRequest)message.get(AbstractHTTPDestination.HTTP_REQUEST);
	    String ipAddr=request.getRemoteAddr();
	    System.out.println("客户端访问IP----"+ipAddr);
	    
	    if(!ALLOWIP.contains(ipAddr)) {
			throw new Fault(new IllegalArgumentException("非法IP地址"), new QName("0009"));
		}
		
		/**
		 * org.apache.cxf.headers.Header
         * QName ：xml 限定名称，客户端设置头信息时，必须与服务器保持一致，否则这里返回的 header 为null，则永远通不过的
         */
		Header authHeader = null;
		//获取验证头
		List<Header> headers = message.getHeaders();
		for(Header h:headers){
			if(h.getName().toString().contains("security")){
				authHeader=h;
				break;
			}
		}
		System.out.println("authHeader");
		System.out.println(authHeader);
		
		if(authHeader !=null) {
			Element auth = (Element) authHeader.getObject();
			NodeList childNodes = auth.getChildNodes();
			String username = null,password = null;
			for(int i = 0, len = childNodes.getLength(); i < len; i++){
					Node item = childNodes.item(i);
					if(item.getNodeName().contains("username")){
						username = item.getTextContent();
						System.out.println(username);
					}
					if(item.getNodeName().contains("password")){
						password = item.getTextContent();
						System.out.println(password);
					}
			}
			
			if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) { 
		    	throw new Fault(new IllegalArgumentException("用户名或密码不能为空"), new QName("0001")); 
		    }
			
			if(!Sm3Utils.verify(USERNAME, username) || !Sm3Utils.verify(PASSWORD,password)) { 
		    	throw new Fault(new IllegalArgumentException("用户名或密码错误"), new QName("0008")); 
		    }
		  
		    if (Sm3Utils.verify(USERNAME, username) && Sm3Utils.verify(PASSWORD,password)) { 
		    	System.out.println("webService 服务端自定义拦截器验证通过...."); 
		    	return;//放行
		    } 
		}else {
			throw new Fault(new IllegalArgumentException("请求头security不合法"), new QName("0010"));
		}
	}

	// 出现错误输出错误信息和栈信息
	public void handleFault(SoapMessage message) {
		Exception exeption = message.getContent(Exception.class);
		System.out.println(exeption.getMessage());
	}
	
}
