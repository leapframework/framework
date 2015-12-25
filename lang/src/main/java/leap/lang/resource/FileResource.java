package leap.lang.resource;

import java.io.File;
import java.io.IOException;

public interface FileResource extends WritableResource {

	long contentLength() throws IOException;
	
	File getFile();

	FileResource createRelative(String relativePath);

}