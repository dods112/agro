import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

class AdminPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color PRIMARY_COLOR = new Color(231, 76, 60);
    private static final Color TEXT_COLOR = new Color(30, 60, 114);
    private static final Color HEADER_COLOR = new Color(52, 73, 94);

    private final MainFrame mainFrame;
    private final DashboardTab dashboardTab;
    private final AddPetTab addPetTab;
    private final ManagePetsTab managePetsTab;
    private final AdoptionsTab adoptionsTab;

    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        add(createHeader(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        dashboardTab = new DashboardTab();
        addPetTab = new AddPetTab();
        managePetsTab = new ManagePetsTab();
        adoptionsTab = new AdoptionsTab();

        tabbedPane.addTab("Dashboard", dashboardTab);
        tabbedPane.addTab("Add Pet", addPetTab);
        tabbedPane.addTab("Manage Pets", managePetsTab);
        tabbedPane.addTab("Adoptions", adoptionsTab);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR));

        JLabel titleLabel = new JLabel("Admin Panel");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    public void refreshData() {
        dashboardTab.refreshStats();
        managePetsTab.refreshTable();
        adoptionsTab.refreshTable();
    }

    class DashboardTab extends JPanel {
        private JLabel totalPetsLabel, availablePetsLabel, adoptedPetsLabel, totalAdoptionsLabel;

        public DashboardTab() {
            setLayout(new BorderLayout());
            setBackground(BACKGROUND_COLOR);

            JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
            statsPanel.setBackground(BACKGROUND_COLOR);
            statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            totalPetsLabel = createStatCard("Total Pets", "0", new Color(102, 126, 234));
            availablePetsLabel = createStatCard("Available", "0", new Color(240, 147, 251));
            adoptedPetsLabel = createStatCard("Adopted", "0", new Color(79, 172, 254));
            totalAdoptionsLabel = createStatCard("Total Adoptions", "0", new Color(67, 233, 123));

            statsPanel.add(totalPetsLabel);
            statsPanel.add(availablePetsLabel);
            statsPanel.add(adoptedPetsLabel);
            statsPanel.add(totalAdoptionsLabel);

            add(statsPanel, BorderLayout.CENTER);
            refreshStats();
        }

        private JLabel createStatCard(String title, String value, Color bgColor) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(bgColor);
            card.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
            valueLabel.setForeground(Color.WHITE);
            valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(new Color(255, 255, 255, 200));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(valueLabel);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(titleLabel);

            JLabel wrapper = new JLabel();
            wrapper.setLayout(new BorderLayout());
            wrapper.add(card);

            return wrapper;
        }

        public void refreshStats() {
            try (Connection conn = DatabaseManager.getConnection();
                    Statement stmt = conn.createStatement()) {

                updateStatCard(totalPetsLabel, stmt, "SELECT COUNT(*) FROM pets");
                updateStatCard(availablePetsLabel, stmt, "SELECT COUNT(*) FROM pets WHERE status = 'AVAILABLE'");
                updateStatCard(adoptedPetsLabel, stmt, "SELECT COUNT(*) FROM pets WHERE status = 'ADOPTED'");
                updateStatCard(totalAdoptionsLabel, stmt, "SELECT COUNT(*) FROM adoptions");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void updateStatCard(JLabel wrapper, Statement stmt, String query) throws SQLException {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                JLabel valueLabel = (JLabel) ((JPanel) wrapper.getComponent(0)).getComponent(0);
                valueLabel.setText(String.valueOf(rs.getInt(1)));
            }
        }
    }

    class AddPetTab extends JPanel {
        private final JTextField nameField, breedField, ageField, colorField, imageUrlField;
        private final JComboBox<String> speciesCombo, genderCombo, sizeCombo;
        private JTextArea descriptionArea;

        public AddPetTab() {
            setLayout(new BorderLayout());
            setBackground(BACKGROUND_COLOR);

            JPanel formPanel = createFormPanel();

            JScrollPane mainScrollPane = new JScrollPane(formPanel);
            mainScrollPane.setBorder(null);
            mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
            add(mainScrollPane, BorderLayout.CENTER);

            nameField = (JTextField) getFieldFromPanel(formPanel, 0);
            speciesCombo = (JComboBox<String>) getFieldFromPanel(formPanel, 1);
            breedField = (JTextField) getFieldFromPanel(formPanel, 2);
            ageField = (JTextField) getFieldFromPanel(formPanel, 3);
            genderCombo = (JComboBox<String>) getFieldFromPanel(formPanel, 4);
            sizeCombo = (JComboBox<String>) getFieldFromPanel(formPanel, 5);
            colorField = (JTextField) getFieldFromPanel(formPanel, 6);

            // Special handling for image upload panel
            JPanel grid = (JPanel) formPanel.getComponent(2);
            JPanel imageUploadPanel = (JPanel) grid.getComponent(7);
            JPanel filePanel = (JPanel) imageUploadPanel.getComponent(2);
            imageUrlField = (JTextField) filePanel.getComponent(0);
        }

        private JPanel createFormPanel() {
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(Color.WHITE);
            formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

            JLabel titleLabel = new JLabel("Add New Pet");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            formPanel.add(titleLabel);
            formPanel.add(Box.createRigidArea(new Dimension(0, 25)));

            JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
            grid.setBackground(Color.WHITE);

            grid.add(createFieldPanel("Pet Name *", new JTextField()));
            grid.add(createFieldPanel("Species *",
                    new JComboBox<>(new String[] { "Dog", "Cat", "Bird", "Rabbit", "Other" })));
            grid.add(createFieldPanel("Breed", new JTextField()));
            grid.add(createFieldPanel("Age *", new JTextField()));
            grid.add(createFieldPanel("Gender *", new JComboBox<>(new String[] { "Male", "Female" })));
            grid.add(createFieldPanel("Size *", new JComboBox<>(new String[] { "Small", "Medium", "Large" })));
            grid.add(createFieldPanel("Color", new JTextField()));
            grid.add(createImageUploadPanel());

            grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
            formPanel.add(grid);
            formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

            JLabel descLabel = new JLabel("Description *");
            descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            formPanel.add(descLabel);

            descriptionArea = new JTextArea(5, 40);
            descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            JScrollPane scrollPane = new JScrollPane(descriptionArea);
            scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            formPanel.add(scrollPane);
            formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            JButton addButton = createSubmitButton();
            formPanel.add(addButton);

            return formPanel;
        }

        private JComponent getFieldFromPanel(JPanel parent, int index) {
            JPanel grid = (JPanel) parent.getComponent(2);
            JPanel fieldPanel = (JPanel) grid.getComponent(index);
            return (JComponent) fieldPanel.getComponent(2);
        }

        private JPanel createFieldPanel(String label, JComponent field) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.WHITE);

            JLabel jLabel = new JLabel(label);
            jLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(jLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));

            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(field);

            return panel;
        }

        private JPanel createImageUploadPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.WHITE);

            JLabel jLabel = new JLabel("Pet Image");
            jLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(jLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));

            JPanel filePanel = new JPanel(new BorderLayout(5, 0));
            filePanel.setBackground(Color.WHITE);
            filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

            JTextField pathField = new JTextField();
            pathField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pathField.setEditable(false);
            filePanel.add(pathField, BorderLayout.CENTER);

            JButton browseBtn = new JButton("Browse");
            browseBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            browseBtn.setBackground(new Color(52, 152, 219));
            browseBtn.setForeground(Color.WHITE);
            browseBtn.setFocusPainted(false);
            browseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            browseBtn.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Image Files", "jpg", "jpeg", "png", "gif"));

                if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                    pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            });

            filePanel.add(browseBtn, BorderLayout.EAST);
            panel.add(filePanel);

            return panel;
        }

        private String copyImageToLocal(String sourcePath) {
            if (sourcePath == null || sourcePath.isEmpty()) {
                return null;
            }

            try {
                // Create images directory if it doesn't exist
                java.io.File imagesDir = new java.io.File("images");
                if (!imagesDir.exists()) {
                    imagesDir.mkdir();
                }

                // Generate unique filename
                java.io.File sourceFile = new java.io.File(sourcePath);
                String extension = sourcePath.substring(sourcePath.lastIndexOf("."));
                String newFileName = System.currentTimeMillis() + extension;
                java.io.File destFile = new java.io.File("images/" + newFileName);

                // Copy file
                Files.copy(sourceFile.toPath(), destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                return "images/" + newFileName;

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to copy image: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        private JButton createSubmitButton() {
            JButton button = new JButton("Add Pet to System");
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setBackground(new Color(39, 174, 96));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(e -> addPet());
            return button;
        }

        private void addPet() {
            String name = nameField.getText().trim();
            String species = (String) speciesCombo.getSelectedItem();
            String breed = breedField.getText().trim();
            String ageStr = ageField.getText().trim();
            String gender = (String) genderCombo.getSelectedItem();
            String size = (String) sizeCombo.getSelectedItem();
            String color = colorField.getText().trim();
            String description = descriptionArea.getText().trim();
            String imageSourcePath = imageUrlField.getText().trim();

            if (name.isEmpty() || ageStr.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields (*)", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Age must be a valid positive number", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Copy image to local images folder
            String imageUrl = copyImageToLocal(imageSourcePath);

            try (Connection conn = DatabaseManager.getConnection()) {
                String sql = "INSERT INTO pets (name, species, breed, age, gender, size, color, description, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setString(2, species);
                pstmt.setString(3, breed.isEmpty() ? "Mixed" : breed);
                pstmt.setInt(4, age);
                pstmt.setString(5, gender);
                pstmt.setString(6, size);
                pstmt.setString(7, color.isEmpty() ? "N/A" : color);
                pstmt.setString(8, description);
                pstmt.setString(9, imageUrl);

                int affected = pstmt.executeUpdate();

                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Pet added successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    dashboardTab.refreshStats();
                    managePetsTab.refreshTable();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to add pet: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void clearForm() {
            nameField.setText("");
            breedField.setText("");
            ageField.setText("");
            colorField.setText("");
            descriptionArea.setText("");
            imageUrlField.setText("");
        }
    }

    class ManagePetsTab extends JPanel {
        private final DefaultTableModel tableModel;
        private final JTable table;

        public ManagePetsTab() {
            setLayout(new BorderLayout());
            setBackground(BACKGROUND_COLOR);

            JLabel titleLabel = new JLabel("Manage All Pets");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            add(titleLabel, BorderLayout.NORTH);

            String[] columns = { "ID", "Name", "Species", "Breed", "Age", "Gender", "Size", "Status" };
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            table = new JTable(tableModel);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.setRowHeight(30);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            table.getTableHeader().setBackground(HEADER_COLOR);
            table.getTableHeader().setForeground(Color.WHITE);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
            add(scrollPane, BorderLayout.CENTER);

            add(createButtonPanel(), BorderLayout.SOUTH);
            refreshTable();
        }

        private JPanel createButtonPanel() {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

            JButton deleteButton = new JButton("Delete Selected");
            deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
            deleteButton.setBackground(PRIMARY_COLOR);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorderPainted(false);
            deleteButton.setOpaque(true);
            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteButton.addActionListener(e -> deletePet());
            buttonPanel.add(deleteButton);

            return buttonPanel;
        }

        public void refreshTable() {
            tableModel.setRowCount(0);

            try (Connection conn = DatabaseManager.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM pets ORDER BY id DESC")) {

                while (rs.next()) {
                    tableModel.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("species"),
                            rs.getString("breed"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("size"),
                            rs.getString("status")
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void deletePet() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a pet to delete", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int petId = (int) tableModel.getValueAt(selectedRow, 0);
            String petName = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete " + petName + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseManager.getConnection()) {
                    String sql = "DELETE FROM pets WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, petId);

                    int affected = pstmt.executeUpdate();

                    if (affected > 0) {
                        JOptionPane.showMessageDialog(this, "Pet deleted successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        refreshTable();
                        dashboardTab.refreshStats();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to delete pet: " + e.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class AdoptionsTab extends JPanel {
        private final DefaultTableModel tableModel;
        private final JTable table;

        public AdoptionsTab() {
            setLayout(new BorderLayout());
            setBackground(BACKGROUND_COLOR);

            JLabel titleLabel = new JLabel("All Adoptions");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            add(titleLabel, BorderLayout.NORTH);

            String[] columns = { "ID", "Pet Name", "Adopter Name", "Email", "Date", "Status", "Notes" };
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            table = new JTable(tableModel);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.setRowHeight(30);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            table.getTableHeader().setBackground(HEADER_COLOR);
            table.getTableHeader().setForeground(Color.WHITE);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
            add(scrollPane, BorderLayout.CENTER);

            refreshTable();
        }

        public void refreshTable() {
            tableModel.setRowCount(0);

            try (Connection conn = DatabaseManager.getConnection();
                    Statement stmt = conn.createStatement()) {

                String sql = """
                            SELECT a.id, p.name as pet_name, u.full_name, u.email, a.adoption_date, a.status, a.notes
                            FROM adoptions a
                            JOIN pets p ON a.pet_id = p.id
                            JOIN users u ON a.user_id = u.id
                            ORDER BY a.adoption_date DESC
                        """;

                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    tableModel.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getString("pet_name"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("adoption_date"),
                            rs.getString("status"),
                            rs.getString("notes")
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}