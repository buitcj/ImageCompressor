#include <stdio.h>
#include <stdio.h>
#include <iostream>

//#include "jpeglib.h"
#include "JpegCompressor.h"

using namespace std;

int main()
{
	unsigned char** out_buf_ptr = new unsigned char*;
	unsigned long size;

	int h = 500;
	int w = 500;

	unsigned char* in_buf = new unsigned char[h*w];

	int ret_val = getCompressedBytes(in_buf, h, w, out_buf_ptr, &size);

	if(ret_val != 0)
	{
		cout << "Error with getCompressedBytes" << endl;
		return ret_val;
	}

	FILE* f = fopen("/home/jbu/tmp/blah.jpg", "wb+");
	if(f != NULL)
	{
		fwrite(*out_buf_ptr, 1, size, f);
		cout << "pt 7" << endl;
		fclose(f);
		cout << "pt 8" << endl;
	}
	
	return 0;
}
