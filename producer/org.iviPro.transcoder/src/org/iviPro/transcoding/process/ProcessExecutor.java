package org.iviPro.transcoding.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessExecutor {

	private final List<String> command;
	private final List<String> standardOutput;
	private final List<String> errorOutput;
	private Process process;

	public ProcessExecutor(List<String> command) {
		if (command.size() <= 0) {
			throw new IllegalArgumentException("Command must not be empty!");
		}
		this.command = command;
		this.standardOutput = new ArrayList<String>();
		this.errorOutput = new ArrayList<String>();
	}

	public int execute() throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(false);
		process = processBuilder.start();
		ProcessOutputReader errorOutputReader = new ProcessOutputReader(
				process.getErrorStream());
		ProcessOutputReader standardOutputReader = new ProcessOutputReader(
				process.getInputStream());
		new Thread(errorOutputReader).start();
		new Thread(standardOutputReader).start();
		int exitCode = process.waitFor();
		for (String line : standardOutputReader.getOutput().split(
				"(\\r?\\n)|\\r")) {
			standardOutput.add(line);
		}
		for (String line : errorOutputReader.getOutput().split("(\\r?\\n)|\\r")) {
			errorOutput.add(line);
		}
		return exitCode;
	}

	public void destroy() {
		if (process != null)
			process.destroy();
	}

	public List<String> getStandardOutput() {
		return standardOutput;
	}

	public List<String> getErrorOutput() {
		return errorOutput;
	}

	public List<String> getCommand() {
		return command;
	}
}
