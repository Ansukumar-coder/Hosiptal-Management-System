import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
public class HospitalGUI extends JFrame {
    private static final int COL_ID = 0, COL_WARD = 5, COL_ROOM = 6, COL_DAY = 7,
            COL_TIME = 8, COL_NEXT = 9, COL_STATUS = 10, COL_DISCHARGED = 12,
            COL_GNAME = 13, COL_GREL = 14, COL_GPHONE = 15;

    private final FileHelper db = new FileHelper();
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private AutoRefreshThread autoRefresh;
    private boolean refreshing = false;   

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Age", "Disease", "Doctor", "Ward", "Room/Bed No",
                    "Visit Day", "Visit Time", "Next Appointment", "Status", "Registered On",
                    "Discharged On", "Guardian", "Relation", "Guardian Phone"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField nameField = new JTextField(10);
    private final JTextField ageField = new JTextField(3);
    private final JTextField diseaseField = new JTextField(10);
    private final JComboBox<String> doctorBox = new JComboBox<>(new String[]{
            "Dr. Sharma (General)", "Dr. Singh (Cardiology)",
            "Dr. Kaur (Pediatrics)", "Dr. Verma (Orthopedic)"});
    private final JComboBox<String> wardBox = new JComboBox<>(new String[]{
            "General Ward", "VIP Room", "ICU", "Private Room", "Emergency", "None"});
    private final JTextField roomField = new JTextField(5);
    private final JComboBox<String> dayBox = new JComboBox<>(new String[]{
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"});
    private final JTextField timeField = new JTextField("10:00", 5);
    private final JTextField nextField = new JTextField(8);
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{
            "Under Treatment", "Admitted", "Follow-up", "Discharged"});
    private final JTextField guardianField = new JTextField(10);
    private final JComboBox<String> relationBox = new JComboBox<>(new String[]{
            "Father", "Mother", "Spouse", "Son", "Daughter", "Brother", "Sister", "Friend", "Other"});
    private final JTextField phoneField = new JTextField(10);
    private final JLabel status = new JLabel("Ready");

    public HospitalGUI() {
        super("Hospital Management System");
        buildGui();
        initStorage();
        refreshPatients();
        autoRefresh = new AutoRefreshThread(this, 5);
        autoRefresh.start();
    }
    private void buildGui() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1250, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Hospital Management System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setOpaque(true);
        title.setBackground(new Color(0, 121, 107));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Name:"));    row1.add(nameField);
        row1.add(new JLabel("Age:"));     row1.add(ageField);
        row1.add(new JLabel("Disease:")); row1.add(diseaseField);
        row1.add(new JLabel("Doctor:"));  row1.add(doctorBox);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Ward:"));          row2.add(wardBox);
        row2.add(new JLabel("Room/Bed No:"));   row2.add(roomField);
        row2.add(new JLabel("Status:"));        row2.add(statusBox);
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row3.add(new JLabel("Visit Day:"));  row3.add(dayBox);
        row3.add(new JLabel("Visit Time (HH:mm):")); row3.add(timeField);
        row3.add(new JLabel("Next Appointment (yyyy-MM-dd):")); row3.add(nextField);
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row4.add(new JLabel("Guardian Name:")); row4.add(guardianField);
        row4.add(new JLabel("Relation:"));      row4.add(relationBox);
        row4.add(new JLabel("Guardian Phone:")); row4.add(phoneField);
        JButton addBtn = new JButton("Add Patient");
        JButton updBtn = new JButton("Update Selected");
        JButton disBtn = new JButton("Discharge Selected");
        JButton delBtn = new JButton("Delete Selected");
        JButton refBtn = new JButton("Refresh");
        JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row5.add(addBtn); row5.add(updBtn); row5.add(disBtn); row5.add(delBtn); row5.add(refBtn);

        addBtn.addActionListener(e -> addPatient());
        updBtn.addActionListener(e -> updateSelected());
        disBtn.addActionListener(e -> dischargeSelected());
        delBtn.addActionListener(e -> deletePatient());
        refBtn.addActionListener(e -> refreshPatients());

        JPanel form = new JPanel(new GridLayout(5, 1));
        form.add(row1);
        form.add(row2);
        form.add(row3);
        form.add(row4);
        form.add(row5);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(form, BorderLayout.CENTER);
        bottom.add(status, BorderLayout.SOUTH);
        table.setRowHeight(22);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {40, 120, 40, 110, 140, 100, 90, 90, 80, 120, 110, 100, 130, 120, 80, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                if (!selected) {
                    String st = String.valueOf(model.getValueAt(row, COL_STATUS));
                    if (st.equals("Discharged"))      c.setBackground(new Color(200, 230, 201)); 
                    else if (st.equals("Admitted"))   c.setBackground(new Color(255, 205, 210)); 
                    else if (st.equals("Follow-up"))  c.setBackground(new Color(255, 249, 196)); 
                    else                              c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || refreshing) return;
            int row = table.getSelectedRow();
            if (row < 0) return;
            wardBox.setSelectedItem(model.getValueAt(row, COL_WARD));
            roomField.setText(dashToEmpty(model.getValueAt(row, COL_ROOM)));
            dayBox.setSelectedItem(model.getValueAt(row, COL_DAY));
            timeField.setText(String.valueOf(model.getValueAt(row, COL_TIME)));
            String next = String.valueOf(model.getValueAt(row, COL_NEXT));
            nextField.setText(next.equals("None") ? "" : next);
            statusBox.setSelectedItem(model.getValueAt(row, COL_STATUS));
            guardianField.setText(dashToEmpty(model.getValueAt(row, COL_GNAME)));
            relationBox.setSelectedItem(model.getValueAt(row, COL_GREL));
            phoneField.setText(dashToEmpty(model.getValueAt(row, COL_GPHONE)));
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private String dashToEmpty(Object value) {
        String s = String.valueOf(value);
        return s.equals("-") ? "" : s;
    }

    private String emptyToDash(String value) {
        return value.isEmpty() ? "-" : value;
    }

    private void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> status.setText(msg));
    }
    private String checkDetails(String ward, String room, String statusText, String time, String next) {
        if (!statusText.equals("Discharged") && !ward.equals("None") && room.isEmpty()) {
            return "Enter the room/bed number for the " + ward + ".";
        }
        if (!time.matches("([01]?\\d|2[0-3]):[0-5]\\d")) {
            return "Visit time must look like 09:30 or 14:00.";
        }
        if (!next.isEmpty() && !next.equalsIgnoreCase("none")) {
            try {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                f.setLenient(false);
                f.parse(next);
            } catch (ParseException ex) {
                return "Next appointment must look like 2026-12-25 (or leave it empty).";
            }
        }
        return null;
    }
    private String checkGuardian(String gName, String gPhone, boolean required) {
        if (required && gName.isEmpty()) {
            return "Enter the guardian name.";
        }
        if (required && gPhone.isEmpty()) {
            return "Enter the guardian phone number.";
        }
        if (!gPhone.isEmpty() && !gPhone.matches("\\+?\\d{10,13}")) {
            return "Guardian phone must be 10 to 13 digits (example 9876543210).";
        }
        return null;
    }

