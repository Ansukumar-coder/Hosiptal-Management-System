import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
                            get(p, 12, "-"), get(p, 13, "-"), get(p, 14, "-"), get(p, 15, "-"),
                            get(p, 16, "-"), get(p, 17, "Not specified"), get(p, 18, "-"), get(p, 19, "-")));
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
        addPatient(name, age, disease, doctor, visitDay, visitTime, nextAppointment, status, ward,
                roomNo, guardianName, guardianRelation, guardianPhone, "-");
    }

    public synchronized int addPatient(String name, int age, String disease, String doctor,
                                        String visitDay, String visitTime, String nextAppointment,
                                        String status, String ward, String roomNo,
                                        String guardianName, String guardianRelation,
                                        String guardianPhone, String photoPath) throws IOException {
        return addPatient(name, age, disease, doctor, visitDay, visitTime, nextAppointment, status, ward,
                roomNo, guardianName, guardianRelation, guardianPhone, photoPath, "Not specified", "-", "-");
    }

    public synchronized int addPatient(String name, int age, String disease, String doctor,
                                        String visitDay, String visitTime, String nextAppointment,
                                        String status, String ward, String roomNo,
                                        String guardianName, String guardianRelation,
                                        String guardianPhone, String photoPath, String gender,
                                        String contactNumber, String address) throws IOException {
        List<Patient> all = getAllPatients();
        int nextId = 1;
        for (Patient p : all) {
            if (p.getId() >= nextId) nextId = p.getId() + 1;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dischargedOn = status.equals("Discharged") ? now() : "-";
        String savedPhoto = photoPath == null || photoPath.isEmpty() || photoPath.equals("-") ? "-" : photoPath;
        if (!savedPhoto.equals("-") && new File(savedPhoto).isFile()) {
            File source = new File(savedPhoto);
            String extension = source.getName().contains(".")
                    ? source.getName().substring(source.getName().lastIndexOf('.')) : ".img";
            File photoDirectory = new File("patient_photos");
            if (!photoDirectory.exists() && !photoDirectory.mkdirs()) throw new IOException("Cannot create patient_photos directory.");
            File destination = new File(photoDirectory, "patient_" + nextId + extension);
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            savedPhoto = destination.getAbsolutePath();
        }
        all.add(new Patient(nextId, name, age, disease, doctor, visitDay, visitTime,
                nextAppointment, status, today, ward, roomNo, dischargedOn,
                guardianName, guardianRelation, guardianPhone, savedPhoto, gender, contactNumber, address));
        writeAll(all);
        return nextId;
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

    public synchronized void updatePatientProfile(int id, String name, int age, String gender,
                                                  String contactNumber, String address,
                                                  String disease, String doctor) throws IOException {
        List<Patient> all = getAllPatients();
        for (Patient p : all) if (p.getId() == id) {
            p.setName(name); p.setAge(age); p.setGender(gender); p.setContactNumber(contactNumber);
            p.setAddress(address); p.setDisease(disease); p.setDoctor(doctor);
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
                        + clean(p.getGuardianPhone()) + "|" + clean(p.getPhotoPath()) + "|"
                        + clean(p.getGender()) + "|" + clean(p.getContactNumber()) + "|" + clean(p.getAddress()));
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
