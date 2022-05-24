package me.devtec.shared.dataholder.loaders;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.devtec.shared.dataholder.Config;
import me.devtec.shared.dataholder.loaders.constructor.DataValue;
import me.devtec.shared.json.Json;

public class PropertiesLoader extends EmptyLoader {
	private final Pattern pattern = Pattern.compile("(.*?)=(.*)");

	@Override
	public void load(String input) {
		this.reset();
		if (input == null)
			return;
		List<String> comments = new LinkedList<>();
		for (String s : input.split(System.lineSeparator())) {
			String f = s.trim();
			if (!f.isEmpty() && !f.startsWith("#"))
				comments.add(s);
			else {
				if (s.startsWith(" ")) { // S-s-space?! Maybe.. this is YAML file.
					this.data.clear();
					break;
				}
				Matcher m = this.pattern.matcher(s);
				if (m.find()) {
					String[] value = YamlLoader.splitFromComment(m.group(2));
					this.data.put(m.group(1), DataValue.of(m.group(2), Json.reader().read(value[0]),
							value.length == 2 ? value[1] : null, Config.simple(new LinkedList<>(comments))));
					comments.clear();
					continue;
				}
			}
		}
		if (!comments.isEmpty())
			if (this.data.isEmpty())
				this.header.addAll(Config.simple(comments));
			else
				this.footer.addAll(Config.simple(comments));
		this.loaded = !this.data.isEmpty();
	}
}
