package mapreduce;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

public class JnaTest
{	
	public static byte[] getBuf()
	{
		final int SIZE_OF_POINTER = 4;
		
		System.out.println("Calling getBuf");
		
		MemFxnsLibrary blah = MemFxnsLibrary.INSTANCE;
		
		Pointer buf_ptr = new Memory(Pointer.SIZE);
		
		Pointer length_ptr = new Memory(SIZE_OF_POINTER);
		
		System.out.println("Calling getBuf in native code");
		
		blah.getBuf(buf_ptr, length_ptr);
		
		System.out.println("Returning from getBuf in native code");
		
		int length = 0;
		length = length_ptr.getInt(0);
		
		System.out.println("length: " + length);
		
		byte[] buf = buf_ptr.getPointer(0).getByteArray(0, length);
		
		
		for(int i = 0; i < length; i++)
		{
			System.out.println("buf[" + i + "]: " + buf[i]);
		}
		
		System.out.println("Returning from getBuf");
		
		return buf;
	}
	
	public static void main(String[] args)
	{
		System.out.println("Starting main");
		
		getBuf();
		
		System.out.println("Ending main");
	}
}