    private String cleanNext(String next, String statusText) {
        if (statusText.equals("Discharged") || next.isEmpty() || next.equalsIgnoreCase("none")) {
            return "None";
        }
        return next;
    }

    private String cleanWard(String ward, String statusText) {
        return statusText.equals("Discharged") ? "None" : ward;
    }

    private String cleanRoom(String ward, String room, String statusText) {
        return (statusText.equals("Discharged") || ward.equals("None") || room.isEmpty()) ? "-" : room;
    }
    private void initStorage() {
        try {
            db.init();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "File error: " + e.getMessage());
        }
    }

    public void refreshPatients() {
        dbExecutor.submit(() -> {
            try {
                List<Patient> patients = db.getAllPatients();
                SwingUtilities.invokeLater(() -> {     
                    int selRow = table.getSelectedRow();
                    String selectedId = selRow >= 0 ? String.valueOf(model.getValueAt(selRow, COL_ID)) : null;

                    refreshing = true;
                    model.setRowCount(0);
                    int active = 0, icu = 0;
                    for (Patient p : patients) {
                        model.addRow(new Object[]{String.valueOf(p.getId()), p.getName(),
                                String.valueOf(p.getAge()), p.getDisease(), p.getDoctor(),
                                p.getWard(), p.getRoomNo(),
                                p.getVisitDay(), p.getVisitTime(), p.getNextAppointment(),
                                p.getStatus(), p.getRegisteredOn(), p.getDischargedOn(),
                                p.getGuardianName(), p.getGuardianRelation(), p.getGuardianPhone()});
                        if (!p.getStatus().equals("Discharged")) active++;
                        if (p.getWard().equals("ICU")) icu++;
                    }
                    if (selectedId != null) {
                        for (int i = 0; i < model.getRowCount(); i++) {
                            if (String.valueOf(model.getValueAt(i, COL_ID)).equals(selectedId)) {
                                table.setRowSelectionInterval(i, i);
                                break;
                            }
                        }
                    }
                    refreshing = false;

                    status.setText("Total: " + patients.size() + "   Active: " + active
                            + "   Discharged: " + (patients.size() - active)
                            + "   In ICU: " + icu
                            + "   (updated " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ")");
                });
            } catch (IOException e) {
                setStatus("Load error: " + e.getMessage());
            }
        });
    }

    private void addPatient() {
        final String name = nameField.getText().trim();
        final String disease = diseaseField.getText().trim();
        final String doctor = (String) doctorBox.getSelectedItem();
        final String rawWard = (String) wardBox.getSelectedItem();
        final String rawRoom = roomField.getText().trim();
        final String day = (String) dayBox.getSelectedItem();
        final String time = timeField.getText().trim();
        final String statusText = (String) statusBox.getSelectedItem();
        final String rawNext = nextField.getText().trim();
        final String gName = guardianField.getText().trim();
        final String gRelation = (String) relationBox.getSelectedItem();
        final String gPhone = phoneField.getText().trim();
        final int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid age.");
            return;
        }
        if (name.isEmpty() || disease.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and disease are required.");
            return;
        }
        String error = checkDetails(rawWard, rawRoom, statusText, time, rawNext);
        if (error == null) error = checkGuardian(gName, gPhone, true);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error);
            return;
        }
        final String next = cleanNext(rawNext, statusText);
        final String ward = cleanWard(rawWard, statusText);
        final String room = cleanRoom(ward, rawRoom, statusText);

        dbExecutor.submit(() -> {
            try {
                db.addPatient(name, age, disease, doctor, day, time, next, statusText, ward, room,
                        gName, gRelation, gPhone);
                setStatus("Patient added: " + name);
                SwingUtilities.invokeLater(() -> {
                    nameField.setText("");
                    ageField.setText("");
                    diseaseField.setText("");
                    roomField.setText("");
                    nextField.setText("");
                    guardianField.setText("");
                    phoneField.setText("");
                });
                refreshPatients();
            } catch (IOException e) {
                setStatus("Add error: " + e.getMessage());
            }
        });
    }

    private void updateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient first.");
            return;
        }
        final int id = Integer.parseInt(String.valueOf(model.getValueAt(row, COL_ID)));
        final String rawWard = (String) wardBox.getSelectedItem();
        final String rawRoom = roomField.getText().trim();
        final String day = (String) dayBox.getSelectedItem();
        final String time = timeField.getText().trim();
        final String statusText = (String) statusBox.getSelectedItem();
        final String rawNext = nextField.getText().trim();
        final String gName = emptyToDash(guardianField.getText().trim());
        final String gRelation = (String) relationBox.getSelectedItem();
        final String gPhoneRaw = phoneField.getText().trim();
        String error = checkDetails(rawWard, rawRoom, statusText, time, rawNext);
        if (error == null) error = checkGuardian(guardianField.getText().trim(), gPhoneRaw, false);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error);
            return;
        }
        final String gPhone = emptyToDash(gPhoneRaw);
        final String next = cleanNext(rawNext, statusText);
        final String ward = cleanWard(rawWard, statusText);
        final String room = cleanRoom(ward, rawRoom, statusText);

        dbExecutor.submit(() -> {
            try {
                db.updatePatient(id, ward, room, day, time, next, statusText,
                        gName, gRelation, gPhone);
                setStatus("Patient updated (ID " + id + ")");
                refreshPatients();
            } catch (IOException e) {
                setStatus("Update error: " + e.getMessage());
            }
        });
    }

    private void dischargeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient first.");
            return;
        }
        final int id = Integer.parseInt(String.valueOf(model.getValueAt(row, COL_ID)));
        int ok = JOptionPane.showConfirmDialog(this,
                "Discharge this patient? (discharge date/time is saved, room is freed, no more appointments)",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        dbExecutor.submit(() -> {
            try {
                db.dischargePatient(id);
                setStatus("Patient discharged (ID " + id + ")");
                SwingUtilities.invokeLater(() -> {
                    statusBox.setSelectedItem("Discharged");
                    wardBox.setSelectedItem("None");
                    roomField.setText("");
                    nextField.setText("");
                });
                refreshPatients();
            } catch (IOException e) {
                setStatus("Discharge error: " + e.getMessage());
            }
        });
    }

    private void deletePatient() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient first.");
            return;
        }
        final int id = Integer.parseInt(String.valueOf(model.getValueAt(row, COL_ID)));

        dbExecutor.submit(() -> {
            try {
                db.deletePatient(id);
                setStatus("Patient deleted (ID " + id + ")");
                refreshPatients();
            } catch (IOException e) {
                setStatus("Delete error: " + e.getMessage());
            }
        });
    }
}
