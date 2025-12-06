import javax.swing.*;
import java.awt.*;
import java.sql.*;

class LoginPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color PRIMARY_COLOR = new Color(102, 126, 234);
    private static final Color TEXT_COLOR = new Color(30, 60, 114);
    private static final Dimension FORM_SIZE = new Dimension(400, 350);
    private static final Dimension FIELD_SIZE = new Dimension(320, 70);

    private final MainFrame mainFrame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND_COLOR);

        JPanel formPanel = createFormPanel();
        add(formPanel);

        usernameField = (JTextField) getFieldComponent(formPanel, 2);
        passwordField = (JPasswordField) getFieldComponent(formPanel, 4);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)));
        formPanel.setPreferredSize(FORM_SIZE);

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(titleLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        formPanel.add(createFieldPanel("Username", createTextField()));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(createFieldPanel("Password", passwordField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton loginBtn = createStyledButton("Login", PRIMARY_COLOR);
        loginBtn.addActionListener(e -> login());
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(loginBtn);

        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel registerLabel = new JLabel(
                "<html><center>Don't have an account? <a href='#'>Register here</a></center></html>");
        registerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainFrame.showPanel("register");
            }
        });
        formPanel.add(registerLabel);

        return formPanel;
    }

    private JComponent getFieldComponent(JPanel parent, int index) {
        JPanel fieldPanel = (JPanel) parent.getComponent(index);
        return (JComponent) fieldPanel.getComponent(2);
    }

    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(FIELD_SIZE);

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(jLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        field.setMaximumSize(new Dimension(320, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(field);

        return panel;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(320, 40));
        return button;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getInt("is_admin") == 1);

                mainFrame.setCurrentUser(user);
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

class RegisterPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color PRIMARY_COLOR = new Color(102, 126, 234);
    private static final Color TEXT_COLOR = new Color(30, 60, 114);
    private static final Dimension FORM_SIZE = new Dimension(450, 600);
    private static final Dimension FIELD_SIZE = new Dimension(370, 65);

    private final MainFrame mainFrame;
    private final JTextField usernameField, emailField, fullNameField, phoneField, addressField;
    private final JPasswordField passwordField;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND_COLOR);

        JPanel formPanel = createFormPanel();
        add(formPanel);

        usernameField = (JTextField) getFieldComponent(formPanel, 2);
        passwordField = (JPasswordField) getFieldComponent(formPanel, 4);
        emailField = (JTextField) getFieldComponent(formPanel, 6);
        fullNameField = (JTextField) getFieldComponent(formPanel, 8);
        phoneField = (JTextField) getFieldComponent(formPanel, 10);
        addressField = (JTextField) getFieldComponent(formPanel, 12);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)));
        formPanel.setPreferredSize(FORM_SIZE);

        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(titleLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        formPanel.add(createFieldPanel("Username", createTextField()));
        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(createFieldPanel("Password", passwordField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formPanel.add(createFieldPanel("Email", createTextField()));
        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formPanel.add(createFieldPanel("Full Name", createTextField()));
        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formPanel.add(createFieldPanel("Phone Number", createTextField()));
        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formPanel.add(createFieldPanel("Address", createTextField()));
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton registerBtn = createStyledButton("Register", PRIMARY_COLOR);
        registerBtn.addActionListener(e -> register());
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(registerBtn);

        return formPanel;
    }

    private JComponent getFieldComponent(JPanel parent, int index) {
        JPanel fieldPanel = (JPanel) parent.getComponent(index);
        return (JComponent) fieldPanel.getComponent(2);
    }

    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(FIELD_SIZE);

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(jLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        field.setMaximumSize(new Dimension(370, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(field);

        return panel;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(370, 40));
        return button;
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() ||
                fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO users (username, password, email, full_name, phone_number, address) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, phone);
            pstmt.setString(6, address);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    User user = new User(userId, username, email, fullName, phone, address, false);
                    mainFrame.setCurrentUser(user);
                    JOptionPane.showMessageDialog(this, "Registration successful!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        fullNameField.setText("");
        phoneField.setText("");
        addressField.setText("");
    }
}

class BrowsePetsPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color PRIMARY_COLOR = new Color(102, 126, 234);
    private static final Color TEXT_COLOR = new Color(30, 60, 114);

    private final MainFrame mainFrame;
    private final JPanel petsGridPanel;

    public BrowsePetsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Available Pets for Adoption");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        petsGridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        petsGridPanel.setBackground(BACKGROUND_COLOR);
        petsGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(petsGridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refreshPets();
    }

    public void refreshPets() {
        petsGridPanel.removeAll();

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM pets WHERE status = 'AVAILABLE'")) {

            while (rs.next()) {
                Pet pet = new Pet(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("species"),
                        rs.getString("breed"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getString("description"),
                        rs.getString("image_url"),
                        rs.getString("status"));

                petsGridPanel.add(createPetCard(pet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        petsGridPanel.revalidate();
        petsGridPanel.repaint();
    }

    private JPanel createPetCard(Pet pet) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(0, 0, 15, 0)));
        card.setPreferredSize(new Dimension(350, 500));

        JLabel imageLabel = new JLabel("PET", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 60));
        imageLabel.setPreferredSize(new Dimension(350, 250));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(230, 235, 245));
        card.add(imageLabel, BorderLayout.NORTH);

        card.add(createInfoPanel(pet), BorderLayout.CENTER);

        return card;
    }

    private JPanel createInfoPanel(Pet pet) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JLabel nameLabel = new JLabel(pet.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nameLabel.setForeground(TEXT_COLOR);
        infoPanel.add(nameLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel detailsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        detailsGrid.setBackground(Color.WHITE);
        detailsGrid.add(createDetailLabel(pet.getSpecies()));
        detailsGrid.add(createDetailLabel(pet.getAge() + " years"));
        detailsGrid.add(createDetailLabel(pet.getGender()));
        detailsGrid.add(createDetailLabel(pet.getSize()));
        infoPanel.add(detailsGrid);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextArea descArea = new JTextArea(pet.getDescription());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(Color.WHITE);
        descArea.setRows(3);
        infoPanel.add(descArea);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton adoptBtn = new JButton("Adopt " + pet.getName());
        adoptBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        adoptBtn.setBackground(PRIMARY_COLOR);
        adoptBtn.setForeground(Color.WHITE);
        adoptBtn.setFocusPainted(false);
        adoptBtn.setBorderPainted(false);
        adoptBtn.setOpaque(true);
        adoptBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        adoptBtn.addActionListener(e -> adoptPet(pet));
        infoPanel.add(adoptBtn);

        return infoPanel;
    }

    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setBackground(new Color(248, 249, 250));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return label;
    }

    private void adoptPet(Pet pet) {
        if (mainFrame.getCurrentUser() == null) {
            JOptionPane.showMessageDialog(this, "Please login to adopt a pet", "Login Required",
                    JOptionPane.WARNING_MESSAGE);
            mainFrame.showPanel("login");
            return;
        }

        String notes = JOptionPane.showInputDialog(this,
                "Why do you want to adopt " + pet.getName() + "?",
                "Adoption Request",
                JOptionPane.QUESTION_MESSAGE);

        if (notes == null)
            return;

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO adoptions (user_id, pet_id, notes) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, mainFrame.getCurrentUser().getId());
            pstmt.setInt(2, pet.getId());
            pstmt.setString(3, notes);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                String updateSql = "UPDATE pets SET status = 'ADOPTED' WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, pet.getId());
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "Adoption successful!\n\nPet: " + pet.getName() + "\nStatus: PENDING",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                refreshPets();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to submit adoption: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

class MyAdoptionsPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color TEXT_COLOR = new Color(30, 60, 114);

    private final MainFrame mainFrame;
    private final JPanel adoptionsListPanel;

    public MyAdoptionsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("My Adoptions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        adoptionsListPanel = new JPanel();
        adoptionsListPanel.setLayout(new BoxLayout(adoptionsListPanel, BoxLayout.Y_AXIS));
        adoptionsListPanel.setBackground(BACKGROUND_COLOR);
        adoptionsListPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JScrollPane scrollPane = new JScrollPane(adoptionsListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshAdoptions() {
        adoptionsListPanel.removeAll();

        if (mainFrame.getCurrentUser() == null) {
            showMessage("Please login to view your adoptions");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = """
                        SELECT a.*, p.*, a.id as adoption_id, a.status as adoption_status
                        FROM adoptions a
                        JOIN pets p ON a.pet_id = p.id
                        WHERE a.user_id = ?
                        ORDER BY a.adoption_date DESC
                    """;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, mainFrame.getCurrentUser().getId());
            ResultSet rs = pstmt.executeQuery();

            boolean hasAdoptions = false;
            while (rs.next()) {
                hasAdoptions = true;
                adoptionsListPanel.add(createAdoptionCard(rs));
                adoptionsListPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }

            if (!hasAdoptions) {
                showMessage("You haven't adopted any pets yet.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        adoptionsListPanel.revalidate();
        adoptionsListPanel.repaint();
    }

    private void showMessage(String message) {
        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(100, 100, 100));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        adoptionsListPanel.add(label);
        adoptionsListPanel.revalidate();
        adoptionsListPanel.repaint();
    }

    private JPanel createAdoptionCard(ResultSet rs) throws SQLException {
        JPanel adoptionCard = new JPanel(new BorderLayout(15, 0));
        adoptionCard.setBackground(Color.WHITE);
        adoptionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        adoptionCard.setMaximumSize(new Dimension(1000, 150));

        JLabel imageLabel = new JLabel("PET");
        imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        imageLabel.setPreferredSize(new Dimension(120, 120));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(230, 235, 245));
        adoptionCard.add(imageLabel, BorderLayout.WEST);

        adoptionCard.add(createAdoptionInfo(rs), BorderLayout.CENTER);

        return adoptionCard;
    }

    private JPanel createAdoptionInfo(ResultSet rs) throws SQLException {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(rs.getString("name"));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(TEXT_COLOR);
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel speciesLabel = new JLabel("Species: " + rs.getString("species") + " | Breed: " + rs.getString("breed"));
        speciesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(speciesLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel dateLabel = new JLabel("Adoption Date: " + rs.getString("adoption_date"));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(dateLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        String notes = rs.getString("notes");
        JLabel notesLabel = new JLabel("Notes: " + (notes != null && !notes.isEmpty() ? notes : "No notes"));
        notesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(notesLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel statusBadge = new JLabel(rs.getString("adoption_status"));
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(243, 156, 18));
        statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        infoPanel.add(statusBadge);

        return infoPanel;
    }
}