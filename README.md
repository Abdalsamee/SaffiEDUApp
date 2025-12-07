📘 SaffiEDUApp – Digital Classroom & Smart Exam Monitoring

A Comprehensive Android-Based Educational Environment

📄 Overview

SaffiEDUApp is an integrated digital classroom application designed to recreate the real school environment within a unified Android platform.
The system provides lesson management, exam administration, assignments, real-time communication, and intelligent exam monitoring — all within a single ecosystem optimized for students and teachers.

The project was developed in response to major educational challenges, especially the severe disruption of schooling in Gaza, where more than 80% of school buildings have been destroyed. The app aims to offer a sustainable digital alternative that ensures continuity of learning, fairness in evaluation, and real-time interaction in a secure and organized environment.
🎯 Problem Statement

Existing educational platforms provide content and assessments, but they lack an integrated classroom experience that includes:

Classroom management

Academic discipline tools

Real-time student monitoring

Smart exam proctoring

Unified access for students and teachers

In digital environments, cheating during exams remains a major issue. Current solutions do not provide reliable dual-camera monitoring, automated cheating detection, or consistent tracking of academic behavior.
SaffiEDUApp addresses this gap by offering a complete, connected, and easy-to-use educational ecosystem.

🎯 Project Goals
Main Objective

Develop an educational Android application that simulates a real classroom environment, ensuring fair, interactive, and structured learning for both students and teachers.

Sub-Objectives

Analyze challenges in e-learning and academic integrity.

Build an organized system with separate roles for students and teachers.

Develop smart camera-based monitoring for online exams.

Create a central data management system for tracking academic progress.

Test the ease of use, reliability, and performance with real users.

⭐ Key Features
🎓 Student Features

Attend classes and access course materials.

Watch lessons (video/PDF) online or offline.

Submit assignments and complete exams.

Receive real-time notifications and alerts.

Communicate with teachers through an integrated chat.

👨‍🏫 Teacher Features

Upload lessons (videos, PDFs, documents).

Create assignments and exams.

Track student progress and performance.

Send notifications to students.

Review intelligent monitoring reports for exams.

🔐 Smart Exam Monitoring (Dual-Camera Proctoring Module)

Uses front and back cameras concurrently.

Detects suspicious behavior (face absence, looking away, sudden movements).

Prevents app switching, split-screen, and background app usage.

Generates automated exam reports with:

Random captured images

Short video clips

ML Kit-based detection logs

All monitoring happens locally on the device to ensure privacy.

🧪 Requirements Analysis Summary
Information Collection Methods

Surveys for students

Interviews with students and one teacher

Document analysis of e-learning practices

Key Findings

Students need simplicity, offline access, and unified content.

Teachers need a structured environment with strong monitoring tools.

Both groups emphasized fairness and transparency in digital assessments.

Smart exam monitoring and real-time communication were highest priority.

🧑‍🤝‍🧑 System Actors
Actor	Responsibilities
Student	Attend lessons, submit assignments, take exams
Teacher	Upload content, create exams, monitor progress
Admin	Manage teacher accounts, resolve technical issues
⚙ Functional Requirements
✔ Students

View lessons (videos/PDF)

Download content offline

Submit assignments

Take monitored exams

✔ Teachers

Upload lessons and exams

Send notifications

Review performance and monitoring reports

✔ System

Dual-camera monitoring

Real-time notifications

Authentication and role-based access

🛡 Non-Functional Requirements

Performance: Home screen loads ≤ 0.5s

Security: Firebase Auth, HTTPS encryption

Reliability: 99% uptime

Scalability: Supports thousands of users via Firebase

Compatibility: Works on modern Android devices

🏗️ System Architecture

SaffiEDUApp follows the Client–Server model directly connected to Firebase services.
It consists of three main layers:

1. Presentation Layer (Android – Jetpack Compose)

Kotlin + Jetpack Compose

MVVM + Clean Architecture

Modules: Authentication, Lessons, Exams, Tasks, Chat, Profile

2. Application Logic & Integration Layer (Firebase)

Firebase Authentication

Cloud Firestore

Firebase Storage

WorkManager for background tasks and offline sync

3. Smart Monitoring Layer (On-Device ML)

ML Kit for face detection and gaze tracking

Detection of screen exits, multitasking, and cheating patterns

Local processing for privacy

Automated report generation (images + video)

🛠 Tools & Technologies

| Category         | Tools / Frameworks                      | Purpose                             |
| ---------------- | --------------------------------------- | ----------------------------------- |
| Hardware         | Windows laptops, Smartphones            | Development and real-device testing |
| Development      | Android Studio, Kotlin, Jetpack Compose | Core app implementation             |
| Backend Services | Firebase Auth, Firestore, Storage       | Data management and authentication  |
| Design           | Figma                                   | UI/UX design                        |
| Management       | GitHub, Google Meet, WhatsApp           | Collaboration & communication       |

▶️ How to Run

1- Clone the repository:

git clone https://github.com/Abdalsamee/SaffiEDUApp.git


2- Open project in Android Studio (Giraffe or later).

3- Add your Firebase configuration (google-services.json).

4- Connect an Android device.

5- Build & run the app.

📈 Future Enhancements

Advanced analytics dashboard for teachers

Web admin panel

AI-based performance predictions

Smarter on-device cheating detection

Multi-school and multi-admin support

🏁 Conclusion

SaffiEDUApp demonstrates how technology can recreate a real classroom experience in a digital environment.
The application successfully integrates content management, communication, and intelligent exam monitoring within a unified Android platform. Despite the environmental and emotional challenges faced by the team, the project achieved its primary goals and offered a meaningful contribution toward sustaining education in Gaza.

The system lays a strong foundation for future expansion and represents a practical, scalable, and socially impactful digital learning solution.

