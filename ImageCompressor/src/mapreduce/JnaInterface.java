package mapreduce;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class JnaInterface
{	
	static final int SIZE_OF_POINTER = 4;
	
	public static void main(String[] args)
	{
		//System.load("/home/jbu/libjpeg/jpeg-9/out_dir/libjpeg.so");
		//System.setProperty("java.library.path", "/home/jbu/libjpeg/jpeg-9/out_dir");
		//System.loadLibrary("jpeg");
		//Native.register("jpeg");
		//System.loadLibrary("libjpeg.so");
		
		String path = "/home/jbu/Dropbox/research/workspace/JpegCompressor/lib";
		
		System.setProperty("jna.library.path", "/home/jbu/Dropbox/research/workspace/JpegCompressor/lib");
		byte[] in_buf = {1, 2, 3, 4};
		int h = 1; 
		int w = 4;
		JnaInterface.getCompressedBytes(in_buf, h, w, path);
	}
	
	public static byte[] getCompressedBytes(byte[] in_buf, int h, int w, String libPath)
	{
		//int getCompressedBytes(unsigned char* in_buf, int in_h, int in_w, 
		//unsigned char** out_buf, unsigned long* size)
		
		JpegCompressionLibrary jcl = (JpegCompressionLibrary) Native.loadLibrary(libPath, JpegCompressionLibrary.class);

		// INPUTS
		Pointer in_buf_p = new Memory(h * w);
		in_buf_p.write(0, in_buf, 0, in_buf.length);
		
		// OUTPUTS
		Pointer out_buf_ptr_ptr = new Memory(Pointer.SIZE);
		Pointer length_ptr = new Memory(Pointer.SIZE); // investigate
		
		jcl.getCompressedBytes(in_buf_p, h, w, out_buf_ptr_ptr, length_ptr);
		
		long length = length_ptr.getLong(0);
		Pointer out_buf_ptr = out_buf_ptr_ptr.getPointer(0);
		byte[] out_buf = out_buf_ptr.getByteArray(0L, (int)length);
		
		// free the memory???
		jcl.releaseBuf(out_buf_ptr);
		
		return out_buf;
	}
}
