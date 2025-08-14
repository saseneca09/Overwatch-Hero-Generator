package overwatch;

// Imports
import javax.swing.*;		// Swing UI components (JFrame, JLabel, JButton, SwingConstants, BorderFactory)
import java.awt.*;			// AWT Classes (Font, Color, Dimension, Image)
import java.util.concurrent.ThreadLocalRandom; // Random selection utility (thread-safe)
import java.io.File;		// File API for checking/reading image files from disk

/**
 * <h2>OverwatchHeroGenerator</h2>
 * A small Java Swing application that randomly selects an Overwatch hero
 * from a chosen role (Tank, Damage, Support) and displays the hero's image
 * along with their name underneath the image.
 *
 * <h3>How images are loaded</h3>
 * Images are expected to be placed in a folder called <b>images/</b> that sits
 * next to where you run the program (i.e., the working directory).
 * Each image filename must match the hero name exactly with a <code>.png</code> extension.
 * <p>Examples:</p>
 * <ul>
 *		<li>images/D.Va.png</li>
 *		<li>images/Soldier 76.png</li>
 *		<li>images/Wrecking Ball.png</li>
 * </ul>
 *
 * <h3>Next ideas</h3>
 * <ul>
 *		<li>Add a "No repeats until all used" tracker per role.</li>
 *		<li>Add a "Random (Any Role)" button.</li>
 *		<li>Add a history panel of recent picks.</li>
 * </ul>
 *
 * @author Sam Seneca
 */
public class OverwatchHeroGenerator {

	/** Main application window (top-level Swing container). */
	private JFrame frame;

	/** Desired frame width in pixels */
	private int frameWidth = 500;

	/** Desired frame height in pixels */
	private int frameHeight = 500;

	/** Label that shows the selected hero's name under the image. */
	private JLabel resultLabel;

	/** Label that displays the selected hero's image. */
	private JLabel imageLabel;

	// ========================
	//    Hero Pools by Role
	// ========================

	/** List of Tank heroes */
	private final String[] TANKS = {
		"D.Va", "Doomfist", "Hazard", "Junker Queen", "Mauga", "Orisa",
		"Ramattra", "Reinhardt", "Roadhog", "Sigma", "Winston",
		"Wrecking Ball", "Zarya"
	};

	/** List of Damage heroes */
	private final String[] DAMAGES = {
		"Ashe", "Bastion", "Cassidy", "Echo", "Freja", "Genji", "Hanzo",
		"Junkrat", "Mei", "Pharah", "Reaper", "Sojourn", "Soldier 76",
		"Sombra", "Symmetra", "Torbjorn", "Tracer", "Venture", "Widowmaker"
	};

	/** List of Support heroes */
	private final String[] SUPPORTS = {
		"Ana", "Baptiste", "Brigitte", "Illari", "Juno", "Kiriko", "Lifeweaver",
		"Lucio", "Mercy", "Moira", "Zenyatta"
	};

	/**
	 * Creates the OverwatchHeroGenerator app instance.
	 * Initializes fields to their default values.
	 */
	public OverwatchHeroGenerator() {}

	/**
	 * Program entry point. Runs the GUI on the Event Dispatch Thread (EDT).
	 *
	 * @param args Command-line arguments (not used)
	 */
	public static void main(String[] args) {
		// Using invokeLater ensures Swing components are created/updated on the EDT
		SwingUtilities.invokeLater(() -> new OverwatchHeroGenerator().createAndShowGUI());
	}

	/**
	 * Creates the core UI and makes the frame visible.
	 * Breaks setup into smaller methods for readability and maintainability.
	 */
	private void createAndShowGUI() {
		frame = new JFrame("Overwatch Hero Generator"); // Window title
		setupFrame();			// size, close behavior, layout
		addTitle();				// top title label
		addImageArea();			// main image display area
		addResultLabel();		// hero name label (under the image)
		addRoleButtons();		// Tank/Damage/Support buttons with actions
		frame.setVisible(true); // Show window last, after components are added
	}

