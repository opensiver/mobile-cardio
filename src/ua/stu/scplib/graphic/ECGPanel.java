package ua.stu.scplib.graphic;

import and.awt.BasicStroke;
import and.awt.Color;
import and.awt.geom.GeneralPath;
import and.awt.geom.Line2D;
import and.awt.geom.Rectangle2D;
import android.content.Context;
import android.util.AttributeSet;
import net.pbdavey.awt.AwtView;
import net.pbdavey.awt.Font;
import net.pbdavey.awt.Graphics2D;
import net.pbdavey.awt.RenderingHints;


public class ECGPanel extends AwtView {
	private short[][] samples;
	private int numberOfChannels;
	private int nSamplesPerChannel;
	private int nTilesPerColumn;
	private int nTilesPerRow;
	private float samplingIntervalInMilliSeconds;
	private float[] amplitudeScalingFactorInMilliVolts;
	private String[] channelNames;
	private float widthOfPixelInMilliSeconds;
	private float heightOfPixelInMilliVolts;
	private float timeOffsetInMilliSeconds;
	private int displaySequence[];
	private int width;
	private int height;
	private boolean fillBackgroundFirst;
	
	public ECGPanel(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ECGPanel(Context context, AttributeSet attribSet) {
		super(context, attribSet);
	}
	
	/**
	 * <p>Construct a component containing an array of tiles of ECG waveforms.</p>
	 *
	 * @param	samples					the ECG data as separate channels
	 * @param	numberOfChannels			the number of channels (leads)
	 * @param	nSamplesPerChannel			the number of samples per channel (same for all channels)
	 * @param	channelNames				the names of each channel with which to annotate them
	 * @param	nTilesPerColumn				the number of tiles to display per column
	 * @param	nTilesPerRow				the number of tiles to display per row (if 1, then nTilesPerColumn should == numberOfChannels)
	 * @param	samplingIntervalInMilliSeconds		the sampling interval (duration of each sample) in milliseconds
	 * @param	amplitudeScalingFactorInMilliVolts	how many millivolts per unit of sample data (may be different for each channel)
	 * @param	horizontalPixelsPerMilliSecond		how may pixels to use to represent one millisecond 
	 * @param	verticalPixelsPerMilliVolt		how may pixels to use to represent one millivolt
	 * @param	timeOffsetInMilliSeconds		how much of the sample data to skip, specified in milliseconds from the start of the samples
	 * @param	displaySequence				an array of indexes into samples (etc.) sorted into desired sequential display order
	 * @param	width					the width of the resulting component (sample data is truncated to fit if necessary)
	 * @param	height					the height of the resulting component (sample data is truncated to fit if necessary)
	 */
	public void setParameters (short[][] samples,int numberOfChannels,int nSamplesPerChannel,String[] channelNames,int nTilesPerColumn,int nTilesPerRow,
			float samplingIntervalInMilliSeconds,float[] amplitudeScalingFactorInMilliVolts,
			float horizontalPixelsPerMilliSecond,float verticalPixelsPerMilliVolt,
			float timeOffsetInMilliSeconds,int[] displaySequence,
			int width,int height,boolean fillBackgroundFirst) {
		this.samples=samples;
		this.numberOfChannels=numberOfChannels;
		this.nSamplesPerChannel=nSamplesPerChannel;
		this.channelNames=channelNames;
		this.nTilesPerColumn=nTilesPerColumn;
		this.nTilesPerRow=nTilesPerRow;
		this.samplingIntervalInMilliSeconds=samplingIntervalInMilliSeconds;
		this.amplitudeScalingFactorInMilliVolts=amplitudeScalingFactorInMilliVolts;
		this.widthOfPixelInMilliSeconds = 1/horizontalPixelsPerMilliSecond;
		this.heightOfPixelInMilliVolts = 1/verticalPixelsPerMilliVolt;
		this.timeOffsetInMilliSeconds=timeOffsetInMilliSeconds;
		this.displaySequence=displaySequence;
		this.width=width;
		this.height=height;
		this.fillBackgroundFirst = fillBackgroundFirst;
	}

	/**
	 * @param	g2
	 * @param	r
	 * @param	fillBackgroundFirst
	 */
	@Override
	public void paint(Graphics2D g2) {
		Color backgroundColor = Color.white;
		Color curveColor = Color.blue;
		Color boxColor = Color.black;
		Color gridColor = Color.red;
		Color channelNameColor = Color.black;
		
		float curveWidth = 1.5f;
		float boxWidth = 2;
		float gridWidth = 1;
		
		Font channelNameFont = new Font("SansSerif",0,14);
		
		int channelNameXOffset = 10;
		int channelNameYOffset = 20;
		
		g2.setBackground(backgroundColor);
		g2.setColor(backgroundColor);
		if (fillBackgroundFirst) {
			g2.fill(new Rectangle2D.Float(0,0,width,height));
		}
		
		float widthOfTileInPixels = (float)width/nTilesPerRow;
		float heightOfTileInPixels = (float)height/nTilesPerColumn;
		
		float widthOfTileInMilliSeconds = widthOfPixelInMilliSeconds*widthOfTileInPixels;
		float  heightOfTileInMilliVolts =  heightOfPixelInMilliVolts*heightOfTileInPixels;

		// first draw boxes around each tile, with anti-aliasing turned on (only way to get consistent thickness)
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(gridColor);

		float drawingOffsetY = 0;
		for (int row=0;row<nTilesPerColumn;++row) {
			float drawingOffsetX = 0;
			for (int col=0;col<nTilesPerRow;++col) {
				g2.setStroke(new BasicStroke(gridWidth));
				for (float time=0; time<widthOfTileInMilliSeconds; time+=200) {
					float x = drawingOffsetX+time/widthOfPixelInMilliSeconds;
					g2.draw(new Line2D.Float(x,drawingOffsetY,x,drawingOffsetY+heightOfTileInPixels));
				}

				g2.setStroke(new BasicStroke(gridWidth));
				for (float milliVolts=-heightOfTileInMilliVolts/2; milliVolts<=heightOfTileInMilliVolts/2; milliVolts+=0.5) {
					float y = drawingOffsetY + heightOfTileInPixels/2 + milliVolts/heightOfTileInMilliVolts*heightOfTileInPixels;
					g2.draw(new Line2D.Float(drawingOffsetX,y,drawingOffsetX+widthOfTileInPixels,y));

				}
				drawingOffsetX+=widthOfTileInPixels;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}

		g2.setColor(boxColor);
		g2.setStroke(new BasicStroke(boxWidth));

		drawingOffsetY = 0;
		int channel=0;
		for (int row=0;row<nTilesPerColumn;++row) {
			float drawingOffsetX = 0;
			for (int col=0;col<nTilesPerRow;++col) {
				// Just drawing each bounding line once doesn't seem to help them sometimes
				// being thicker than others ... is this a stroke width problem (better if anti-aliasing on, but then too slow) ?
				//g2d.draw(new Rectangle2D.Double(drawingOffsetX,drawingOffsetY,drawingOffsetX+widthOfTile-1,drawingOffsetY+heightOfTile-1));
				if (row == 0)
					g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY,drawingOffsetX+widthOfTileInPixels,drawingOffsetY));					// top
				if (col == 0)
					g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY,drawingOffsetX,drawingOffsetY+heightOfTileInPixels));					// left
				g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY+heightOfTileInPixels,drawingOffsetX+widthOfTileInPixels,drawingOffsetY+heightOfTileInPixels));	// bottom
				g2.draw(new Line2D.Float(drawingOffsetX+widthOfTileInPixels,drawingOffsetY,drawingOffsetX+widthOfTileInPixels,drawingOffsetY+heightOfTileInPixels));	// right
				
				if (channelNames != null && channel < displaySequence.length && displaySequence[channel] < channelNames.length) {
					String channelName=channelNames[displaySequence[channel]];
					if (channelName != null) {
						g2.setColor(channelNameColor);
						g2.setFont(channelNameFont);
						g2.drawString(channelName,drawingOffsetX+channelNameXOffset,drawingOffsetY+channelNameYOffset);
					}
				}
				
				drawingOffsetX+=widthOfTileInPixels;
				++channel;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);	// ugly without

		g2.setColor(curveColor);
		g2.setStroke(new BasicStroke(curveWidth));

		float interceptY = heightOfTileInPixels/2;
		float widthOfSampleInPixels=samplingIntervalInMilliSeconds/widthOfPixelInMilliSeconds;
		int timeOffsetInSamples = (int)(timeOffsetInMilliSeconds/samplingIntervalInMilliSeconds);
		int widthOfTileInSamples = (int)(widthOfTileInMilliSeconds/samplingIntervalInMilliSeconds);
		int usableSamples = nSamplesPerChannel-timeOffsetInSamples;
		if (usableSamples <= 0) {
			//usableSamples=0;
			return;
		}
		else if (usableSamples > widthOfTileInSamples) {
			usableSamples=widthOfTileInSamples-1;
		}

		drawingOffsetY = 0;
		channel=0;
		GeneralPath thePath = new GeneralPath();
		for (int row=0;row<nTilesPerColumn && channel<numberOfChannels;++row) {
			float drawingOffsetX = 0;
			for (int col=0;col<nTilesPerRow && channel<numberOfChannels;++col) {
				float yOffset = drawingOffsetY + interceptY;
				short[] samplesForThisChannel = samples[displaySequence[channel]];
				int i = timeOffsetInSamples;
				float rescaleY =  amplitudeScalingFactorInMilliVolts[displaySequence[channel]]/heightOfPixelInMilliVolts;

				float fromXValue = drawingOffsetX;
				float fromYValue = yOffset - samplesForThisChannel[i]*rescaleY;
				thePath.reset();
				thePath.moveTo(fromXValue,fromYValue);
				++i;
				for (int j=1;j<usableSamples;++j) {
					float toXValue = fromXValue + widthOfSampleInPixels;
					float toYValue = yOffset - samplesForThisChannel[i]*rescaleY;
					i++;
					if ((int)fromXValue != (int)toXValue || (int)fromYValue != (int)toYValue) {
						thePath.lineTo(toXValue,toYValue);
					}
					fromXValue=toXValue;
					fromYValue=toYValue;
				}
				g2.draw(thePath);
				drawingOffsetX+=widthOfTileInPixels;
				++channel;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}
		return;
	}
}

