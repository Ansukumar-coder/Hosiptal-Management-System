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

    public Patient(int id, String name, int age, String disease, String doctor,
                   String visitDay, String visitTime, String nextAppointment,
                   String status, String registeredOn, String ward, String roomNo,
                   String dischargedOn, String guardianName, String guardianRelation,
                   String guardianPhone) {
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
}
