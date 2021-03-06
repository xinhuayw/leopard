package io.leopard.web.mvc.option;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OptionController {

	@RequestMapping("enum/{id}")
	@ResponseBody
	public List<OptionVO> onum(@PathVariable String id) throws OptionNotFoundException {
		List<OptionVO> data = OptionData.getData(id);
		if (data == null) {
			throw new OptionNotFoundException("枚举[" + id + "]不存在.");
		}
		return data;
	}

	@RequestMapping("enum/list")
	@ResponseBody
	public Map<String, List<OptionVO>> list(List<String> idList) throws OptionNotFoundException {
		Map<String, List<OptionVO>> map = new LinkedHashMap<String, List<OptionVO>>();
		for (String id : idList) {
			List<OptionVO> data = OptionData.getData(id);
			if (data == null) {
				throw new OptionNotFoundException("枚举[" + id + "]不存在.");
			}
			map.put(id, data);
		}
		return map;
	}

}
