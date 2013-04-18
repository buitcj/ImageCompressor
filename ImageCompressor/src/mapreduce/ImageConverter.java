package mapreduce;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class ImageConverter
{
	public static boolean convertImageTileToJpeg(String s_input_path, String s_output_path_prefix)
	{
		try
		{
			Path path = new Path(s_input_path);
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);

			if (fs.exists(path))
			{
				// CONVERT THE IMAGE HERE
				
				//open the image============================
				
				ImagePlus imp = IJ.openImage(path.toString());  
				int bytes_per_pixel = imp.getBytesPerPixel();
				int height = imp.getHeight();
				int width = imp.getWidth();
				int nChannels = imp.getNChannels();
				int slice_depth = imp.getNSlices();
				int bit_depth = imp.getBitDepth();
				
				System.out.println("bit_depth: " + bit_depth);
				System.out.println("nChannels: " + nChannels);
				System.out.println("slice_depth: " + slice_depth);
				System.out.println("bytes_per_pixel: " + bytes_per_pixel);
				
				// get the pixels===========================
				
				ImageProcessor ip = imp.getProcessor();
				
				ip.setRoi(new Roi(0, 0, 500, 500));
				ip = ip.crop();
				width = height = 500;
				
				Object pixels = new Object();
				
				if(bytes_per_pixel == 1)
				{
					pixels = (byte[]) ip.getPixelsCopy();
				}
				else if(bytes_per_pixel == 2)
				{
					pixels = (short[]) ip.getPixelsCopy();
				}
				else
				{
					pixels = (int[]) ip.getPixelsCopy();
				}
				
				// create the output========================
				ImagePlus output_imp;
				
				if(nChannels == 1)
				{
					// NOT SURE WHY THIS REQUIRES RGB
					output_imp = IJ.createImage("output_jpeg", "8-bit RGB jpeg", width, height, slice_depth);
				}
				else if(nChannels == 3)
				{
					output_imp = IJ.createImage("output_jpeg", "8-bit RGB jpeg", width, height, slice_depth);
				}
				else
					return false;
				
				output_imp.getProcessor().setPixels(pixels);
				
				FileSaver output_saver = new FileSaver(output_imp);
				return output_saver.saveAsJpeg(s_output_path_prefix);
			}
			else
			{
				return false;
			}
		}
		catch (IOException e)
		{	
			e.printStackTrace();
			return false;
		}
	}
	public static boolean convertImageToJpeg(String s_input_path, String s_output_path)
	{
		System.out.println("***Starting call to convertImageToJpeg");
		try
		{
			Path path = new Path(s_input_path);
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			
			System.out.println("***pt1: " + path.toString());

			if (fs.exists(path))
			{
				System.out.println("***pt2");
				// CONVERT THE IMAGE HERE
				
				//open the image============================
				
				ImagePlus imp = IJ.openImage(path.toString());   // imp is a null ptr!
				int bytes_per_pixel = imp.getBytesPerPixel();
				int height = imp.getHeight();
				int width = imp.getWidth();
				int nChannels = imp.getNChannels();
				int slice_depth = imp.getNSlices();
				int bit_depth = imp.getBitDepth();
				
				System.out.println("bit_depth: " + bit_depth);
				System.out.println("nChannels: " + nChannels);
				System.out.println("slice_depth: " + slice_depth);
				System.out.println("bytes_per_pixel: " + bytes_per_pixel);
				
				// get the pixels===========================
				
				System.out.println("***pt3");
				
				ImageProcessor ip = imp.getProcessor();
				
				if(ip != null)
					System.out.println("***imp not null");
				else
					System.out.println("***imp null");
				
				Object pixels = new Object();
				
				if(bytes_per_pixel == 1)
				{
					pixels = (byte[]) ip.getPixelsCopy();
				}
				else if(bytes_per_pixel == 2)
				{
					pixels = (short[]) ip.getPixelsCopy();
				}
				else
				{
					pixels = (int[]) ip.getPixelsCopy();
				}
				
				// create the output========================
				ImagePlus output_imp;
				
				System.out.println("***pt4");
				
				if(nChannels == 1)
				{
					// NOT SURE WHY THIS REQUIRES RGB
					output_imp = IJ.createImage("output_jpeg", "8-bit RGB jpeg", width, height, slice_depth);
				}
				else if(nChannels == 3)
				{
					output_imp = IJ.createImage("output_jpeg", "8-bit RGB jpeg", width, height, slice_depth);
				}
				else
					return false;
				
				System.out.println("***pt5");
				
				output_imp.getProcessor().setPixels(pixels);
				
				System.out.println("***pt6");
				
				System.out.println("About to use filesaver to output the file");
				FileSaver output_saver = new FileSaver(output_imp);
				
				System.out.println("***pt7");
				
				boolean ret_val = output_saver.saveAsJpeg(s_output_path);
				
				System.out.println("***pt8");
				
				return ret_val;
			}
			else
			{
				System.out.println("File did not exist on path: " + path.toString());
				return false;
			}
		}
		catch (IOException e)
		{	
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args)
	{
		for(int i = 0; i < args.length; i++)
		{
			System.out.println("args[" + i + "]: " + args[i]);
		}
		ImageConverter.convertImageTileToJpeg(args[0], args[1]);
		//ImageConverter.convertImageToJpeg(args[0], args[1]);
	}
}
