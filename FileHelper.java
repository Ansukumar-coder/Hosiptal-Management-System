import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileHelper {

    private static final String FILE_NAME = "patients.txt";
    private final File file = new File(FILE_NAME);
    public void init() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public synchronized List<Patient> getAllPatients() throws IOException {
        List<Patient> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 5) continue;
                try {
                    list.add(new Patient(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]),
                            p[3], p[4],
                            get(p, 5, "Monday"), get(p, 6, "10:00"), get(p, 7, "None"),
                            get(p, 8, "Under Treatment"), get(p, 9, "-"),
                            get(p, 10, "None"), get(p, 11, "-"),
                            get(p, 12, "-"), get(p, 13, "-"), get(p, 14, "-"), get(p, 15, "-")));
                } catch (NumberFormatException ignored) {
                    
                }
            }
        }
        return list;
    }

    public synchronized void addPatient(String name, int age, String disease, String doctor,
                                        String visitDay, String visitTime, String nextAppointment,
                                        String status, String ward, String roomNo,
                                        String guardianName, String guardianRelation,
                                        String guardianPhone) throws IOException {
        List<Patient> all = getAllPatients();
        int nextId = 1;
        for (Patient p : all) {
            if (p.getId() >= nextId) nextId = p.getId() + 1;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dischargedOn = status.equals("Discharged") ? now() : "-";
        all.add(new Patient(nextId, name, age, disease, doctor, visitDay, visitTime,
                nextAppointment, status, today, ward, roomNo, dischargedOn,
                guardianName, guardianRelation, guardianPhone));
        writeAll(all);
    }

    public synchronized void updatePatient(int id, String ward, String roomNo, String visitDay,
                                           String visitTime, String nextAppointment, String status,
                                           String guardianName, String guardianRelation,
                                           String guardianPhone) throws IOException {
        List<Patient> all = getAllPatients();
        for (Patient p : all) {
            if (p.getId() == id) {
                boolean wasDischarged = p.getStatus().equals("Discharged");
                p.setWard(ward);
                p.setRoomNo(roomNo);
                p.setVisitDay(visitDay);
                p.setVisitTime(visitTime);
                p.setNextAppointment(nextAppointment);
                p.setStatus(status);
                p.setGuardianName(guardianName);
                p.setGuardianRelation(guardianRelation);
                p.setGuardianPhone(guardianPhone);
                if (status.equals("Discharged")) {
                    if (!wasDischarged) p.setDischargedOn(now());
                } else {
                    p.setDischargedOn("-");
                }
            }
        }
        writeAll(all);
    }
    public synchronized void dischargePatient(int id) throws IOException {
        List<Patient> all = getAllPatients();
        for (Patient p : all) {
            if (p.getId() == id) {
                if (!p.getStatus().equals("Discharged")) p.setDischargedOn(now());
                p.setStatus("Discharged");
                p.setNextAppointment("None");
                p.setWard("None");
                p.setRoomNo("-");
            }
        }
        writeAll(all);
    }

    public synchronized void deletePatient(int id) throws IOException {
        List<Patient> all = getAllPatients();
        all.removeIf(p -> p.getId() == id);
        writeAll(all);
    }
    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    private void writeAll(List<Patient> all) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
            for (Patient p : all) {
                pw.println(p.getId() + "|" + clean(p.getName()) + "|" + p.getAge() + "|"
                        + clean(p.getDisease()) + "|" + clean(p.getDoctor()) + "|"
                        + clean(p.getVisitDay()) + "|" + clean(p.getVisitTime()) + "|"
                        + clean(p.getNextAppointment()) + "|" + clean(p.getStatus()) + "|"
                        + clean(p.getRegisteredOn()) + "|" + clean(p.getWard()) + "|"
                        + clean(p.getRoomNo()) + "|" + clean(p.getDischargedOn()) + "|"
                        + clean(p.getGuardianName()) + "|" + clean(p.getGuardianRelation()) + "|"
                        + clean(p.getGuardianPhone()));
            }
        }
    }

    private String get(String[] parts, int index, String defaultValue) {
        return (index < parts.length && !parts[index].isEmpty()) ? parts[index] : defaultValue;
    }
    private String clean(String text) {
        return text.replace("|", "/");
    }
}
