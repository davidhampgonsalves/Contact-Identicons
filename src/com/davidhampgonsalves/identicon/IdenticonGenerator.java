package com.davidhampgonsalves.identicon;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;

public class IdenticonGenerator {
	public static int height = 5;
	public static int width = 5;

	public static Bitmap generate(String userName,
			HashGeneratorInterface hashGenerator) {
		
		byte[] hash = hashGenerator.generate(userName);

		Bitmap identicon = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		// get byte values as unsigned ints
		int r = hash[0] & 255;
		int g = hash[1] & 255;
		int b = hash[2] & 255;

		int background = Color.parseColor("#f0f0f0");
		int foreground = Color.argb(255, r, g, b);

		for (int x = 0; x < width; x++) {
			
			//make identicon horizontally symmetrical 
			int i = x < 3 ? x : 4 - x;
			int pixelColor;
			for (int y = 0; y < height; y++) {
				
				if ((hash[i] >> y & 1) == 1)
					pixelColor = foreground;
				else
					pixelColor = background;

				identicon.setPixel(x, y, pixelColor);
			}
		}
		
		//scale image by 2 to add border
		Bitmap bmpWithBorder = Bitmap.createBitmap(12, 12, identicon.getConfig());
	    Canvas canvas = new Canvas(bmpWithBorder);
	    canvas.drawColor(background);
	    identicon = Bitmap.createScaledBitmap(identicon, 10, 10, false);
	    canvas.drawBitmap(identicon, 1, 1, null);
	    
		return bmpWithBorder;
	}
}