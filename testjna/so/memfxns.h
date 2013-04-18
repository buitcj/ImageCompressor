#ifndef MEM_H
#define MEM_H
extern "C" 
{
void getBuf(char** buf_ptr, int* length);
void releaseBuf(char*);
}
#endif
