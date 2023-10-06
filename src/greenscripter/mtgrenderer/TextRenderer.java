package greenscripter.mtgrenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class TextRenderer {

	public static void render(Graphics g, String text, int x, int y) {
		FontMetrics fm = g.getFontMetrics();

		List<String> parts = extractSegments(text);
		for (String s : parts) {
			if (s.startsWith("{")) {
				int size = (int) (fm.getAscent());
				g.drawImage(getSymbol(s.replace("/", "")), x, y - size, size, size, null);
				x += fm.getAscent() * 1.05;
			} else {
				g.drawString(s, x, y);
				x += fm.stringWidth(s);
			}
		}

	}

	public static int getLength(Graphics g, String text) {
		return getLength(g.getFontMetrics(), text);
	}

	public static int getLength(FontMetrics fm, String text) {
		int length = 0;
		List<String> parts = extractSegments(text);
		for (String s : parts) {
			if (s.startsWith("{")) {
				length += fm.getAscent();
			} else {
				length += fm.stringWidth(s);
			}
		}
		return length;
	}

	public static List<String> extractSegments(String text) {
		List<String> chunks = new ArrayList<>();
		String s = "";
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '{') {
				if (!s.isEmpty()) chunks.add(s);
				s = "";
			}
			s += text.charAt(i);
			if (text.charAt(i) == '}') {
				if (!s.isEmpty()) chunks.add(s);
				s = "";
			}
		}
		if (!s.isEmpty()) chunks.add(s);
		return chunks;
	}

	public static List<String> splitByCase(String text) {
		List<String> chunks = new ArrayList<>();
		String s = "";
		boolean capital = true;
		for (int i = 0; i < text.length(); i++) {
			if (capital != Character.isUpperCase(text.charAt(i))) {
				if (!s.isEmpty()) chunks.add(s);
				s = "";
				capital = Character.isUpperCase(text.charAt(i));
			}
			s += text.charAt(i);

		}
		if (!s.isEmpty()) chunks.add(s);
		return chunks;
	}

	private static Map<String, BufferedImage> symbols = new HashMap<>();

	public static BufferedImage getSymbol(String id) {
		if (!symbols.containsKey(id)) {
			try {
				InputStream in = CardRenderer.class.getClassLoader().getResourceAsStream("greenscripter/mtgrenderer/assets/magicsymbols/" + id.substring(1, id.length() - 1) + ".png");
				if (in == null) return null;
				BufferedImage symbol = ImageIO.read(in);
				symbols.put(id, symbol);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return symbols.get(id);
	}

}
