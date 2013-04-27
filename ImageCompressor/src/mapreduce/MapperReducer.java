package mapreduce;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MapperReducer
{

	public static class Map extends
			Mapper<LongWritable, Text, Text, IntWritable>
	{

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException
		{

			System.out.println("***Mapper starting");
			System.out.println("Mapper got key: " + key);
			System.out.println("Mapper got value: " + value);
			// Text word = new Text("output/image.jpg");
			IntWritable one = new IntWritable(1);
			context.write(new Text(value.toString()), one);
		}
	}

	public static class Reduce extends
			Reducer<Text, IntWritable, Text, IntWritable>
	{

		private Path[] localFiles;
		private URI[] cacheFiles;


		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException
		{
			System.out.println("***Reducer starting");

			localFiles = DistributedCache.getLocalCacheFiles(context
					.getConfiguration());

            String jpegLibPath = "";
			if (null != localFiles)
			{
				System.out.println("***localFiles.length: " + localFiles.length);
				if (localFiles.length > 0)
				{
					for (int i = 0; i < localFiles.length; i++)
					{
						Path localFile = localFiles[i]; 
						System.out.println("***local file: " + localFile);
						
						if(localFile.toString().endsWith("libjpegcompressor.so"))
						{
                            jpegLibPath = localFile.toString(); 
						}
						
						IntWritable idx = new IntWritable(i);
						context.write(new Text(localFile.toString()), idx);
					}
				}
			}
			else
			{
				System.out.println("***localFiles was null!");
				return;
			}

            int lastIndexOfSlash = key.toString().lastIndexOf("/");
            String fileName = key.toString().substring(lastIndexOfSlash + 1);

            ImageBundle metadata = ImageConverter.getImageInfo(key.toString());
            if(metadata == null)
            {
                return;
            }

            System.setProperty("jna.library.path", jpegLibPath); 

            final int BLOCK_SIZE = 500;
            
            for(int y = 0; y < metadata.height; y += BLOCK_SIZE)
            {
                for(int x = 0; x < metadata.width; x += BLOCK_SIZE)
                {
                    System.out.println("x: " + x + " y: " + y);

                    // read pixels
                    ImageBundle ib = ImageConverter.readPixels(key.toString(), x / BLOCK_SIZE, y / BLOCK_SIZE);

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
                    byte[] out_buf = JnaInterface.getCompressedBytes(inBytes, ib.height, ib.width, actualNumChannels, jpegLibPath);
                    System.out.println("***got compressed bytes");

                    // write pixels
                    Configuration conf = new Configuration(); 
                    FileSystem fs =	FileSystem.get(conf); 

                    final String basePath = "/user/jbu/output/" + fileName;
                    Path outFile = new Path(basePath + "-" + (new Integer(x)).toString() + "_" + (new Integer(y)).toString());  

                    if(!fs.exists(outFile)) 
                    { 
                        FSDataOutputStream out = fs.create(outFile); 
                        out.write(out_buf, 0, out_buf.length); 
                        out.close();
                    }
                }
            }
		}
	}

	public static void main(String[] args)
	{
		System.out.println("MapperReducer in main function");
		Configuration conf = new Configuration();

		try
		{
			FileSystem fs = FileSystem.get(conf);

			Job job = new Job(conf, "ImageCompressor");
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);

			job.setJarByClass(MapperReducer.class);
			job.setMapperClass(MapperReducer.Map.class);
			job.setReducerClass(MapperReducer.Reduce.class);

			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);

			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));

			DistributedCache.addFileToClassPath(new Path(
					"/user/jbu/lib/jna.jar"), job.getConfiguration(), fs);
			DistributedCache.addCacheFile(new Path(
					"/user/jbu/lib/libjpegcompressor.so").toUri(), job
					.getConfiguration());

			System.out.println("***Waiting for job completion");
			
			job.waitForCompletion(true);
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("MapperReducer exiting main function");
	}

}
