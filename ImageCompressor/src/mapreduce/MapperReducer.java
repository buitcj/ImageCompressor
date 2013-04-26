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

			Path parentDir = null;
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
							parentDir = localFile.getParent();
							context.write(new Text(parentDir.toString()), new IntWritable(99));
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
			
			ImageConverter ic = new ImageConverter();
			System.out.println("***Reducer's key: " + key.toString());
			
			System.out.println("about to convert image to jpeg");
			//boolean worked = ic.convertImageToJpeg(key.toString(), "/user/jbu/output/blah.jpg");
			System.out.println("done converting image to jpeg");
			
			/* code below sets up jna and then calls libmemfxns */
			if(parentDir != null) 
			{ 
				System.out.println("parentDir: " + parentDir.toString()); 
				System.setProperty("jna.library.path", parentDir.toString()); 
				
				context.write(new Text("got here1"), new IntWritable(66));
				
				int h = 200; 
				int w = 200;
				byte[] in_buf = new byte[h*w];
				int z = 0;
				for(int j = 0; j < h; j++)
				{
					for(int i = 0; i < w; i++)
					{
						in_buf[z++] = (byte) (z % 255);
					}
				}
				
				byte[] out_buf = JnaInterface.getCompressedBytes(in_buf, h, w, parentDir.toString() + "/" + "libjpegcompressor.so");
				
                System.out.println("***got compressed bytes");

				Configuration conf = new Configuration(); 
				FileSystem fs =	FileSystem.get(conf); 
				
				Path outFile = new Path("/user/jbu/output/blah.jpg");
				if(!fs.exists(outFile)) 
				{ 
                    System.out.println("***About to create the file");
					FSDataOutputStream out = fs.create(outFile); 
                    System.out.println("***Done creating the file");
					out.write(out_buf, 0, out_buf.length); 
					out.close();
				}
			}
			else
			{
				System.out.println("parent dir was null");
			}

			/*
			 * Code below attempts to compress an entire image to jpeg boolean
			 * success =
			 * ImageConverter.convertImageToJpeg(localFiles[0].toString(),
			 * "/user/bui/output/" + fileName + ".jpg");
			 * 
			 * if(success) System.out.println("convertImageToJpeg succeeded");
			 * else System.out.println("convertImageToJpeg failed");
			 */

			
			/* THIS CODE CREATES A BUFFER AND WRITES A FILE TO HDFS WITH THE BUFFER
			 * 
			 * Configuration conf = new Configuration(); FileSystem fs =
			 * FileSystem.get(conf); byte[] buf = {0, 1, 2, 3, 4};
			 * 
			 * Path outFile = new Path("/user/bui/output/blah.jpg");
			 * if(!fs.exists(outFile)) { FSDataOutputStream out =
			 * fs.create(outFile); out.write(buf, 0, buf.length); out.close(); }
			 */

			/*
			 * This code appears to create files in the paths that are passed in
			 * as input
			 * 
			 * Configuration conf = new Configuration(); FileSystem fs =
			 * FileSystem.get(conf); byte[] buf = {0, 1, 2, 3, 4};
			 * 
			 * while (values.iterator().hasNext()) { IntWritable value =
			 * values.iterator().next(); context.write(key, value);
			 * System.out.println("Writing: " + key + " " + value);
			 * 
			 * Path outFile = new Path(key.toString()); if(!fs.exists(outFile))
			 * { FSDataOutputStream out = fs.create(outFile); out.write(buf, 0,
			 * buf.length); } }
			 */
			
			System.out.flush();
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
