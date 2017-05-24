package pasta.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DualByteArrayOutputStream extends ByteArrayOutputStream {

	private ChildByteArrayOutputStream output;
	private ChildByteArrayOutputStream error;
	
	public DualByteArrayOutputStream() {
		super();
		this.output = new ChildByteArrayOutputStream(this);
		this.error = new ChildByteArrayOutputStream(this);
	}
	
	@Override
	public void close() throws IOException {
		this.output.close();
		this.error.close();
		super.close();
	}
	
	@Override
	public void flush() throws IOException {
		this.output.flush();
		this.error.flush();
		super.flush();
	}
	
	public ByteArrayOutputStream getOutputStream() {
		return output;
	}
	public ByteArrayOutputStream getErrorStream() {
		return error;
	}
}
