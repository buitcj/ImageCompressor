package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.io.*;
import ij.plugin.filter.*;
import ij.plugin.frame.LineWidthAdjuster;
import java.awt.*;

/** This plugin implements the Edit/Options/Appearance command. */
public class AppearanceOptions implements PlugIn, DialogListener {
	private boolean interpolate = Prefs.interpolateScaledImages;
	private boolean open100 = Prefs.open100Percent;
	private boolean black = Prefs.blackCanvas;
	private boolean noBorder = Prefs.noBorder;
	private boolean inverting = Prefs.useInvertingLut;
	private boolean antialiased = Prefs.antialiasedTools;
	private int setMenuSize = Menus.getFontSize();
	private boolean redrawn, repainted;

 	public void run(String arg) {
 		showDialog();
 	}
		
	void showDialog() {
		GenericDialog gd = new GenericDialog("Appearance", IJ.getInstance());
		gd.addCheckbox("Interpolate zoomed images", Prefs.interpolateScaledImages);
		gd.addCheckbox("Open images at 100%", Prefs.open100Percent);
		gd.addCheckbox("Black canvas", Prefs.blackCanvas);
		gd.addCheckbox("No image border", Prefs.noBorder);
		gd.addCheckbox("Use inverting lookup table", Prefs.useInvertingLut);
		gd.addCheckbox("Antialiased tool icons", Prefs.antialiasedTools);
		gd.addNumericField("Menu font size:", Menus.getFontSize(), 0, 3, "points");
        gd.addHelp(IJ.URL+"/docs/menus/edit.html#appearance");
        gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled()) {
			if (antialiased!=Prefs.antialiasedTools)
				Toolbar.getInstance().repaint();
			Prefs.interpolateScaledImages = interpolate;
			Prefs.open100Percent = open100;
			Prefs.blackCanvas = black;
			Prefs.noBorder = noBorder;
			Prefs.useInvertingLut = inverting;
			Prefs.antialiasedTools = antialiased;
			if (redrawn) draw();
			if (repainted) repaintWindow();
			Prefs.open100Percent = open100;
			return;
		}
		if (setMenuSize!=Menus.getFontSize() && !IJ.isMacintosh()) {
			Menus.setFontSize(setMenuSize);
			IJ.showMessage("Appearance", "Restart ImageJ to use the new font size");
		}
		if (Prefs.useInvertingLut) {
			IJ.showMessage("Appearance",
				"The \"Use inverting lookup table\" option is set. Newly opened\n"+
				"8-bit images will use an inverting LUT (white=0, black=255).");
		}
	}
	
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (IJ.isMacOSX()) IJ.wait(100);
		boolean interpolate = gd.getNextBoolean();
		Prefs.open100Percent = gd.getNextBoolean();
		boolean blackCanvas = gd.getNextBoolean();
		boolean noBorder = gd.getNextBoolean();
		Prefs.useInvertingLut = gd.getNextBoolean();
		boolean antialiasedTools = gd.getNextBoolean();
		boolean toolbarChange = antialiasedTools!=Prefs.antialiasedTools;
		Prefs.antialiasedTools = antialiasedTools;
		if (toolbarChange) Toolbar.getInstance().repaint();
		setMenuSize = (int)gd.getNextNumber();
		if (interpolate!=Prefs.interpolateScaledImages) {
			Prefs.interpolateScaledImages = interpolate;
			draw();
		}
		if (blackCanvas!=Prefs.blackCanvas) {
			Prefs.blackCanvas = blackCanvas;
			repaintWindow();
		}
		if (noBorder!=Prefs.noBorder) {
			Prefs.noBorder = noBorder;
			repaintWindow();
		}
		return true;
    }
    
    void draw() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			imp.draw();
		redrawn = true;
    }

	void repaintWindow() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			ImageWindow win = imp.getWindow();
			if (win!=null) {
				if (Prefs.blackCanvas) {
					win.setForeground(Color.white);
					win.setBackground(Color.black);
				} else {
					win.setForeground(Color.black);
					win.setBackground(Color.white);
				}
				imp.repaintWindow();
			}
		}
		repainted = true;
	}
		
}
