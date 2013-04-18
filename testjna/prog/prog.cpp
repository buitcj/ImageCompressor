#include <stdio.h>
#include "memfxns.h"
#include "jpeglib.h"
#include <stdio.h>
#include <iostream>

using namespace std;

int main()
{
	/* CODE BELOW WILL TEST THE GET/RELEASE BUF FUNCTIONALITY OF THE LIBRARY I CREATED
	char* buf = NULL;
	int length;
	getBuf(&buf, &length);
	printf("length: %d\n", length);
	for(int i = 0; i < 4; i++)
		printf("%d\n", buf[i]);
	releaseBuf(buf);
	*/

	// EXPECTED INPUTS
	int w = 200;
	int h = 200;
	unsigned char* buf = new unsigned char[w*h];
	int z = 0;
	for(int j = 0; j < h; j++)
	{
		for(int i = 0; i < w; i++)
		{
			buf[z++] = (z % 255);
		}
	}

	cout << "pt 1" << endl;

 	struct jpeg_compress_struct cinfo;
        struct jpeg_error_mgr jerr;
        cinfo.err = jpeg_std_error(&jerr);
        jpeg_create_compress(&cinfo);

	unsigned char** buf_ptr = new unsigned char*;
	unsigned long size;
	jpeg_mem_dest(&cinfo, buf_ptr, &size);

	cinfo.image_width = w;
	cinfo.image_height = h;
	cinfo.input_components = 1;
	cinfo.in_color_space = JCS_GRAYSCALE;

	jpeg_set_defaults(&cinfo);

	jpeg_start_compress(&cinfo, TRUE);

	JSAMPROW row_pointer[1];
	int row_stride = w;

	cout << "pt 2" << endl;
	
	while(cinfo.next_scanline < cinfo.image_height)
	{
		cout << "pt 2.5" << endl;
		cout << "next_scanline: " << cinfo.next_scanline << " " << " row_stride: " << row_stride << endl;
		row_pointer[0] = &buf[cinfo.next_scanline * row_stride];
		jpeg_write_scanlines(&cinfo, row_pointer, 1);
	}

	cout << "pt 3" << endl;

	jpeg_finish_compress(&cinfo);

	cout << "pt 4" << endl;

	jpeg_destroy_compress(&cinfo);

	cout << "pt 5" << endl;

	FILE* f = fopen("/home/jbu/tmp/blah.jpg", "wb+");

	cout << "pt 6" << endl;

	if(f != NULL)
	{
		fwrite(*buf_ptr, 1, w*h, f);
		cout << "pt 7" << endl;
		fclose(f);
		cout << "pt 8" << endl;
	}
	

	return 0;
}
