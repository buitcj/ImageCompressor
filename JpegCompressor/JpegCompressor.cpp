#include <stdio.h>
#include "JpegCompressor.h"
#include "jpeglib.h"
#include <stdio.h>
#include <iostream>

using namespace std;


int getCompressedBytes(unsigned char* in_buf, int in_h, int in_w, int num_channels, unsigned char** out_buf, unsigned long* out_size)
{
 	struct jpeg_compress_struct cinfo;
    struct jpeg_error_mgr jerr;
    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_compress(&cinfo);

    unsigned long size = 0;

	jpeg_mem_dest(&cinfo, out_buf, &size);

	cinfo.image_width = in_w;
	cinfo.image_height = in_h;
    if(num_channels == 1)
    {
        //cout << "GRAYSCALE DETECTED" << endl;
	    cinfo.input_components = 1;
	    cinfo.in_color_space = JCS_GRAYSCALE;
    }
    else if(num_channels == 3)
    {
        //cout << "RGB DETECTED" << endl;
	    cinfo.input_components = 3;
        cinfo.in_color_space = JCS_RGB;
    }


	jpeg_set_defaults(&cinfo);

	jpeg_start_compress(&cinfo, TRUE);

	JSAMPROW row_pointer[1];
	int row_stride = cinfo.image_width * num_channels;

	while(cinfo.next_scanline < cinfo.image_height)
	{
		row_pointer[0] = &in_buf[cinfo.next_scanline * row_stride];
		jpeg_write_scanlines(&cinfo, row_pointer, 1);
	}

	jpeg_finish_compress(&cinfo);
    *out_size = size;
    
	jpeg_destroy_compress(&cinfo);

	return 0;
}

void releaseMemory(unsigned char* ptr)
{
	if(ptr != NULL)
		delete(ptr);
}

/*
int main()
{
	unsigned char** out_buf_ptr = new unsigned char*;
	unsigned long size;

	int h = 500;
	int w = 500;

	unsigned char* in_buf = new unsigned char[h*w];

	int z = 0;
        for(int j = 0; j < h; j++)
        {
                for(int i = 0; i < w; i++)
                {
                        in_buf[z++] = (z % 255);
                }
        }

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
		fclose(f);
	}
	
	return 0;
}
*/
