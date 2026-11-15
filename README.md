#  Hospital Management System

A Java desktop application for managing common hospital workflows. It uses a Swing graphical interface and stores application data in local text files.

## Features

- **Dashboard** with hospital overview information
- **Patient records** with visit, ward, status, guardian, and photo details
- **Doctor records** with specialization, contact, and availability
- **Appointment management**
- **Billing records**
- **Bed management**
- **Activity logging**
- An additional legacy hospital patient workflow in `HospitalGUI`

```bash
  

## 📂 Project Structure
├── Main.java             # Application entry point
├── MediCareGUI.java      # Main MediCare interface
├── HospitalGUI.java      # Legacy hospital workflow
├── Patient.java          # Patient data model
├── FileHelper.java       # Patient data file operations
├── ModuleStore.java      # Module data storage
└── AutoRefreshThread.java # Refersh patient list every five sec

