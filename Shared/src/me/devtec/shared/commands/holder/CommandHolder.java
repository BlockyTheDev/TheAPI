package me.devtec.shared.commands.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.devtec.shared.API;
import me.devtec.shared.commands.structures.ArgumentCommandStructure;
import me.devtec.shared.commands.structures.CommandStructure;
import me.devtec.shared.utility.StringUtils;

public class CommandHolder<S> {
	private CommandStructure<S> structure;
	public CommandHolder(CommandStructure<S> structure) {
		this.structure=structure;
	}
	
	public List<String> tablist(Object obj, String[] args) {
		if(!structure.getSenderClass().isAssignableFrom(obj.getClass()))
			return Collections.emptyList();
		@SuppressWarnings("unchecked")
		S s = (S)obj;
		int pos = 0;
		CommandStructure<S> cmd = structure;
		int argPos = 0;
		for(String arg : args) {
			++argPos;
			CommandStructure<S> next = cmd.findStructure(s, arg, true);
			if(next != null) {
				cmd = next;
				++pos;
			}else {
				return (pos == args.length-1 || maybeArgs(cmd, argPos - args.length-1)) ? StringUtils.copyPartialMatches(args[args.length-1], toList(cmd.getNextStructures(s, true))) : Collections.emptyList();
			}
		}
		return StringUtils.copyPartialMatches(args[args.length-1], toList(cmd.getParent().getNextStructures(s, true)));
	}

	private List<String> toList(List<CommandStructure<S>> nextStructures) {
		List<String> args = new ArrayList<>();
		for(CommandStructure<S> structure : nextStructures) {
			args.addAll(structure.tabList());
		}
		return args;
	}

	public void execute(Object obj, String[] args) {
		if(!structure.getSenderClass().isAssignableFrom(obj.getClass()))
			return;
		@SuppressWarnings("unchecked")
		S s = (S)obj;
		CommandStructure<S> cmd = structure;
		int pos = 0;
		for(String arg : args) {
			++pos;
			CommandStructure<S> next = cmd.findStructure(s, arg, false);
			if(next == null && cmd.getFallback() != null) {
				cmd.getFallback().execute(s, cmd, args);
				return;
			}
			if(next == null && maybeArgs(cmd, pos - args.length-1)) {
				break;
			}
			if(next != null)
				cmd = next;
		}
		cmd.getExecutor().execute(s, cmd, args);
	}
	
	public void register(String command, String... aliases) {
		API.commandsRegister.register(this, command, aliases);
	}

	public CommandStructure<S> getStructure() {
		return structure;
	}

	private boolean maybeArgs(CommandStructure<S> cmd, int i) {
		if(cmd instanceof ArgumentCommandStructure)
			return ((ArgumentCommandStructure<S>) cmd).getArgs().isEmpty() && (((ArgumentCommandStructure<S>) cmd).length() == -1 || ((ArgumentCommandStructure<S>) cmd).length() >= i);
		return false;
	}
}
