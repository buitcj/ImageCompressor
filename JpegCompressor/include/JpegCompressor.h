#ifndef JPEG_COMPRESSOR_H
#define JPEG_COMPRESSOR_H
extern "C"
{
int getCompressedBytes(unsigned char* in_buf, int in_h, int in_w, unsigned char** out_buf, unsigned long* size);
void releaseMemory(unsigned char* ptr);
}
#endif

