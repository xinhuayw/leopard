package io.leopard.web.mvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.leopard.web.xparam.resolver.UnderlineNameConfiger;

public class MvcJson {

	private static ObjectWriter formatWriter;

	private static ObjectMapper mapper; // can reuse, share

	static {
		// voFiller.init();
		// Onum序列化
		SimpleModule module = new SimpleModule();
		module.addSerializer(new OnumJsonSerializer());

		boolean enable = UnderlineNameConfiger.isEnable();
		// System.err.println("MappingJacksonResponseBodyAdvice underline:" + underline + " enable:" + enable);
		if (enable) {
			mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
			mapper.registerModule(module);
			formatWriter = mapper.writer().withDefaultPrettyPrinter();
			mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		}
		else {
			mapper = new ObjectMapper();
			mapper.registerModule(module);
			formatWriter = mapper.writer().withDefaultPrettyPrinter();
			mapper = new ObjectMapper();
		}
		mapper.registerModule(module);
	}

	public static String toJson(Object data, boolean format) throws JsonProcessingException {
		String json = null;
		if (format) {
			json = formatWriter.writeValueAsString(data);
		}
		else {
			json = mapper.writeValueAsString(data);
		}
		return json;
	}
}
