package duplicateFileSearcher;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class FilePair {

	public File f1;
	public File f2;
	
	public FilePair(File a, File b) {
		f1 = a;
		f2 = b;
	}
	
	public FilePair(String a, String b) {
		f1 = new File(a);
		f2 = new File(b);
	}
	
	public Stream<String> getAsStream() {
		return Arrays.stream(new String[] {f1.getAbsolutePath(), f2.getAbsolutePath()});
	}
}
