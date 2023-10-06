package greenscripter.mtgrenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CardRenderer {

	public static void main(String[] args) throws Exception {
		String name = "Zombie Pilferer";
		String text = "Whenever you attack with one or more zombies, draw that many cards, then discard that many cards. You may put all Artifact cards from among them onto the battlefield. Then each player loses the game. {G} {R} {J}";
		text = text.replace("~", name);

		BufferedImage image = renderCard(name, "Legendary Creature — Zombie Wizard", "U B R RG", text, "2/3", false, null);
		renderMetaText(image, "Stable Diffusion", "AI   •   EN", "000 / 999 C", "By @Overlord", true);

		image = renderCard(name, "Legendary Creature — Zombie Wizard", "U B R RG J", text, "2/3", false, null);
		renderMetaText(image, "Stable Diffusion", "AI   •   EN", "000 / 999 C", "By @Overlord", true);

		ImageIO.write(image, "png", new File("Output.png"));

	}

	public static Font beleren;
	public static Font belerenSmallCaps;
	public static Font mplantin;
	public static Font gotham;
	static {
		try {
			beleren = Font.createFont(Font.TRUETYPE_FONT, CardRenderer.class.getClassLoader().getResourceAsStream("greenscripter/mtgrenderer/assets/Beleren.ttf"));
			belerenSmallCaps = Font.createFont(Font.TRUETYPE_FONT, CardRenderer.class.getClassLoader().getResourceAsStream("greenscripter/mtgrenderer/assets/BelerenSmallCaps.ttf"));
			mplantin = Font.createFont(Font.TRUETYPE_FONT, CardRenderer.class.getClassLoader().getResourceAsStream("greenscripter/mtgrenderer/assets/MPlantin.ttf"));
			gotham = Font.createFont(Font.TRUETYPE_FONT, CardRenderer.class.getClassLoader().getResourceAsStream("greenscripter/mtgrenderer/assets/gotham-medium.ttf"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage renderCard(String name, String type, String cost, String text, String powerToughness, boolean isRare, BufferedImage art) throws Exception {
		String[] symbols = cost.split(" ");
		Frame frame = new Frame(cost, powerToughness != null, type, text);
		if (isRare) {
			frame.info.add(FrameInfo.RARE);
		}
		BufferedImage frameImage = frame.getFrame();
		BufferedImage image = new BufferedImage(frameImage.getWidth(), frameImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		if (art != null) {
			double scaleX = 579 / (double) art.getWidth();
			double scaleY = 428 / (double) art.getHeight();

			double scale = Math.max(scaleX, scaleY);

			int width = (int) (art.getWidth() * scale);
			int height = (int) (art.getHeight() * scale);
			g.drawImage(art, 45, 96, width, height, null);
		}
		g.drawImage(frameImage, 0, 0, null);

		g.setColor(Color.black);

		int firstSymbolX = 580 + 10 - 35 * (symbols.length - 1);
		int x = firstSymbolX;
		int y = 55 - 3;
		for (String s : symbols) {
			if (s.isEmpty()) continue;
			g.fillOval(x - 1, y + 3, 32, 32);

			BufferedImage bufferedImage = getSymbol(s);
			g.drawImage(bufferedImage, x, y, 32, 32, null);
			x += 35;

		}

		Font font = beleren.deriveFont(36f);
		float fontSize = 36;
		do {
			g.setFont(font.deriveFont(fontSize));
			fontSize--;
		} while (TextRenderer.getLength(g, name) > firstSymbolX - (100 - 44) && fontSize > 1);

		TextRenderer.render(g, name, 100 - 44, 100 - 17);

		g.setFont(font.deriveFont(30f).deriveFont(Font.BOLD));
		fontSize = 30;
		do {
			g.setFont(font.deriveFont(fontSize));
			fontSize--;
		} while (TextRenderer.getLength(g, type) > 524 && fontSize > 1);
		g.drawString(type, 100 - 44, 550 + 13);

		g.setFont(font.deriveFont(35f).deriveFont(Font.BOLD));

		FontMetrics fm = g.getFontMetrics();
		if (powerToughness != null) {
			if (frame.info.contains(FrameInfo.VEHICLE)) {
				g.setColor(Color.white);
			}
			g.drawString(powerToughness, 580 - fm.stringWidth(powerToughness) / 2, 860 - 2 + fm.getAscent() / 2);
			g.setColor(Color.black);
		}

		font = mplantin.deriveFont(33f);
		g.setFont(font);
		renderText(text, 100 - 44 + 2, 620 + 5 - 4, 550, 240, g);

		return image;

	}

	public static BufferedImage renderMetaText(BufferedImage image, String name, String setLang, String setCountRarity, String other, boolean hasStats) {
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		BufferedImage mark = Frame.getImage("miscmasks", "artistmarker.png");
		g.drawImage(mark, 0, 0, null);

		g.setFont(belerenSmallCaps.deriveFont(16f));
		g.setColor(Color.white);
		g.drawString(name, 148, 905);

		g.setFont(gotham.deriveFont(15f));
		g.drawString(setLang, 43, 905);
		g.drawString(setCountRarity, 43, 888);

		g.setFont(mplantin.deriveFont(15f));
		FontMetrics fm = g.getFontMetrics();
		g.drawString(other, 627 - fm.stringWidth(other), hasStats ? 905 : 888);

		return image;

	}

	public static void renderText(String text, int x, int y, int width, int height, Graphics2D g) {
		List<String> lines = new ArrayList<>();
		String[] words = text.replace("\n", "\n ").split(" ");

		float fontSize = g.getFont().getSize2D();
		FontMetrics fm = g.getFontMetrics();

		do {
			lines.clear();
			String line = "";

			for (String s : words) {

				if (TextRenderer.getLength(fm, line + "" + s) > width) {
					lines.add(line);
					line = "";
				} else if (line.endsWith("\n ")) {
					lines.add(line);
					line = "";
				}
				line += s + " ";
			}
			lines.add(line);
			if (lines.size() * (fm.getMaxAscent() + fm.getMaxDescent()) > height) {
				fontSize -= 1;
				g.setFont(g.getFont().deriveFont(fontSize));
				fm = g.getFontMetrics();
			}
		} while (lines.size() * (fm.getMaxAscent() + fm.getMaxDescent()) > height && fontSize > 1);

		for (String s : lines) {
			TextRenderer.render(g, s, x, y);
			if (s.trim().isEmpty()) {
				y += (fm.getMaxAscent() + fm.getMaxDescent()) / 2;

			} else {
				y += fm.getMaxAscent() + fm.getMaxDescent();
			}
		}

	}

	public static String manaSymbol(String s) {
		s = s.toUpperCase();
		if (s.equals("UW")) {
			return "WU";
		}
		if (s.equals("WR")) {
			return "RW";
		}
		if (s.equals("BU")) {
			return "UB";
		}
		if (s.equals("GB")) {
			return "BG";
		}
		if (s.equals("GR")) {
			return "RG";
		}
		if (s.equals("RU")) {
			return "UR";
		}
		if (s.equals("BW")) {
			return "WB";
		}
		if (s.equals("RB")) {
			return "BR";
		}
		if (s.equals("WG")) {
			return "GW";
		}
		if (s.equals("UG")) {
			return "GU";
		}

		if (s.equals("W2")) {
			return "2W";
		}
		if (s.equals("U2")) {
			return "2U";
		}
		if (s.equals("B2")) {
			return "2B";
		}
		if (s.equals("R2")) {
			return "2R";
		}
		if (s.equals("G2")) {
			return "2G";
		}

		if (s.equals("WP")) {
			return "PW";
		}
		if (s.equals("UP")) {
			return "PU";
		}
		if (s.equals("BP")) {
			return "PB";
		}
		if (s.equals("RP")) {
			return "PR";
		}
		if (s.equals("GP")) {
			return "PG";
		}

		return s;
	}

	public static BufferedImage getSymbol(String a) {
		if (!frames.containsKey(a)) {
			float[] f = new float[100];

			Arrays.fill(f, 1f / 100f);
			Kernel kernel = new Kernel(10, 10, f);

			BufferedImageOp op = new ConvolveOp(kernel);

			BufferedImage bufferedImage = TextRenderer.getSymbol("{" + manaSymbol(a) + "}");
			if (bufferedImage == null) return null;
			bufferedImage = op.filter(bufferedImage, null);
			frames.put(a, bufferedImage);
		}
		return frames.get(a);
	}

	private static Map<String, BufferedImage> frames = new HashMap<>();

	private static class Frame {

		List<FrameInfo> info = new ArrayList<>();

		public Frame(String fromCMC, boolean creature, String type, String text) {
			String[] parts = fromCMC.split(" ");
			int length = 0;
			for (String s : parts) {
				if (s.length() > length) length = s.length();
			}
			if (fromCMC.contains("U")) {
				info.add(FrameInfo.BLUE);
			}
			if (fromCMC.contains("R")) {
				info.add(FrameInfo.RED);
			}
			if (fromCMC.contains("G")) {
				info.add(FrameInfo.GREEN);
			}
			if (fromCMC.contains("B")) {
				info.add(FrameInfo.BLACK);
			}
			if (fromCMC.contains("W")) {
				info.add(FrameInfo.WHITE);
			}
			if (info.size() == 0) {
				info.add(FrameInfo.COLORLESS);
			}
			if (info.size() > 2) {
				info.clear();
				info.add(FrameInfo.GOLD);
			}
			if (info.size() == 2) {
				if (length == 1) info.add(FrameInfo.GOLD);
			}
			if (type.contains("Land")) {
				info.clear();
				info.addAll(getLandColors(type, text));
				info.add(FrameInfo.LAND);
			}
			if (creature) info.add(FrameInfo.CREATURE);
			if (type.contains("Legendary")) {
				info.add(FrameInfo.LEGENDARY);
			}
			if (type.contains("Vehicle")) {
				info.add(FrameInfo.VEHICLE);
			}
			if (type.contains("Artifact")) {
				info.add(FrameInfo.ARTIFACT);
			}

		}

		private static Set<FrameInfo> getLandColors(String type, String text) {
			String[] lines = text.split("\n");
			text = "";
			for (String s : lines) {
				if (s.contains(":")) {
					text += s.substring(s.indexOf(":"));
				} else {
					text += s;
				}
			}
			Set<FrameInfo> info = new HashSet<>();
			if (type.contains("Plains") || text.contains("{W}") || text.contains("{2W}") || text.contains("{PW}") || text.contains("{WU}") || text.contains("{RW}") || text.contains("{WB}") || text.contains("{GW}")) {
				info.add(FrameInfo.WHITE);
			}
			if (type.contains("Island") || text.contains("{U}") || text.contains("{2U}") || text.contains("{PU}") || text.contains("{WU}") || text.contains("{UB}") || text.contains("{UR}") || text.contains("{GU}")) {
				info.add(FrameInfo.BLUE);
			}
			if (type.contains("Swamp") || text.contains("{B}") || text.contains("{2B}") || text.contains("{PB}") || text.contains("{UB}") || text.contains("{BG}") || text.contains("{WB}") || text.contains("{BR}")) {
				info.add(FrameInfo.BLACK);
			}
			if (type.contains("Mountain") || text.contains("{R}") || text.contains("{2R}") || text.contains("{PR}") || text.contains("{RW}") || text.contains("{RG}") || text.contains("{UR}") || text.contains("{BR}")) {
				info.add(FrameInfo.RED);
			}
			if (type.contains("Forest") || text.contains("{G}") || text.contains("{2G}") || text.contains("{PG}") || text.contains("{BG}") || text.contains("{RG}") || text.contains("{GW}") || text.contains("{GB}")) {
				info.add(FrameInfo.GREEN);
			}
			if (info.size() == 0) {
				info.add(FrameInfo.COLORLESS);
			}
			if (info.size() > 2 || text.contains("any color") || text.contains("chosen color") || text.contains("mana in any combination of colors")) {
				info.clear();
				info.add(FrameInfo.GOLD);
			}
			return info;
		}

		public boolean isMono() {
			int c = 0;
			if (info.contains(FrameInfo.BLACK)) {
				c++;
			}
			if (info.contains(FrameInfo.BLUE)) {
				c++;
			}
			if (info.contains(FrameInfo.GREEN)) {
				c++;
			}
			if (info.contains(FrameInfo.RED)) {
				c++;
			}
			if (info.contains(FrameInfo.WHITE)) {
				c++;
			}
			if (info.contains(FrameInfo.COLORLESS)) {
				c++;
			}
			return c == 1;
		}

		public String colorName() {
			String name = "";
			if (info.contains(FrameInfo.BLACK) && info.contains(FrameInfo.GREEN)) {
				name = "BG";
			}
			if (info.contains(FrameInfo.BLACK) && info.contains(FrameInfo.RED)) {
				name = "BR";
			}
			if (info.contains(FrameInfo.GREEN) && info.contains(FrameInfo.BLUE)) {
				name = "GU";
			}
			if (info.contains(FrameInfo.GREEN) && info.contains(FrameInfo.WHITE)) {
				name = "GW";
			}
			if (info.contains(FrameInfo.RED) && info.contains(FrameInfo.GREEN)) {
				name = "RG";
			}
			if (info.contains(FrameInfo.RED) && info.contains(FrameInfo.WHITE)) {
				name = "RW";
			}
			if (info.contains(FrameInfo.BLUE) && info.contains(FrameInfo.BLACK)) {
				name = "UB";
			}
			if (info.contains(FrameInfo.WHITE) && info.contains(FrameInfo.BLACK)) {
				name = "WB";
			}
			if (info.contains(FrameInfo.WHITE) && info.contains(FrameInfo.BLUE)) {
				name = "WU";
			}
			if (info.contains(FrameInfo.BLUE) && info.contains(FrameInfo.RED)) {
				name = "UR";
			}
			if (isMono()) {
				if (info.contains(FrameInfo.BLACK)) {
					name = "B";
				}
				if (info.contains(FrameInfo.BLUE)) {
					name = "U";
				}
				if (info.contains(FrameInfo.GREEN)) {
					name = "G";
				}
				if (info.contains(FrameInfo.RED)) {
					name = "R";
				}
				if (info.contains(FrameInfo.WHITE)) {
					name = "W";
				}
				if (info.contains(FrameInfo.COLORLESS)) {
					name = "C";
				}
			}
			return name;
		}

		public String frameName() {
			if (info.contains(FrameInfo.LAND) && info.contains(FrameInfo.COLORLESS)) {
				return "PlainLand" + (info.contains(FrameInfo.RARE) ? "Rare" : "") + ".png";
			}
			String name = colorName();

			if (info.contains(FrameInfo.GOLD)) {
				name += "Gold";
			}
			if (info.contains(FrameInfo.RARE)) {
				name += "Rare";
			}

			return name + ".png";
		}

		public String overlayName() {

			String name = colorName();
			if (info.contains(FrameInfo.GOLD) && name.length() == 0) {
				name += "Gold";
			}

			return name + ".png";
		}

		public BufferedImage getFrame() throws IOException {
			BufferedImage frameOriginal = getImage("frames", frameName());

			BufferedImage frame = new BufferedImage(frameOriginal.getWidth(), frameOriginal.getHeight(), BufferedImage.TYPE_INT_ARGB);
			frame.createGraphics().drawImage(frameOriginal, 0, 0, null);

			if (info.contains(FrameInfo.CREATURE)) {
				if (info.contains(FrameInfo.VEHICLE)) {
					frame.getGraphics().drawImage(getImage("creaturemasks", "Vehicle.png"), 0, 0, null);
				} else if (isMono()) {
					if (info.contains(FrameInfo.BLACK)) {
						frame.getGraphics().drawImage(getImage("creaturemasks", "Black.png"), 0, 0, null);
					}
					if (info.contains(FrameInfo.BLUE)) {
						frame.getGraphics().drawImage(getImage("creaturemasks", "Blue.png"), 0, 0, null);
					}
					if (info.contains(FrameInfo.GREEN)) {
						frame.getGraphics().drawImage(getImage("creaturemasks", "Green.png"), 0, 0, null);
					}
					if (info.contains(FrameInfo.RED)) {
						frame.getGraphics().drawImage(getImage("creaturemasks", "Red.png"), 0, 0, null);
					}
					if (info.contains(FrameInfo.WHITE)) {
						frame.getGraphics().drawImage(getImage("creaturemasks", "White.png"), 0, 0, null);
					}
					if (info.contains(FrameInfo.COLORLESS)) {
						frame.getGraphics().drawImage(getImage("creaturemasks", "Colorless.png"), 0, 0, null);
					}
				} else if (info.contains(FrameInfo.GOLD)) {
					frame.getGraphics().drawImage(getImage("creaturemasks", "Gold.png"), 0, 0, null);
				} else {
					frame.getGraphics().drawImage(getImage("creaturemasks", "Split.png"), 0, 0, null);
				}
			}
			if (info.contains(FrameInfo.ARTIFACT)) {
				frame.getGraphics().drawImage(getImage("miscmasks", "Artifact.png"), 0, 0, null);
			}
			if (info.contains(FrameInfo.LAND)) {
				frame.getGraphics().drawImage(getImage("miscmasks", "Land.png"), 0, 0, null);
			}
			if (info.contains(FrameInfo.VEHICLE)) {
				frame.getGraphics().drawImage(getImage("miscmasks", "Vehicle.png"), 0, 0, null);
			}
			if (info.contains(FrameInfo.LEGENDARY)) {
				frame.getGraphics().drawImage(getImage("legendarymasks", overlayName()), 0, 0, null);
				if (info.contains(FrameInfo.GOLD)) {
					frame.getGraphics().drawImage(getImage("legendarymasks", "GoldText.png"), 0, 0, null);
				}
				if (!isMono() && !info.contains(FrameInfo.GOLD)) {
					frame.getGraphics().drawImage(getImage("legendarymasks", "MultiText.png"), 0, 0, null);
				}
			}
			return frame;
		}

		@Override
		public String toString() {
			return "Frame [info=" + info + "]";
		}

		public static BufferedImage getImage(String a, String b) {
			if (!frames.containsKey(a + b)) {
				try {
					BufferedImage symbol = ImageIO.read(CardRenderer.class.getClassLoader().getResourceAsStream("greenscripter/mtgrenderer/assets/" + a + "/" + b));
					frames.put(a + b, symbol);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return frames.get(a + b);
		}

		private static Map<String, BufferedImage> frames = new HashMap<>();

	}

	private enum FrameInfo {
		RED, GREEN, BLUE, WHITE, BLACK, COLORLESS, GOLD, RARE, CREATURE, LEGENDARY, VEHICLE, LAND, ARTIFACT
	}

}
