all:
	g++ -fPIC -c -Wall -I${JPEG_LIB_DIR}/include ./*.cpp
	g++ -shared -fPIC -Wl,-z,muldefs -Wall -I${JPEG_LIB_DIR}/include -o ./lib/libjpegcompressor.so ./*.o /home/jbu/libjpeg/jpeg-9/*.o



clean: 
	rm ./*.o
	rm ./lib/libjpegcompressor.so
	rm ./lib/libjpegcompressor.a


#g++ -fPIC -c -Wall -I${JPEG_LIB_DIR}/include -L${JPEG_LIB_DIR}/static -ljpeg ./*.cpp
#g++ -shared -fPIC -Wall -I${JPEG_LIB_DIR}/include -L${JPEG_LIB_DIR}/static -ljpeg -o ./lib/libjpegcompressor.so ./*.o /home/jbu/libjpeg/jpeg-9/O/*.o

#g++ -static -Wall -c -I${JPEG_LIB_DIR}/include -L${JPEG_LIB_DIR}/static -ljpeg ./*cpp
#ar -r ./lib/libjpegcompressor.a ./*.o /home/jbu/libjpeg/jpeg-9/*.o

#g++ -fPIC -c -Wall -I${JPEG_LIB_DIR} -L${JPEG_LIB_DIR}/out_dir -ljpeg ./*.cpp
#g++ -shared -fPIC -Wall -I${JPEG_LIB_DIR} -L${JPEG_LIB_DIR}/out_dir -ljpeg -o ./lib/libjpegcompressor.so ./*.o /home/jbu/libjpeg/jpeg-9/*.o
