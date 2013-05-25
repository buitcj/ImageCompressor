package mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

// <Text, InputStream> corresponds to <ImageURL, InputStream to that file>
public class ImageFileInputFormat extends FileInputFormat<Text, InputStream> {

	@Override
	public RecordReader<Text, InputStream> createRecordReader(InputSplit is,
			TaskAttemptContext arg1) throws IOException, InterruptedException {
		
		return new ImageRecordReader(is);
	}
	
	protected boolean isSplitable(FileSystem fs, Path filename) 
	{
		return false;
	}
}
