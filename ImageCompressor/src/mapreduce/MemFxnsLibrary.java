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
public interface MemFxnsLibrary extends Library
{
	MemFxnsLibrary INSTANCE = (MemFxnsLibrary)Native.loadLibrary("memfxns", MemFxnsLibrary.class);

	void getBuf(Pointer buf, Pointer length);
}
