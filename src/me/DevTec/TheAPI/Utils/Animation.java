package me.DevTec.TheAPI.Utils;

import java.util.List;

public class Animation {
	private List<String> lines;
	private long last = System.currentTimeMillis()/50, tics;
	private int c;

	public Animation(List<String> text, long ticks) {
		lines = text;
		tics = ticks;
	}

	public long getTicks() {
		return tics;
	}

	public String get() {
		if (c >= lines.size())
			c = 0;
		if (last - System.currentTimeMillis()/50 + tics <= 0) {
			last = System.currentTimeMillis()/50;
			return lines.get(c++);
		}
		return lines.get(c);
	}

	public List<String> getLines() {
		return lines;
	}
}
