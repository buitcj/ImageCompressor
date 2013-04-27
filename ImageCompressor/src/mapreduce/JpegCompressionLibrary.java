package mapreduce;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

/*
 * Notes:
 * 
 * In linux, if the library name is libX11.so then you want the string "X11"
 * Need to set jna.library.path to make jna find your library
 * 
 */
public interface JpegCompressionLibrary extends Library
{
	//Native.register("/home/jbu/libjpeg/jpeg-9/out_dir/libjpeg.so");
	/*JpegCompressionLibrary INSTANCE = (JpegCompressionLibrary)
			Native.loadLibrary("jpegcompressor", JpegCompressionLibrary.class);*/
	
	//JpegCompressionLibrary INSTANCE = null;

	// int getCompressedBytes(unsigned char* in_buf, int in_h, int in_w, 
	// unsigned char** out_buf, unsigned long* size)
	int getCompressedBytes(Pointer in_buf, int in_h, int in_w, int num_channels, Pointer out_buf, Pointer size);
	
	void releaseMemory(Pointer ptr);
}
