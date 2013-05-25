package mapreduce;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;



public class ImageRecordReader extends RecordReader<Text, InputStream> {
	
    private String mPath = null;
    private InputStream mInputStream = null;

    boolean nextCalled = false;

	public ImageRecordReader(InputSplit is)
	{
		
	}

	@Override
	public void close() throws IOException {
        // don't want to close input stream bc it should remain open until we're done with it
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return new Text(mPath);
	}

	@Override
	public InputStream getCurrentValue() throws IOException, InterruptedException {
        return mInputStream;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
        if(mInputStream == null) return 0.0f;
        else return 100.0f;
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext arg1)
			throws IOException, InterruptedException {
        FileSplit fSplit = (FileSplit) split;
        mPath = fSplit.getPath().toString();
        
        try {
            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(conf);
            InputStream is = (InputStream) fs.open(new Path(mPath));
            
            mInputStream = is;
        }
        catch (Exception e)
        {
            mInputStream = null;
            System.err.println("ImageRecordReader.initialize(...) failed");
        }
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
        if(nextCalled == false)
        {
            nextCalled = true;
            
            
            return true;
        }
        else return false;
	}
}
