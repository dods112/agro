import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.sql.*;

public class PetAdoptionSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            DatabaseManager.initialize();
            new SplashScreen();
        });
    }
}

class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:pet_adoption.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT UNIQUE NOT NULL,
                            password TEXT NOT NULL,
                            email TEXT NOT NULL,
                            full_name TEXT NOT NULL,
                            phone_number TEXT NOT NULL,
                            address TEXT NOT NULL,
                            is_admin INTEGER DEFAULT 0,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS pets (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            species TEXT NOT NULL,
                            breed TEXT,
                            age INTEGER NOT NULL,
                            gender TEXT NOT NULL,
                            size TEXT NOT NULL,
                            color TEXT,
                            description TEXT NOT NULL,
                            image_url TEXT,
                            status TEXT DEFAULT 'AVAILABLE',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS adoptions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            pet_id INTEGER NOT NULL,
                            adoption_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            status TEXT DEFAULT 'PENDING',
                            notes TEXT,
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (pet_id) REFERENCES pets(id)
                        )
                    """);

            try {
                stmt.execute(
                        """
                                    INSERT OR IGNORE INTO users (username, password, email, full_name, phone_number, address, is_admin)
                                    VALUES ('admin', 'admin123', 'admin@petadoption.com', 'System Administrator', '0000000000', 'Admin Office', 1)
                                """);
            } catch (SQLException e) {
            }

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pets");
            if (rs.next() && rs.getInt(1) == 0) {
                insertSamplePets(conn);
            }

            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertSamplePets(Connection conn) throws SQLException {
        String[] samplePets = {
                "INSERT INTO pets (name, species, breed, age, gender, size, color, description, image_url) VALUES " +
                        "('Max', 'Dog', 'Golden Retriever', 3, 'Male', 'Large', 'Golden', 'Friendly and energetic dog, great with kids!', 'https://images.unsplash.com/photo-1633722715463-d30f4f325e24')",

                "INSERT INTO pets (name, species, breed, age, gender, size, color, description, image_url) VALUES " +
                        "('Luna', 'Cat', 'Persian', 2, 'Female', 'Medium', 'White', 'Calm and affectionate cat, loves to cuddle.', 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba')",

                "INSERT INTO pets (name, species, breed, age, gender, size, color, description, image_url) VALUES " +
                        "('Charlie', 'Dog', 'Beagle', 5, 'Male', 'Medium', 'Brown', 'Playful and curious, perfect for active families.', 'https://images.unsplash.com/photo-1505628346881-b72b27e84530')",

                "INSERT INTO pets (name, species, breed, age, gender, size, color, description, image_url) VALUES " +
                        "('Bella', 'Cat', 'Siamese', 1, 'Female', 'Small', 'Cream', 'Young and playful kitten, very social and friendly.', 'https://images.unsplash.com/photo-1573865526739-10c1de0ac088')",

                "INSERT INTO pets (name, species, breed, age, gender, size, color, description, image_url) VALUES " +
                        "('Rocky', 'Dog', 'German Shepherd', 4, 'Male', 'Large', 'Black/Brown', 'Loyal and protective, well-trained guard dog.', 'https://images.unsplash.com/photo-1568572933382-74d440642117')",

                "INSERT INTO pets (name, species, breed, age, gender, size, color, description, image_url) VALUES " +
                        "('Mittens', 'Cat', 'Tabby', 3, 'Female', 'Medium', 'Orange', 'Independent but loving cat, enjoys outdoor exploration.', 'https://images.unsplash.com/photo-1529778873920-4da4926a72c2')"
        };

        for (String sql : samplePets) {
            conn.createStatement().execute(sql);
        }
    }
}

class User {
    private final int id;
    private final String username;
    private final String email;
    private final String fullName;
    private final String phoneNumber;
    private final String address;
    private final boolean isAdmin;

    public User(int id, String username, String email, String fullName, String phoneNumber, String address,
            boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.isAdmin = isAdmin;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}

class Pet {
    private final int id;
    private final String name;
    private final String species;
    private final String breed;
    private final int age;
    private final String gender;
    private final String size;
    private final String color;
    private final String description;
    private final String imageUrl;
    private final String status;

    public Pet(int id, String name, String species, String breed, int age, String gender,
            String size, String color, String description, String imageUrl, String status) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.gender = gender;
        this.size = size;
        this.color = color;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }
}

class Adoption {
    private final int id;
    private final User user;
    private final Pet pet;
    private final java.util.Date adoptionDate;
    private final String status;
    private final String notes;

    public Adoption(int id, User user, Pet pet, java.util.Date adoptionDate, String status, String notes) {
        this.id = id;
        this.user = user;
        this.pet = pet;
        this.adoptionDate = adoptionDate;
        this.status = status;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Pet getPet() {
        return pet;
    }

    public java.util.Date getAdoptionDate() {
        return adoptionDate;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}

class SplashScreen extends JWindow {
    private static final int DISPLAY_TIME = 3000;
    private static final int WIDTH = 700;
    private static final int HEIGHT = 500;

    public SplashScreen() {
        JPanel panel = createModernSplashPanel();
        setContentPane(panel);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);

        javax.swing.Timer timer = new javax.swing.Timer(DISPLAY_TIME, e -> {
            dispose();
            new MainFrame();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private JPanel createModernSplashPanel() {
        return new JPanel() {
            private float alpha = 0f;

            {
                Timer fadeTimer = new Timer(30, e -> {
                    alpha += 0.05f;
                    if (alpha >= 1f) {
                        alpha = 1f;
                        ((Timer) e.getSource()).stop();
                    }
                    repaint();
                });
                fadeTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(67, 97, 238),
                        getWidth(), getHeight(), new Color(128, 90, 213));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                drawAnimatedShapes(g2d);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 90));
                String iconText = "PET";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(iconText)) / 2;
                int y = getHeight() / 2 - 50;

                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.drawString(iconText, x + 3, y + 3);
                g2d.setColor(Color.WHITE);
                g2d.drawString(iconText, x, y);

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 42));
                String title = "Pet Adoption System";
                fm = g2d.getFontMetrics();
                x = (getWidth() - fm.stringWidth(title)) / 2;
                g2d.drawString(title, x, y + 80);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 20));
                String subtitle = "Find your perfect companion";
                fm = g2d.getFontMetrics();
                x = (getWidth() - fm.stringWidth(subtitle)) / 2;
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.drawString(subtitle, x, y + 130);

                drawLoadingBar(g2d, y + 180);
            }

            private void drawAnimatedShapes(Graphics2D g2d) {
                g2d.setColor(new Color(255, 255, 255, 20));

                Ellipse2D circle1 = new Ellipse2D.Double(50, 50, 100, 100);
                g2d.fill(circle1);

                Ellipse2D circle2 = new Ellipse2D.Double(getWidth() - 150, getHeight() - 150, 120, 120);
                g2d.fill(circle2);

                RoundRectangle2D rect = new RoundRectangle2D.Double(getWidth() - 180, 80, 80, 80, 20, 20);
                g2d.fill(rect);
            }

            private void drawLoadingBar(Graphics2D g2d, int y) {
                int barWidth = 300;
                int barHeight = 6;
                int x = (getWidth() - barWidth) / 2;

                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(x, y, barWidth, barHeight, barHeight, barHeight);

                int progress = (int) (barWidth * alpha);
                GradientPaint loadingGradient = new GradientPaint(
                        x, y, new Color(100, 255, 218),
                        x + progress, y, new Color(72, 209, 204));
                g2d.setPaint(loadingGradient);
                g2d.fillRoundRect(x, y, progress, barHeight, barHeight, barHeight);
            }
        };
    }
}

class MainFrame extends JFrame {
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final Color PRIMARY_COLOR = new Color(67, 97, 238);
    private static final Color SECONDARY_COLOR = new Color(100, 88, 255);
    private static final Color ACCENT_COLOR = new Color(72, 209, 204);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color TEXT_COLOR = new Color(15, 23, 42);
    private static final Color LIGHT_BG = new Color(248, 250, 252);

    private User currentUser;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final JPanel headerPanel;

    public MainFrame() {
        setTitle("Pet Adoption System");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(LIGHT_BG);

        headerPanel = createModernHeader();

        mainPanel.add(new LoginPanel(this), "login");
        mainPanel.add(new RegisterPanel(this), "register");
        mainPanel.add(new BrowsePetsPanel(this), "browse");
        mainPanel.add(new MyAdoptionsPanel(this), "myAdoptions");
        mainPanel.add(new AdminPanel(this), "admin");

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        showPanel("browse");
        setVisible(true);
    }

    private JPanel createModernHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(WINDOW_WIDTH, 80));
        header.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("PET ADOPTION");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
        leftPanel.add(logoLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        updateHeaderButtons(rightPanel);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private void updateHeaderButtons(JPanel rightPanel) {
        rightPanel.removeAll();

        if (currentUser == null) {
            addModernButton(rightPanel, "Browse Pets", new Color(255, 255, 255, 30), Color.WHITE,
                    e -> showPanel("browse"));
            addModernButton(rightPanel, "Login", Color.WHITE, PRIMARY_COLOR, e -> showPanel("login"));
            addModernButton(rightPanel, "Register", ACCENT_COLOR, Color.WHITE, e -> showPanel("register"));
        } else {
            JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            userInfoPanel.setOpaque(false);

            if (currentUser.isAdmin()) {
                JLabel adminBadge = createBadge("ADMIN", DANGER_COLOR);
                userInfoPanel.add(adminBadge);
            }

            JLabel userLabel = new JLabel(currentUser.getUsername());
            userLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            userLabel.setForeground(Color.WHITE);
            userInfoPanel.add(userLabel);

            rightPanel.add(userInfoPanel);

            if (!currentUser.isAdmin()) {
                addModernButton(rightPanel, "Browse", new Color(255, 255, 255, 30), Color.WHITE,
                        e -> showPanel("browse"));
                addModernButton(rightPanel, "My Adoptions", new Color(255, 255, 255, 30), Color.WHITE,
                        e -> showPanel("myAdoptions"));
            }

            addModernButton(rightPanel, "Logout", DANGER_COLOR, Color.WHITE, e -> logout());
        }

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private JLabel createBadge(String text, Color color) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setBackground(color);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return badge;
    }

    private void addModernButton(JPanel panel, String text, Color bgColor, Color fgColor,
            java.awt.event.ActionListener listener) {
        JButton button = createModernButton(text, bgColor, fgColor);
        button.addActionListener(listener);
        panel.add(button);
    }

    private JButton createModernButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));

        return button;
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);

        switch (panelName) {
            case "browse" -> ((BrowsePetsPanel) mainPanel.getComponent(2)).refreshPets();
            case "myAdoptions" -> ((MyAdoptionsPanel) mainPanel.getComponent(3)).refreshAdoptions();
            case "admin" -> ((AdminPanel) mainPanel.getComponent(4)).refreshData();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateHeaderButtons((JPanel) headerPanel.getComponent(1));

        if (user != null) {
            showPanel(user.isAdmin() ? "admin" : "browse");
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void logout() {
        currentUser = null;
        updateHeaderButtons((JPanel) headerPanel.getComponent(1));
        showPanel("browse");
        showModernDialog("Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showModernDialog(String message, String title, int messageType) {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}