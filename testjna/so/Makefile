all:
	#g++ -Wall -fPIC -c ./*.cpp
	#g++ -shared -Wl,-soname,libmemfxns.so -o ./lib/libmemfxns.so ./*.o
	g++ -Wall -shared -fPIC -o ./lib/libmemfxns.so ./memfxns.cpp

clean: 
	rm ./*.o
	rm ./lib/libmemfxns.so