	/**
	 * Configures the main window properties:
	 * <ul>
	 *		<li>Size</li>
	 *		<li>Close operation</li>
	 *		<li>Layout manager (none -> absolute positioning)</li>
	 *		<li>Resizable (disabled)</li>
	 * </ul>
	 */
	private void setupFrame() {
		frame.setSize(frameWidth, frameHeight);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exits app on close
		frame.setLayout(null); // absolute positioning (x,y,width,height for each component)
		frame.setResizable(false); // keep a fixed size to match out absolute layout
	}

	/**
	 * Adds a centered title label at the top of the window.
	 * Uses a bold font and occupies the full width for easy centering.
	 */
	private void addTitle() {
		JLabel title = new JLabel("Overwatch Hero Generator");
		title.setFont(new Font("Verdana", Font.BOLD, 20));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		// x=0 to span full width 500; y=15 a bit below the top edge; height ~30px
		title.setBounds(0, 15, 500, 30);
		frame.add(title);
	}

	/**
	 * Adds the image display area.
	 * This is a JLabel with a neutral background and border that will hold an ImageIcon.
	 * It's sized to be wide and not too tall.
	 */
	private void addImageArea() {
		imageLabel = new JLabel("", SwingConstants.CENTER); // center icon horizontally
		// Position: left margin 50, top 60; width = frameWidth - 100; height 280
		imageLabel.setBounds(50, 60, frameWidth - 100, 280);
		imageLabel.setOpaque(true); // allow background color to show
		imageLabel.setBackground(new Color(245, 245, 245)); // light gray background
		imageLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220))); // subtle border
		frame.add(imageLabel);
	}

	/**
	 * Adds the result label (hero name) under the image area.
	 * Starts with a helpful prompt until a role is clicked.
	 */
	private void addResultLabel() {
		resultLabel = new JLabel("Pick a role to generate a hero!", SwingConstants.CENTER);
		resultLabel.setFont(new Font("Verdana", Font.PLAIN, 18));
		// Placed directly below the image area: y = 60 + 280 + 10 -> 350
		resultLabel.setBounds(50, 60 + 280 + 10, frameWidth - 100, 28);
		frame.add(resultLabel);
	}

	/**
	 * Creates the three role buttons (Tank, Damage, Support), sizes them uniformly,
	 * positions them centered along the bottom, and wires up click actions
	 * so each one selects from its respective hero pool.
	 *
	 * The code ensures all buttons share the largest preferred width
	 * (plus padding), so they look uniform even if text lengths differ.
	 */
	private void addRoleButtons() {
		// Keep a consistent look across buttons
		Font btnFont = new Font("Verdana", Font.PLAIN, 14);

		// Create buttons
		JButton tank = createButton("Tank", btnFont);
		JButton damage = createButton("Damage", btnFont);
		JButton support = createButton("Support", btnFont);

		// --------------- Uniform sizing logic ---------------
		// Ask each button for its natural preferred size
		Dimension tSize = tank.getPreferredSize();		// Tank button's preferred size
		Dimension dSize = damage.getPreferredSize();	// Damage button's preferred size
		Dimension sSize = support.getPreferredSize();	// Support button's preferred size

		int padding = 16; // add extra horizontal padding for nicer appearance

		// Compute a single uniform width = the max preferred width + padding
		int uniformWidth = Math.max(tSize.width, Math.max(dSize.width, sSize.width)) + padding;

		// Use the tallest preferred height among the three to avoid clipping
		int uniformHeight = Math.max(tSize.height, Math.max(dSize.height, sSize.height));

		// --------------- Horizontal centering math ---------------
		int spacing = 20; // gap between buttons
		int totalWidth = uniformWidth * 3 + spacing * 2; // three buttons + two gaps
		int startX = (frameWidth - totalWidth) / 2; // left edge for first button
		int y = frameHeight - uniformHeight - 40; // near bottom, 40px margin

		// --------------- Place the buttons ---------------
		tank.setBounds(startX, y, uniformWidth, uniformHeight);
		damage.setBounds(startX + uniformWidth + spacing, y, uniformWidth, uniformHeight);
		support.setBounds(startX + (uniformWidth + spacing) * 2, y, uniformWidth, uniformHeight);

		// --------------- Wire up actions ---------------
		// Each click chooses a random hero from the corresponding role array
		tank.addActionListener(e -> showRandomHero("Tank", TANKS));
		damage.addActionListener(e -> showRandomHero("Damage", DAMAGES));
		support.addActionListener(e -> showRandomHero("Support", SUPPORTS));

		// Add buttons to the frame last so they're visible
		frame.add(tank);
		frame.add(damage);
		frame.add(support);
	}

	/**
	 * Helper for creating a JButton with the given text and font applied.
	 *
	 * @param text button label text
	 * @param font font to apply to the button
	 * @return a configured JButton instance
	 */
	private JButton createButton(String text, Font font) {
		JButton button = new JButton(text);
		button.setFont(font);
		return button;
	}

	/**
	 * Chooses a random hero from the provided pool and updates both the
	 * hero name label and the image display.
	 *
	 * @param role role name ("Tank", "Damage", "Support")
	 * @param pool array of hero names for the selected role
	 */
	private void showRandomHero(String role, String[] pool) {
		// Guard: empty pool should not happen, but handle gracefully
		if (pool.length == 0) {
			resultLabel.setText("(no heroes configured)");
			setHeroImage(null); // clear any existing image
			return;
		}

		// Pick an index in [0, pool.length)
		int idx = ThreadLocalRandom.current().nextInt(pool.length);

		// Retrieve the hero name and update the UI
		String hero = pool[idx];

		// Show the hero name beneath the image
		resultLabel.setText(hero);

		// Attempt the load and display the hero's image based on the name
		setHeroImage(hero);
	}

	/**
	 * Loads and displays the hero's image from disk and displays it in {@link #imageLabel}.
	 * The image is scaled to fit within the label's rectangle while preserving aspect ratio.
	 * If the image is missing, a friendly text message appears in the image area.
	 *
	 * @param hero the selected hero's name; must match the PNG filename in the images folder
	 */
	private void setHeroImage(String hero) {
		// If null is passed, clear the image area and text
		if (hero == null) {
			imageLabel.setIcon(null);
			imageLabel.setText("");
			return;
		}

		// Build file path like: images/Genji.png
		// IMPORTANT: the filename must exactly match the hero string (case/punctuation/spacing)
		String filePath = "images/" + hero + ".png";

		// File check lets us show a helpful message instead of a silent failure
		File imgFile = new File(filePath);
		if (!imgFile.exists()) {
			imageLabel.setIcon(null);
			imageLabel.setText("No image found for " + hero);
			return;
		}

		// Load the image as an icon
		ImageIcon icon = new ImageIcon(filePath);

		// Determine the available display area in the label
		int maxW = imageLabel.getWidth();
		int maxH = imageLabel.getHeight();

		// If called before the layout has finalized, width/height can be 0; use fallbacks
		if (maxW <= 0 || maxH <= 0) {
			maxW = 400; // chosen to match the setBounds size roughly
			maxH = 280;
		}

		// Original image dimensions
		int ow = icon.getIconWidth();
		int oh = icon.getIconHeight();

		// Compute a scale that preserves aspect ratio and fits within (maxW, maxH)
		double scale = Math.min((double) maxW / ow, (double) maxH / oh);

		// Compute the new width/height after scaling
		int nw = Math.max(1, (int) Math.round(ow * scale));
		int nh = Math.max(1, (int) Math.round(oh * scale));

		// Create a smoothly scaled instance of the image
		Image scaled = icon.getImage().getScaledInstance(nw, nh, Image.SCALE_SMOOTH);

		// Remove any placeholder text and set the scaled icon
		imageLabel.setIcon(new ImageIcon(scaled));
		imageLabel.setText("");
	}
}