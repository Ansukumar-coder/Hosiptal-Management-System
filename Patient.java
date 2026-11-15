public class Patient {
    private int id;
    private String name;
    private int age;
    private String disease;
    private String doctor;
    private String visitDay;         
    private String visitTime;        
    private String nextAppointment;  
    private String status;           
    private String registeredOn;     
    private String ward;             
    private String roomNo;           
    private String dischargedOn;     
    private String guardianName;     
    private String guardianRelation; 
    private String guardianPhone;    
    private String photoPath;
    private String gender;
    private String contactNumber;
    private String address;

    public Patient(int id, String name, int age, String disease, String doctor,
                   String visitDay, String visitTime, String nextAppointment,
                   String status, String registeredOn, String ward, String roomNo,
                   String dischargedOn, String guardianName, String guardianRelation,
                   String guardianPhone) {
        this(id, name, age, disease, doctor, visitDay, visitTime, nextAppointment, status,
                registeredOn, ward, roomNo, dischargedOn, guardianName, guardianRelation, guardianPhone, "-");
    }

    public Patient(int id, String name, int age, String disease, String doctor,
                   String visitDay, String visitTime, String nextAppointment,
                   String status, String registeredOn, String ward, String roomNo,
                   String dischargedOn, String guardianName, String guardianRelation,
                   String guardianPhone, String photoPath) {
        this(id, name, age, disease, doctor, visitDay, visitTime, nextAppointment, status,
                registeredOn, ward, roomNo, dischargedOn, guardianName, guardianRelation, guardianPhone,
                photoPath, "Not specified", "-", "-");
    }

    public Patient(int id, String name, int age, String disease, String doctor,
                   String visitDay, String visitTime, String nextAppointment,
                   String status, String registeredOn, String ward, String roomNo,
                   String dischargedOn, String guardianName, String guardianRelation,
                   String guardianPhone, String photoPath, String gender, String contactNumber, String address) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.disease = disease;
        this.doctor = doctor;
        this.visitDay = visitDay;
        this.visitTime = visitTime;
        this.nextAppointment = nextAppointment;
        this.status = status;
        this.registeredOn = registeredOn;
        this.ward = ward;
        this.roomNo = roomNo;
        this.dischargedOn = dischargedOn;
        this.guardianName = guardianName;
        this.guardianRelation = guardianRelation;
        this.guardianPhone = guardianPhone;
        this.photoPath = photoPath == null || photoPath.isEmpty() ? "-" : photoPath;
        this.gender = gender == null || gender.isEmpty() ? "Not specified" : gender;
        this.contactNumber = contactNumber == null || contactNumber.isEmpty() ? "-" : contactNumber;
        this.address = address == null || address.isEmpty() ? "-" : address;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDisease() { return disease; }
    public String getDoctor() { return doctor; }
    public String getVisitDay() { return visitDay; }
    public String getVisitTime() { return visitTime; }
    public String getNextAppointment() { return nextAppointment; }
    public String getStatus() { return status; }
    public String getRegisteredOn() { return registeredOn; }
    public String getWard() { return ward; }
    public String getRoomNo() { return roomNo; }
    public String getDischargedOn() { return dischargedOn; }
    public String getGuardianName() { return guardianName; }
    public String getGuardianRelation() { return guardianRelation; }
    public String getGuardianPhone() { return guardianPhone; }
    public String getPhotoPath() { return photoPath; }
    public String getGender() { return gender; }
    public String getContactNumber() { return contactNumber; }
    public String getAddress() { return address; }

    public void setVisitDay(String visitDay) { this.visitDay = visitDay; }
    public void setVisitTime(String visitTime) { this.visitTime = visitTime; }
    public void setNextAppointment(String nextAppointment) { this.nextAppointment = nextAppointment; }
    public void setStatus(String status) { this.status = status; }
    public void setWard(String ward) { this.ward = ward; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public void setDischargedOn(String dischargedOn) { this.dischargedOn = dischargedOn; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public void setGuardianRelation(String guardianRelation) { this.guardianRelation = guardianRelation; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setDisease(String disease) { this.disease = disease; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    public void setGender(String gender) { this.gender = gender; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setAddress(String address) { this.address = address; }
}
