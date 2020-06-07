package pms.interceptor;

import java.io.OutputStream;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * WebService的输出拦截器
 * @author coisini
 * @date May 2020, 13
 *
 */
public class WebServiceOutInterceptor extends AbstractPhaseInterceptor<Message> {

	public WebServiceOutInterceptor() {
		// 拦截器在调用方法之前拦截SOAP消息
		super(Phase.PRE_STREAM);
	}

	public void handleMessage(Message message) throws Fault {
		OutputStream os = (OutputStream) message.getContent(OutputStream.class);
		if (os == null) {
			return;
		}
		CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
		message.setContent(OutputStream.class, newOut);
		newOut.registerCallback(new LoggingCallback(message, os));
	}

	// 出现错误输出错误信息和栈信息
	public void handleFault(Message message) {
		Exception exeption = message.getContent(Exception.class);
		System.out.println(exeption.getMessage());
	}

	class LoggingCallback implements CachedOutputStreamCallback {
		private final Message message;
		private final OutputStream origStream;

		public LoggingCallback(Message msg, OutputStream os) {
			this.message = msg;
			this.origStream = os;
		}

		public void onFlush(CachedOutputStream cos) {
		}

		public void onClose(CachedOutputStream cos) {
			
		}
	}

}