/**
 * Copyright 2014 Ryszard Wiśniewski <brut.alll@gmail.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jadx.core.utils.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



import jadx.core.utils.exceptions.JadxRuntimeException;

/**
 * @author Ryszard Wiśniewski "brut.alll@gmail.com"
 */
public class Res9patchStreamDecoder {

	public void decode(InputStream in, OutputStream out) {
		try {
			Bitmap im = BitmapFactory.decodeStream(in);
			int w = im.getWidth();
			int h = im.getHeight();


			Bitmap im2 = Bitmap.createBitmap(w + 2, h + 2, Bitmap.Config.ARGB_8888);
			//im2.createGraphics().drawImage(im, 1, 1, w, h, null);
			Canvas canvas = new Canvas(im2);

			NinePatch np = getNinePatch(in);
			drawHLine(im2, h + 1, np.padLeft + 1, w - np.padRight);
			drawVLine(im2, w + 1, np.padTop + 1, h - np.padBottom);

			int[] xDivs = np.xDivs;
			for (int i = 0; i < xDivs.length - 1; i += 2) {
				drawHLine(im2, 0, xDivs[i] + 1, xDivs[i + 1]);
			}

			int[] yDivs = np.yDivs;
			for (int i = 0; i < yDivs.length - 1; i += 2) {
				drawVLine(im2, 0, yDivs[i] + 1, yDivs[i + 1]);
			}

			im2.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (Exception e) {
			throw new JadxRuntimeException("9patch image decode error", e);
		}
	}

	private NinePatch getNinePatch(InputStream in) throws IOException {
		ExtDataInput di = new ExtDataInput(in);
		find9patchChunk(di);
		return NinePatch.decode(di);
	}

	private void find9patchChunk(DataInput di) throws IOException {
		di.skipBytes(8);
		while (true) {
			int size;
			try {
				size = di.readInt();
			} catch (IOException ex) {
				throw new JadxRuntimeException("Cant find nine patch chunk", ex);
			}
			if (di.readInt() == NP_CHUNK_TYPE) {
				return;
			}
			di.skipBytes(size + 4);
		}
	}

	private void drawHLine(Bitmap im, int y, int x1, int x2) {
		for (int x = x1; x <= x2; x++) {
			im.setPixel(x, y, NP_COLOR);
		}
	}

	private void drawVLine(Bitmap im, int x, int y1, int y2) {
		for (int y = y1; y <= y2; y++) {
			im.setPixel(x, y, NP_COLOR);
		}
	}

	private static final int NP_CHUNK_TYPE = 0x6e705463; // npTc
	private static final int NP_COLOR = 0xff000000;

	private static class NinePatch {
		public final int padLeft;
		public final int padRight;
		public final int padTop;
		public final int padBottom;
		public final int[] xDivs;
		public final int[] yDivs;

		public NinePatch(int padLeft, int padRight, int padTop, int padBottom,
				int[] xDivs, int[] yDivs) {
			this.padLeft = padLeft;
			this.padRight = padRight;
			this.padTop = padTop;
			this.padBottom = padBottom;
			this.xDivs = xDivs;
			this.yDivs = yDivs;
		}

		public static NinePatch decode(ExtDataInput di) throws IOException {
			di.skipBytes(1);
			byte numXDivs = di.readByte();
			byte numYDivs = di.readByte();
			di.skipBytes(1);
			di.skipBytes(8);
			int padLeft = di.readInt();
			int padRight = di.readInt();
			int padTop = di.readInt();
			int padBottom = di.readInt();
			di.skipBytes(4);
			int[] xDivs = di.readIntArray(numXDivs);
			int[] yDivs = di.readIntArray(numYDivs);

			return new NinePatch(padLeft, padRight, padTop, padBottom, xDivs, yDivs);
		}
	}
}
