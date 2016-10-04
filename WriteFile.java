import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
public class WriteFile {
	private String path;
	private boolean appendToFile;
	public WriteFile(String path, boolean append) {
	this.path = path;
	this.appendToFile = append;
	}

	public void writeToFile(String text) throws IOException {
		FileWriter write = new FileWriter(path, appendToFile);
		PrintWriter linePrinter = new PrintWriter(write);
		linePrinter.printf(text);
		linePrinter.close();
	}
}
