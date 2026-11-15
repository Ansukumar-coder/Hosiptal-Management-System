import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Base64;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
public class MediCareGUI extends JFrame {
    private static final Color NAVY = new Color(18, 35, 62);
    private static final Color BLUE = new Color(37, 99, 166);
    private static final Color BG = new Color(244, 247, 251);
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final FileHelper patients = new FileHelper();
    private final ModuleStore store = new ModuleStore();
    private final JLabel topTitle = new JLabel("Dashboard");
    private final JPanel dashboard = new JPanel(new BorderLayout(18, 18));
    private final JLabel totalPatients = new JLabel("0");
    private final JLabel totalDoctors = new JLabel("0");
    private final JLabel todaysAppts = new JLabel("0");
    private final JLabel bedsAvailable = new JLabel("0");
    private boolean darkMode;

    public MediCareGUI() {
        super("MediCare | Hospital Management System");
        try { patients.init(); } catch (IOException ex) { showError(ex); }
        buildShell();
        buildDashboard();
        addModule("Patients", new PatientPanel());
        addModule("Doctors", new DataPanel("Doctors", new String[]{"Doctor ID", "Name", "Specialization", "Contact", "Availability"}, new String[]{"Name", "Specialization", "Contact", "Availability"}));
        addModule("Appointments", new DataPanel("Appointments", new String[]{"Appointment ID", "Patient ID", "Doctor", "Date", "Time", "Status"}, new String[]{"Patient ID", "Doctor", "Date (yyyy-MM-dd)", "Time", "Status"}));
        addModule("Billing", new DataPanel("Billing", new String[]{"Bill ID", "Patient ID", "Consultation", "Medicines", "Total", "Payment Status"}, new String[]{"Patient ID", "Consultation charge", "Medicine charge", "Payment status (Paid/Unpaid)"}, true));
        addModule("Bed Management", new DataPanel("Beds", new String[]{"Bed ID", "Ward", "Bed Number", "Status", "Patient ID"}, new String[]{"Ward", "Bed number", "Status (Available/Occupied)", "Patient ID"}));
        addModule("Reports", reportsPanel());
        addModule("Settings", settingsPanel());
        showPage("Dashboard");
        new javax.swing.Timer(30000, e -> refreshDashboard()).start();
        setSize(1420, 880);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildShell() {
        setLayout(new BorderLayout());
        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("  MediCare");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 25));
        brand.setBorder(new EmptyBorder(22, 8, 25, 0));
        sidebar.add(brand);
        String[][] nav = {{"▦", "Dashboard"}, {"♙", "Patients"}, {"✚", "Doctors"}, {"◷", "Appointments"},
                {"$", "Billing"}, {"▤", "Bed Management"}, {"▧", "Reports"}, {"⚙", "Settings"}};
        for (String[] item : nav) {
            JButton button = new JButton(item[0] + "    " + item[1]);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setForeground(new Color(221, 231, 245));
            button.setBackground(NAVY);
            button.setFont(new Font("SansSerif", Font.PLAIN, 15));
            button.setBorder(new EmptyBorder(12, 20, 12, 10));
            button.setFocusPainted(false);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            button.addActionListener(e -> showPage(item[1]));
            sidebar.add(button);
        }
        sidebar.add(Box.createVerticalGlue());
        JButton logout = new JButton("↪    Exit MediCare");
        logout.setForeground(new Color(255, 205, 210));
        logout.setBackground(NAVY);
        logout.setBorder(new EmptyBorder(14, 20, 18, 10));
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.addActionListener(e -> { if (JOptionPane.showConfirmDialog(this, "Close MediCare?", "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) dispose(); });
        sidebar.add(logout);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG);
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(17, 26, 17, 24));
        topTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel today = new JLabel(new SimpleDateFormat("EEEE, dd MMM yyyy").format(new Date()));
        today.setForeground(new Color(99, 115, 129));
        top.add(topTitle, BorderLayout.WEST);
        top.add(today, BorderLayout.EAST);
        right.add(top, BorderLayout.NORTH);
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(22, 24, 24, 24));
        right.add(content, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    private void addModule(String name, JPanel panel) { panel.setBackground(BG); content.add(panel, name); }

    private void showPage(String name) {
        topTitle.setText(name);
        cards.show(content, name);
        if (name.equals("Dashboard")) refreshDashboard();
    }

    private void buildDashboard() {
        dashboard.setBackground(BG);
        dashboard.removeAll();
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("TOTAL PATIENTS", totalPatients, new Color(38, 107, 183)));
        stats.add(statCard("TOTAL DOCTORS", totalDoctors, new Color(22, 145, 129)));
        stats.add(statCard("TODAY'S APPOINTMENTS", todaysAppts, new Color(225, 147, 47)));
        stats.add(statCard("AVAILABLE BEDS", bedsAvailable, new Color(125, 92, 182)));
        JPanel mid = new JPanel(new GridLayout(1, 2, 16, 0));
        mid.setOpaque(false);
        mid.add(listCard("Recent Patient Registrations", recentPatients()));
        mid.add(listCard("Recent Activities", recentActivity()));
        dashboard.add(stats, BorderLayout.NORTH);
        dashboard.add(mid, BorderLayout.CENTER);
        dashboard.setBorder(new EmptyBorder(0, 0, 0, 0));
        if (dashboard.getParent() == null) content.add(dashboard, "Dashboard");
    }

    private JPanel statCard(String caption, JLabel value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, accent), new EmptyBorder(18, 18, 18, 18)));
        JLabel title = new JLabel(caption);
        title.setForeground(new Color(102, 117, 130));
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        value.setFont(new Font("SansSerif", Font.BOLD, 32));
        value.setForeground(new Color(29, 48, 70));
        card.add(title, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel listCard(String title, JComponent body) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        card.add(label, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent recentPatients() {
        DefaultListModel<String> list = new DefaultListModel<>();
        try {
            List<Patient> all = patients.getAllPatients();
            for (int i = all.size() - 1; i >= Math.max(0, all.size() - 7); i--) {
                Patient p = all.get(i); list.addElement("#" + p.getId() + "   " + p.getName() + "   ·   " + p.getRegisteredOn());
            }
        } catch (IOException ex) { list.addElement("Could not load patient records."); }
        return new JScrollPane(new JList<>(list));
    }

    private JComponent recentActivity() {
        DefaultListModel<String> list = new DefaultListModel<>();
        try {
            List<String[]> entries = store.read("Activity", 2);
            for (int i = entries.size() - 1; i >= Math.max(0, entries.size() - 8); i--) list.addElement(entries.get(i)[0] + "   ·   " + entries.get(i)[1]);
        } catch (IOException ignored) { }
        if (list.isEmpty()) list.addElement("No activity recorded yet.");
        return new JScrollPane(new JList<>(list));
    }

    private void refreshDashboard() {
        try { totalPatients.setText(String.valueOf(patients.getAllPatients().size())); }
        catch (IOException ex) { totalPatients.setText("—"); }
        try {
            List<String[]> doctors = store.read("Doctors", 5);
            totalDoctors.setText(String.valueOf(doctors.size()));
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            int appointments = 0;
            for (String[] row : store.read("Appointments", 6)) if (today.equals(row[3]) && !"Cancelled".equalsIgnoreCase(row[5])) appointments++;
            todaysAppts.setText(String.valueOf(appointments));
            int available = 0;
            for (String[] row : store.read("Beds", 5)) if ("Available".equalsIgnoreCase(row[3])) available++;
            bedsAvailable.setText(String.valueOf(available));
        } catch (IOException ex) { totalDoctors.setText("—"); }
        buildDashboard(); dashboard.revalidate(); dashboard.repaint();
    }

    private JPanel reportsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 16));
        JButton csv = actionButton("Export patient register to CSV");
        csv.addActionListener(e -> exportPatients());
        panel.add(new JLabel("Export a portable copy of the patient register.")); panel.add(csv);
        return panel;
    }

    private JPanel settingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 16));
        JButton theme = actionButton("Toggle light / dark appearance");
        theme.addActionListener(e -> { darkMode = !darkMode; applyTheme(getContentPane(), darkMode); });
        panel.add(new JLabel("Appearance")); panel.add(theme);
        return panel;
    }

    private void applyTheme(Component component, boolean dark) {
        Color background = dark ? new Color(31, 41, 55) : BG;
        Color foreground = dark ? new Color(238, 242, 247) : new Color(29, 48, 70);
        if (component instanceof JPanel) component.setBackground(background);
        if (component instanceof JLabel) ((JLabel) component).setForeground(foreground);
        if (component instanceof Container) for (Component child : ((Container) component).getComponents()) applyTheme(child, dark);
        component.repaint();
    }

    private void exportPatients() {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new File("MediCare_Patients.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8))) {
            out.println("ID,Name,Age,Gender,Contact,Address,Disease,Doctor,Status,Registered On");
            for (Patient p : patients.getAllPatients()) out.println(csv(p.getId()) + "," + csv(p.getName()) + "," + p.getAge() + "," + csv(p.getGender()) + "," + csv(p.getContactNumber()) + "," + csv(p.getAddress()) + "," + csv(p.getDisease()) + "," + csv(p.getDoctor()) + "," + csv(p.getStatus()) + "," + csv(p.getRegisteredOn()));
            JOptionPane.showMessageDialog(this, "CSV report saved.");
        } catch (IOException ex) { showError(ex); }
    }

    private String csv(Object value) { return "\"" + String.valueOf(value).replace("\"", "\"\"") + "\""; }
    private JButton actionButton(String text) { JButton b = new JButton(text); b.setBackground(BLUE); b.setForeground(Color.WHITE); b.setFocusPainted(false); return b; }
    private void showError(Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "MediCare", JOptionPane.ERROR_MESSAGE); }

    private void offerPatientDocument(Patient patient, boolean discharge) {
        String heading = discharge ? "DISCHARGE CERTIFICATE" : "PATIENT REGISTRATION ACKNOWLEDGEMENT";
        if (JOptionPane.showConfirmDialog(this, "Save " + heading.toLowerCase(Locale.ROOT) + " for " + patient.getName() + "?", "Patient document", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File((discharge ? "Discharge_Certificate_" : "Patient_Acknowledgement_") + patient.getId() + ".html"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File output = chooser.getSelectedFile();
        if (!output.getName().toLowerCase(Locale.ROOT).endsWith(".html")) output = new File(output.getParentFile(), output.getName() + ".html");
        try {
            StringBuilder html = new StringBuilder("<!doctype html><html><head><meta charset='utf-8'><title>").append(heading)
                    .append("</title><style>@page{size:A4;margin:18mm}body{font:14px Arial;color:#203447;background:#eef2f5}.sheet{max-width:760px;margin:24px auto;padding:44px;background:white;border-top:12px solid #1767a9}.brand{font-weight:bold;color:#1767a9;letter-spacing:2px}.title{text-align:center;margin:36px 0;color:#173d5b}.row{padding:12px;border-bottom:1px solid #dce4ea}.label{display:inline-block;width:180px;color:#687988}.sign{display:flex;justify-content:space-between;margin-top:80px}.sign span{border-top:1px solid #789;padding-top:8px;width:220px;text-align:center}.foot{text-align:center;margin-top:35px;color:#778}@media print{body{background:white}.sheet{margin:0 auto}}</style></head><body><main class='sheet'><div class='brand'>MEDICARE HOSPITAL MANAGEMENT SYSTEM</div><p>Patient care and medical records</p><hr><h1 class='title'>")
                    .append(heading).append("</h1>");
            if (!patient.getPhotoPath().equals("-") && new File(patient.getPhotoPath()).isFile()) {
                File photo = new File(patient.getPhotoPath());
                String mime = photo.getName().toLowerCase(Locale.ROOT).endsWith(".png") ? "image/png" : "image/jpeg";
                html.append("<img style='float:right;width:110px;height:130px;object-fit:cover' src='data:").append(mime).append(";base64,")
                        .append(Base64.getEncoder().encodeToString(Files.readAllBytes(photo.toPath()))).append("'>");
            }
            html.append(docRow("Patient ID", String.valueOf(patient.getId())))
                    .append(docRow("Patient name", patient.getName())).append(docRow("Age / gender", patient.getAge() + " / " + patient.getGender()))
                    .append(docRow("Contact", patient.getContactNumber())).append(docRow("Address", patient.getAddress()))
                    .append(docRow("Medical information", patient.getDisease())).append(docRow("Attending doctor", patient.getDoctor()))
                    .append(docRow("Ward / room", patient.getWard() + " / " + patient.getRoomNo()))
                    .append(docRow(discharge ? "Discharged on" : "Registered on", discharge ? patient.getDischargedOn() : patient.getRegisteredOn()))
                    .append(discharge ? "<p>This certificate confirms discharge from MediCare.</p>" : "<p>This document confirms registration in the MediCare patient register.</p>")
                    .append("<div class='sign'><span>Authorized medical officer</span><span>Hospital seal</span></div><p class='foot'>Open in a browser and choose Print → Save as PDF.</p></main></body></html>");
            Files.write(output.toPath(), html.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) { showError(ex); }
    }

    private String docRow(String label, String value) { return "<div class='row'><span class='label'>" + html(label) + "</span><b>" + html(value) + "</b></div>"; }
    private String html(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }

    private final class PatientPanel extends JPanel {
        private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Gender", "Contact", "Address", "Diagnosis", "Doctor", "Status", "Registered"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        private final JTable table = new JTable(model);
        private final javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        private final JTextField search = new JTextField(22);
        PatientPanel() {
            super(new BorderLayout(12, 12)); setBackground(BG);
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); toolbar.setOpaque(false);
            JButton add = actionButton("Add Patient"), edit = actionButton("Edit"), delete = actionButton("Delete"), discharge = actionButton("Discharge"), refresh = actionButton("Refresh");
            toolbar.add(new JLabel("Search name / ID:")); toolbar.add(search); toolbar.add(add); toolbar.add(edit); toolbar.add(delete); toolbar.add(discharge); toolbar.add(refresh);
            table.setRowHeight(27); table.setFillsViewportHeight(true); table.setRowSorter(sorter);
            add(toolbar, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER);
            add.addActionListener(e -> addPatientDialog()); edit.addActionListener(e -> editPatientDialog()); delete.addActionListener(e -> deletePatient()); discharge.addActionListener(e -> dischargePatient()); refresh.addActionListener(e -> reload());
            search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() { public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); } public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); } public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); } });
            reload();
        }
        private void reload() {
            model.setRowCount(0);
            try { for (Patient p : patients.getAllPatients()) model.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(), p.getContactNumber(), p.getAddress(), p.getDisease(), p.getDoctor(), p.getStatus(), p.getRegisteredOn()}); }
            catch (IOException ex) { showError(ex); }
        }
        private void filter() {
            String q = search.getText().trim().toLowerCase(Locale.ROOT);
            sorter.setRowFilter(q.isEmpty() ? null : javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q), 0, 1));
        }
        private void addPatientDialog() {
            JTextField name = new JTextField(), age = new JTextField(), contact = new JTextField(), address = new JTextField(), disease = new JTextField(), doctor = new JTextField(), photo = new JTextField();
            photo.setEditable(false);
            JButton browse = new JButton("Browse photo");
            browse.addActionListener(e -> { JFileChooser chooser = new JFileChooser(); chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif")); if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) photo.setText(chooser.getSelectedFile().getAbsolutePath()); });
            JPanel photoInput = new JPanel(new BorderLayout(6, 0)); photoInput.add(photo, BorderLayout.CENTER); photoInput.add(browse, BorderLayout.EAST);
            JComboBox<String> gender = new JComboBox<>(new String[]{"Female", "Male", "Other", "Not specified"});
            Object[] fields = {"Name", name, "Age", age, "Gender", gender, "Contact number", contact, "Address", address, "Medical information / diagnosis", disease, "Doctor", doctor, "Patient photo", photoInput};
            if (JOptionPane.showConfirmDialog(this, fields, "Register patient", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            int years; try { years = Integer.parseInt(age.getText().trim()); if (years < 0) throw new NumberFormatException(); } catch (NumberFormatException ex) { showError(new Exception("Enter a valid age.")); return; }
            if (name.getText().trim().isEmpty()) { showError(new Exception("Patient name is required.")); return; }
            try {
                int id = patients.addPatient(name.getText().trim(), years, disease.getText().trim(), doctor.getText().trim().isEmpty() ? "Not assigned" : doctor.getText().trim(),
                        new SimpleDateFormat("EEEE").format(new Date()), "10:00", "None", "Under Treatment", "None", "-", "-", "-", contact.getText().trim(),
                        photo.getText().trim().isEmpty() ? "-" : photo.getText().trim(), (String) gender.getSelectedItem(), contact.getText().trim(), address.getText().trim());
                Patient saved = null; for (Patient p : patients.getAllPatients()) if (p.getId() == id) saved = p;
                if (saved != null) { offerPatientDocument(saved, false); store.log("Registered patient #" + id + " — " + saved.getName()); }
                reload(); refreshDashboard();
            } catch (IOException ex) { showError(ex); }
        }
        private void editPatientDialog() {
            int id = selectedId(); if (id < 0) { showError(new Exception("Select a patient first.")); return; }
            try {
                Patient p = null; for (Patient item : patients.getAllPatients()) if (item.getId() == id) p = item;
                if (p == null) return;
                JTextField name = new JTextField(p.getName()), age = new JTextField(String.valueOf(p.getAge())), contact = new JTextField(p.getContactNumber()), address = new JTextField(p.getAddress()), disease = new JTextField(p.getDisease()), doctor = new JTextField(p.getDoctor());
                JComboBox<String> gender = new JComboBox<>(new String[]{"Female", "Male", "Other", "Not specified"}); gender.setSelectedItem(p.getGender());
                Object[] fields = {"Name", name, "Age", age, "Gender", gender, "Contact number", contact, "Address", address, "Medical information / diagnosis", disease, "Doctor", doctor};
                if (JOptionPane.showConfirmDialog(this, fields, "Edit patient #" + id, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                int years; try { years = Integer.parseInt(age.getText().trim()); if (years < 0) throw new NumberFormatException(); } catch (NumberFormatException ex) { showError(new Exception("Enter a valid age.")); return; }
                patients.updatePatientProfile(id, name.getText().trim(), years, String.valueOf(gender.getSelectedItem()), contact.getText().trim(), address.getText().trim(), disease.getText().trim(), doctor.getText().trim()); store.log("Updated patient #" + id); reload(); refreshDashboard();
            } catch (IOException ex) { showError(ex); }
        }
        private int selectedId() { int row = table.getSelectedRow(); return row < 0 ? -1 : Integer.parseInt(String.valueOf(table.getValueAt(table.convertRowIndexToModel(row), 0))); }
        private void deletePatient() {
            int id = selectedId(); if (id < 0) { showError(new Exception("Select a patient first.")); return; }
            if (JOptionPane.showConfirmDialog(this, "Delete patient #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try { patients.deletePatient(id); store.log("Deleted patient #" + id); reload(); refreshDashboard(); } catch (IOException ex) { showError(ex); }
        }
        private void dischargePatient() {
            int id = selectedId(); if (id < 0) { showError(new Exception("Select a patient first.")); return; }
            if (JOptionPane.showConfirmDialog(this, "Discharge selected patient?", "Confirm discharge", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try {
                patients.dischargePatient(id);
                for (Patient p : patients.getAllPatients()) if (p.getId() == id) offerPatientDocument(p, true);
                List<String[]> bedRows = store.read("Beds", 5);
                boolean changed = false;
                for (String[] bed : bedRows) if (String.valueOf(id).equals(bed[4])) { bed[3] = "Available"; bed[4] = ""; changed = true; }
                if (changed) store.write("Beds", bedRows);
                store.log("Discharged patient #" + id); reload(); refreshDashboard();
            } catch (IOException ex) { showError(ex); }
        }
    }

    private final class DataPanel extends JPanel {
        private final String module; private final String[] columns, inputs; private final boolean billing;
        private final DefaultTableModel model; private final JTable table;
        DataPanel(String module, String[] columns, String[] inputs) { this(module, columns, inputs, false); }
        DataPanel(String module, String[] columns, String[] inputs, boolean billing) {
            super(new BorderLayout(12, 12)); this.module = module; this.columns = columns; this.inputs = inputs; this.billing = billing; setBackground(BG);
            model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } }; table = new JTable(model); table.setRowHeight(27); table.setAutoCreateRowSorter(true);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); actions.setOpaque(false);
            JButton add = actionButton("Add record"), edit = actionButton("Edit selected"), delete = actionButton("Delete selected"), refresh = actionButton("Refresh");
            actions.add(add); actions.add(edit); actions.add(delete); actions.add(refresh); add(actions, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER);
            if (billing) { JButton invoice = actionButton("Print invoice"); actions.add(invoice); invoice.addActionListener(e -> printInvoice()); }
            add.addActionListener(e -> editRecord(-1)); edit.addActionListener(e -> editRecord(selectedRow())); delete.addActionListener(e -> deleteRecord()); refresh.addActionListener(e -> reload()); reload();
        }
        private int selectedRow() { int row = table.getSelectedRow(); return row < 0 ? -1 : table.convertRowIndexToModel(row); }
        private void reload() { model.setRowCount(0); try { for (String[] row : store.read(module, columns.length)) model.addRow(row); } catch (IOException ex) { showError(ex); } }
        private void editRecord(int row) {
            String[] old = row < 0 ? new String[columns.length] : new String[columns.length];
            if (row >= 0) for (int i = 0; i < columns.length; i++) old[i] = String.valueOf(model.getValueAt(row, i)); else Arrays.fill(old, "");
            JTextField[] fields = new JTextField[inputs.length]; Object[] form = new Object[inputs.length * 2];
            for (int i = 0; i < inputs.length; i++) { form[i * 2] = inputs[i]; fields[i] = new JTextField(billing && i == 3 ? old[5] : old[i + 1]); form[i * 2 + 1] = fields[i]; }
            if (JOptionPane.showConfirmDialog(this, form, row < 0 ? "Add " + module : "Edit " + module, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            String[] fresh = new String[columns.length]; fresh[0] = row < 0 ? nextId() : old[0];
            for (int i = 0; i < inputs.length; i++) fresh[i + 1] = fields[i].getText().trim();
            if (billing) {
                fresh[5] = fresh[4];
                try { double consultation = Double.parseDouble(fresh[2]), medicine = Double.parseDouble(fresh[3]); fresh[4] = String.format(Locale.US, "%.2f", consultation + medicine); }
                catch (NumberFormatException ex) { showError(new Exception("Charges must be valid numbers.")); return; }
            }
            List<String[]> rows; try { rows = store.read(module, columns.length); if (row < 0) rows.add(fresh); else rows.set(row, fresh); store.write(module, rows); store.log((row < 0 ? "Added " : "Updated ") + module + " record #" + fresh[0]); reload(); refreshDashboard(); }
            catch (IOException ex) { showError(ex); }
        }
        private String nextId() { int max = 0; for (int i = 0; i < model.getRowCount(); i++) try { max = Math.max(max, Integer.parseInt(String.valueOf(model.getValueAt(i, 0)).replaceAll("\\D", ""))); } catch (NumberFormatException ignored) { } return String.valueOf(max + 1); }
        private void deleteRecord() {
            int row = selectedRow(); if (row < 0) { showError(new Exception("Select a record first.")); return; }
            if (JOptionPane.showConfirmDialog(this, "Delete selected record?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try { List<String[]> rows = store.read(module, columns.length); String id = rows.get(row)[0]; rows.remove(row); store.write(module, rows); store.log("Deleted " + module + " record #" + id); reload(); refreshDashboard(); } catch (IOException ex) { showError(ex); }
        }
        private void printInvoice() {
            int row = selectedRow(); if (row < 0) { showError(new Exception("Select a bill first.")); return; }
            StringBuilder invoice = new StringBuilder("MEDICARE HOSPITAL\nPATIENT INVOICE\n\n");
            for (int column = 0; column < columns.length; column++) invoice.append(columns[column]).append(": ").append(model.getValueAt(row, column)).append('\n');
            JTextArea printable = new JTextArea(invoice.toString()); printable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            try { printable.print(); } catch (java.awt.print.PrinterException ex) { showError(ex); }
        }
    }
}
