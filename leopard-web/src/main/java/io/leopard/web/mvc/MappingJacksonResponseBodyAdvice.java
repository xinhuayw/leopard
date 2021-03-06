package io.leopard.web.mvc;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.leopard.data.env.EnvUtil;
import io.leopard.mvc.trynb.ResultModifierImpl;
import io.leopard.web.mvc.json.MvcOutputJson;
import io.leopard.web.servlet.JsonDebugger;

public class MappingJacksonResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	// private VoFillerImpl voFiller = new VoFillerImpl();

	// @Value("${xparam.underline}")
	// private String underline;

	@Value("${leopard.mvc.json.format}")
	private String format;

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest req,
			ServerHttpResponse response) {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

		String callback = request.getParameter("callback");

		String format = request.getParameter("format");
		if (format == null) {
			format = this.format;
		}
		boolean isFormat = "true".equals(format);

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("status", "success");
		if (EnvUtil.isDevEnv()) {// 这里加上开发环境判断
			data = ResponseBodyReturnValueConverterImpl.getInstance().convert(data);
			Map<String, Object> debugMap = JsonDebugger.getDebugMap();
			if (debugMap != null) {
				map.putAll(debugMap);
			}
			// data = voFiller.fill(data);
		}
		map.put("data", data);

		ResultModifierImpl.getInstance().modify(request, null, null, map);

		String json = null;
		try {
			json = MvcOutputJson.toJson(map, isFormat);
		}
		catch (JsonProcessingException e) {
			Throwable t = e.getCause();
			if (t != null && t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			throw new RuntimeException("Json序列化出错[" + e.getMessage() + "]", e);
		}

		request.setAttribute("result.json", json);
		request.setAttribute("result.data", data);

		if (StringUtils.isNotEmpty(callback)) {
			if (!isValidCallback(callback)) {
				callback = "callback";
			}
			return callback + "(" + json + ");";
		}
		return json;
	}

	protected static boolean isValidCallback(String callback) {
		if (StringUtils.isEmpty(callback)) {
			throw new IllegalArgumentException("参数callback不能为空.");
		}
		boolean isValidCallback = callback.matches("^[a-zA-Z0-9_\\.]+$");
		return isValidCallback;
	}
}
