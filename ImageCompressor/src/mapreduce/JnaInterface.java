package mapreduce;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class JnaInterface
{	
	static final int SIZE_OF_POINTER = 8;
	
	public static void main(String[] args)
	{
		//System.load("/home/jbu/libjpeg/jpeg-9/out_dir/libjpeg.so");
		//System.setProperty("java.library.path", "/home/jbu/libjpeg/jpeg-9/out_dir");
		//System.loadLibrary("jpeg");
		//Native.register("jpeg");
		//System.loadLibrary("libjpeg.so");
		
		String path = "/home/jbu/Dropbox/research/workspace/JpegCompressor/lib/libjpegcompressor.so";
		
		//System.setProperty("jna.library.path", "/home/jbu/Dropbox/research/workspace/JpegCompressor/lib");
		byte[] in_buf = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		int h = 1; 
		int w = 10;
		JnaInterface.getCompressedBytes(in_buf, h, w, 1, path);
	}
	
	public static byte[] getCompressedBytes(byte[] in_buf, int h, int w, int num_channels, String libPath)
	{
		//int getCompressedBytes(unsigned char* in_buf, int in_h, int in_w, 
		//unsigned char** out_buf, unsigned long* size)
		
		System.out.println("0");
		
		JpegCompressionLibrary jcl = (JpegCompressionLibrary) Native.loadLibrary(libPath, JpegCompressionLibrary.class);

		System.out.println("1");
		
		// INPUTS
		Pointer in_buf_p = new Memory(h * w * num_channels);
		in_buf_p.write(0, in_buf, 0, in_buf.length);
		
		System.out.println("2");
		
		// OUTPUTS
		Pointer out_buf_ptr_ptr = new Memory(Pointer.SIZE);
		Pointer out_buf_ptr = new Memory(Pointer.SIZE);
		out_buf_ptr_ptr.setPointer(0, out_buf_ptr);
		
		Pointer length_ptr = new Memory(4); // investigate
		
		//length_ptr.get
		
		System.out.println("3");
		
		System.out.println("inbuf len: " + in_buf.length + " h: " + h + " w: " + w + " num_channels: " + num_channels);
		
		jcl.getCompressedBytes(in_buf_p, h, w, num_channels, out_buf_ptr_ptr, length_ptr);
		
		System.out.println("4");
		
		int length = length_ptr.getInt(0);
		
		System.out.println("length: " + length);
		
		System.out.println("5");
		
		byte[] out_buf = out_buf_ptr_ptr.getPointer(0).getByteArray(0L, (int)length);
		
		System.out.println("6");
		
		// free the memory???
		jcl.releaseMemory(out_buf_ptr_ptr.getPointer(0));
		
		System.out.println("7");
		
		System.out.flush();
		
		return out_buf;
	}
}
