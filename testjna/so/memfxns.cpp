#include "memfxns.h"

void getBuf(char** buf_ptr, int* length)
{
	*length = 4;
	char* ret_val = new char[4];
	for(int i = 0; i < *length; i++)
	{
		ret_val[i] = i;
	}	
	*buf_ptr = ret_val; 
}

void releaseBuf(char* buf)
{
	delete buf;
}
