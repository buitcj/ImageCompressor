package solo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

import solo.ImageBundle;
import solo.ImageConverter;
import solo.JnaInterface;

public class Master {
	
	private static String JPEG_COMPRESSOR_LIB;

	public static void compressAll(Vector<String> inputPaths, String outputDir) throws IOException {
		File fileOutputDir = new File(outputDir);
		assert (fileOutputDir.isDirectory());
		String stringOutputDir = outputDir;
		stringOutputDir.concat("/");

		for (String path : inputPaths) {
			// File inputFile = new File(path);
			Path inputPath = Paths.get(path);
			String stringOutputFile = stringOutputDir;
			stringOutputFile = stringOutputFile.concat(inputPath.getFileName()
					.toString());
			
			ImageBundle metadata = ImageConverter.getImageInfo(path);
			
			final int BLOCK_SIZE = 500;
            for(int y = 0; y < metadata.height; y += BLOCK_SIZE)
            {
                for(int x = 0; x < metadata.width; x += BLOCK_SIZE)
                {
                    System.out.println("x: " + x + " y: " + y);

                    // read pixels
                    ImageBundle ib = ImageConverter.readPixels(metadata.imp, x / BLOCK_SIZE, y / BLOCK_SIZE);

                    if(ib == null || ib.pixels == null)
                    {
                        System.out.println("srcBytes was null");
                        return;
                    }

                    System.out.println("starting buffer rewrite code");

                    byte[] inBytes = null;
                    int actualNumChannels = 1;
                    if(ib.numChannels == 1 && ib.bytesPerPixel == 4)
                    {
                        actualNumChannels = 3;

                        // need to convert
                        int size = ib.width * ib.height * 3;
                        inBytes = new byte[size]; // 3 for RGB
                        System.out.println("inBytes size: " + size);
                        int srcBytes_counter = 0;
                        int inBytes_counter = 0;
                        int[] pixels = (int[]) ib.pixels;
                        for(int iy = 0; iy < ib.height; iy++)
                        {
                            for(int ix = 0; ix < ib.width; ix++)
                            {
                                int pixel = pixels[srcBytes_counter++];
                                inBytes[inBytes_counter] = (byte)((pixel&0xff0000)>>16);
                                inBytes[inBytes_counter+1] = (byte)((pixel&0xff00)>>8);
                                inBytes[inBytes_counter+2] = (byte)(pixel&0xff);
                                inBytes_counter+=3;
                            }
                        }
                    }

                    // compress
                    byte[] out_buf = JnaInterface.getCompressedBytes(inBytes, ib.height, ib.width, actualNumChannels, JPEG_COMPRESSOR_LIB);

                    System.out.println("***got compressed bytes");

                    // write pixels
                    String stringOutputFilePiece = (stringOutputFile + "-" + (new Integer(x)).toString() + "_" + (new Integer(y)).toString() + ".jpg");
                    File fileSOFP = new File(stringOutputFilePiece);
                    if(!fileSOFP.exists())
                    {
                    	fileSOFP.createNewFile();
                    	FileOutputStream fos = new FileOutputStream(stringOutputFilePiece);
                        fos.write(out_buf);
                        fos.close();
                    }
                }
            }
		}
	}

	public static void getInputPathsRecursively(String inputPath,
			Vector<String> collectedInputPaths) {

		File fileInputPath = new File(inputPath);
		if (fileInputPath.isDirectory() == false) {
			collectedInputPaths.add(inputPath);
		} else {
			File[] files = fileInputPath.listFiles();

			for (int i = 0; i < files.length; i++) {
				getInputPathsRecursively(files[i].toString(),
						collectedInputPaths);
			}
		}
	}

	public static void main(String[] args) {
		// root dir should be the first param

		assert (args.length == 2);

		// String inputPath = args[1];
		// String outputPath = args[2];
		// JPEG_COMPRESSOR_LIB = args[3];

		String inputPath = "/home/jbu/images/";
		String outputPath = "/home/jbu/output/";
		JPEG_COMPRESSOR_LIB = "/home/jbu/Dropbox/research/workspace/JpegCompressor/lib/libjpegcompressor.so";

		System.out.println("inputPath: " + inputPath);
		System.out.println("outputPath: " + outputPath);

		System.out.println("Calling getInputPathsRecursively...");
		Vector<String> collectedInputPaths = new Vector<String>();
		getInputPathsRecursively(inputPath, collectedInputPaths);
		for (String path : collectedInputPaths) {
			System.out.println("Found input: " + path);
		}

		System.out.println("Calling compressAll...");
		try {
			compressAll(collectedInputPaths, outputPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
