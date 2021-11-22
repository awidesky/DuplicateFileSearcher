package duplicateFileSearcher;

import java.io.File;
import java.io.Serializable;

public class FilePair implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1490215177091792663L;
	public File f1;
	public File f2;
	
	public FilePair(File a, File b) {
		f1 = a;
		f2 = b;
	}
	
}
