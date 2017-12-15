/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
