all:
	g++ -Wall -shared -fPIC -I/home/jbu/Dropbox/research/workspace/testjna/so/include -I${JPEG_LIB_DIR}/include -L/home/jbu/Dropbox/research/workspace/testjna/so/lib -L${JPEG_LIB_DIR} -lmemfxns -ljpeg -o ./lib/libjpegcompressor.so ./*.cpp

clean: 
	#rm ./*.o
	rm ./lib/libjpegcompressor.so
