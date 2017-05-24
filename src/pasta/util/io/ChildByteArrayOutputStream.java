package pasta.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChildByteArrayOutputStream extends ByteArrayOutputStream {

	private DualByteArrayOutputStream parent;
	
	public ChildByteArrayOutputStream(DualByteArrayOutputStream parent) {
		super();
		this.parent = parent;
	}
	
	@Override
	public synchronized void write(byte[] b, int off, int len) {
		super.write(b, off, len);
		this.parent.write(b, off, len);
	}
	
	@Override
	public synchronized void write(int b) {
		super.write(b);
		parent.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
	}
}
