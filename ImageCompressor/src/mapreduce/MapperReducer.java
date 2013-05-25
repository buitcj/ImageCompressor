package mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Iterable;
import java.util.Iterator;
import java.net.URI;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MapperReducer
{

	public static class Map extends
			Mapper<Text, InputStream, Text, Text>
	{
		private Path[] localFiles;
		private URI[] cacheFiles;

		public void map(Text key, InputStream value, Context context)
				throws IOException, InterruptedException
		{

			System.out.println("***Mapper starting");
			System.out.println("Mapper got key: " + key);

            long mapStart = System.currentTimeMillis();
            long start, end;

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

                        //IntWritable idx = new IntWritable(i);
                        //context.write(new Text(localFile.toString()), idx);
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

            start = System.currentTimeMillis();
            ImageBundle metadata = ImageConverter.getImageInfo(value, key.toString());
            System.out.println("Opening the image took: " + (System.currentTimeMillis() - start));
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
                    start = System.currentTimeMillis();
                    ImageBundle ib = ImageConverter.readPixels(metadata.imp, x / BLOCK_SIZE, y / BLOCK_SIZE);
                    System.out.println("Read pixels took: " + (System.currentTimeMillis() - start));

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
                        start = System.currentTimeMillis();
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

                        end = System.currentTimeMillis() - start;
                        System.out.println("preprocessing buffer took: " + end);
                    }

                    // compress
                    start = System.currentTimeMillis();
                    byte[] out_buf = JnaInterface.getCompressedBytes(inBytes, ib.height, ib.width, actualNumChannels, jpegLibPath);
                    System.out.println("compression took: " + (System.currentTimeMillis() - start));

                    System.out.println("***got compressed bytes");

                    // write pixels
                    Configuration conf = new Configuration();
                    FileSystem fs = FileSystem.get(conf);

                    final String basePath = "/user/bui/output/" + fileName;
                    Path outFile = new Path(basePath + "-" + (new Integer(x)).toString() + "_" + (new Integer(y)).toString() + ".jpg");

                    if(!fs.exists(outFile))
                    {
                        start = System.currentTimeMillis();
                        FSDataOutputStream out = fs.create(outFile);
                        out.write(out_buf, 0, out_buf.length);
                        out.close();
                        System.out.println("writing took: " + (System.currentTimeMillis() - start));
        
                        context.write(key, new Text(outFile.toString()));
                    }
                }
            }

            System.out.println("Reduce of one key took: " + (System.currentTimeMillis() - mapStart));
		}
	}

	public static class Reduce extends
			Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException
		{
            Iterator iter = values.iterator();
            while(iter.hasNext())
            {
                Text t = (Text) iter.next();
                context.write(key, t);
            }
		}
	}

	public static void main(String[] args)
	{
		System.out.println("MapperReducer in main function");
		Configuration conf = new Configuration();
		conf.set("mapred.child.java.opts", "-Xmx1024m");

		try
		{
			FileSystem fs = FileSystem.get(conf);

			Job job = new Job(conf, "ImageCompressor");
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);

			job.setJarByClass(MapperReducer.class);
			job.setMapperClass(MapperReducer.Map.class);
			job.setReducerClass(MapperReducer.Reduce.class);

			job.setInputFormatClass(ImageFileInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
			
			ImageFileInputFormat.addInputPath(job, new Path(args[0]));
			TextOutputFormat.setOutputPath(job, new Path(args[1]));

			DistributedCache.addFileToClassPath(new Path(
					//"/user/jbu/lib/jna.jar"), job.getConfiguration(), fs);
					args[2]), job.getConfiguration(), fs);
			DistributedCache.addCacheFile(new Path(
					//"/user/jbu/lib/libjpegcompressor.so").toUri(), job
					args[3]).toUri(), job
					.getConfiguration());
			DistributedCache.addFileToClassPath(new Path(
					args[4]), job.getConfiguration(), fs);

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
