package io.leopard.web.view.trynb;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import io.leopard.json.Json;
import io.leopard.mvc.cors.CorsConfig;
import io.leopard.mvc.trynb.ErrorUtil;
import io.leopard.mvc.trynb.ResultModifierImpl;
import io.leopard.mvc.trynb.TrynbResolver;
import io.leopard.mvc.trynb.model.TrynbInfo;
import io.leopard.web.view.AbstractView;
import io.leopard.web.view.StatusCodeException;

@Order(1)
@Component
public class JsonViewTrynbResolver implements TrynbResolver {

	@Autowired
	private CorsConfig corsConfig;

	@Override
	public ModelAndView resolveView(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler, Exception exception, TrynbInfo trynbInfo) {
		// Class<?> returnType = handler.getMethod().getReturnType();
		// if (!JsonView.class.isAssignableFrom(returnType)) {
		ResponseBody body = handler.getMethodAnnotation(ResponseBody.class);
		if (body == null) {
			return null;
		}
		// }

		if (corsConfig.isEnable()) {
			// response.addHeader("Access-Control-Allow-Headers", "X-Requested-With,X_Requested_With,Content-Type");
			// response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			String allowOrigin = corsConfig.getAccessControlAllowOrigin(request);
			if (StringUtils.isNotEmpty(allowOrigin)) {
				response.addHeader("Access-Control-Allow-Origin", allowOrigin);
				response.addHeader("Access-Control-Allow-Credentials", "true");
				// response.addHeader("Access-Control-Allow-Methods", "POST");
				// response.addHeader("Access-Control-Allow-Headers", "x_requested_with,content-type");
			}
		}

		ErrorJsonView jsonView = new ErrorJsonView();
		jsonView.setException(exception);
		jsonView.setHandler(handler);

		if (exception instanceof StatusCodeException) {
			StatusCodeException e = (StatusCodeException) exception;
			jsonView.setStatus(e.getStatus());
		}
		else {
			jsonView.setStatus(trynbInfo.getStatusCode());
		}

		jsonView.setMessage(trynbInfo.getMessage());

		if (trynbInfo.isTrynbMessage() || trynbInfo.isApiMessage() || trynbInfo.isTranslate()) {
			jsonView.setOriginalMessage(ErrorUtil.parseMessage(exception));
		}
		if (SystemUtils.IS_OS_WINDOWS) {// TODO 测试代码
			jsonView.setDetailMessage(exception.getMessage());
		}
		jsonView.setException(exception.getClass().getName());
		return jsonView;
	}

	public class ErrorJsonView extends AbstractView {

		private HandlerMethod handler;

		private Exception exception;

		public void setHandler(HandlerMethod handler) {
			this.handler = handler;
		}

		public void setException(Exception exception) {
			this.exception = exception;
		}

		private Map<String, Object> map = new LinkedHashMap<String, Object>();

		public void setStatus(String status) {
			map.put("status", status);
		}

		public void setDetailMessage(String detailMessage) {
			map.put("detailMessage", detailMessage);
		}

		public void setOriginalMessage(String originalMessage) {
			map.put("originalMessage", originalMessage);
		}

		public void setMessage(String message) {
			map.put("message", message);
		}

		public void setException(String exception) {
			map.put("exception", exception);
		}

		@Override
		public String getContentType() {
			return "application/json; charset=UTF-8";
		}

		@Override
		public String getBody(HttpServletRequest request, HttpServletResponse response) {
			ResultModifierImpl.getInstance().modify(request, handler, exception, map);
			String json = Json.toFormatJson(map);
			request.setAttribute("result.json", json);

			String callback = request.getParameter("callback");
			if (StringUtils.isNotEmpty(callback)) {
				if (!isValidCallback(callback)) {
					callback = "callback";
				}
				return callback + "(" + json + ");";
			}
			return json;
		}

	}

	protected static boolean isValidCallback(String callback) {
		if (StringUtils.isEmpty(callback)) {
			throw new IllegalArgumentException("参数callback不能为空.");
		}
		boolean isValidCallback = callback.matches("^[a-zA-Z0-9_\\.]+$");
		return isValidCallback;
	}
}
