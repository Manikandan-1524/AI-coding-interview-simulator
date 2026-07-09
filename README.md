<div align="center">

# 🎯 AI Coding Interview Simulator

**Practice technical interviews with an AI that generates questions, judges your code, asks live follow-ups, and tracks your progress.**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com)
[![Groq](https://img.shields.io/badge/Groq-Llama_3.3_70B-F55036?style=for-the-badge&logo=meta&logoColor=white)](https://groq.com)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

> Most practice platforms hand you a static question bank and a pass/fail judge. This project goes further — it behaves like an actual interviewer: asking follow-up questions about your solution, analyzing your weak spots over time, and never running out of fresh problems.

</div>

---

<div align="center">

## 📑 Table of Contents

**[Why This Project](#-why-this-project) · [Key Features](#-key-features) · [System Architecture](#️-system-architecture) · [How a Submission Works](#-how-a-submission-actually-works) · [Tech Stack](#-tech-stack) · [Project Structure](#-project-structure) · [Getting Started](#-getting-started) · [Roadmap](#️-roadmap) · [Contact](#-contact)**

</div>

---

<div align="center">

## 🆚 Why This Project

| Capability | Typical Question Bank (LeetCode-style) | **This Project** |
|---|---|---|
| Question source | Fixed, pre-written bank | ♾️ Curated seed bank + AI-generated wording & test cases, so it never feels repetitive |
| Feedback | Pass / Fail only | 🤖 AI reviews code style, efficiency, and approach |
| Interview realism | None — just a judge | 🎙️ AI asks a live follow-up question after every submission, like a real interviewer |
| Progress tracking | Basic solve count | 📈 Per-topic pass-rate analysis with a personalized AI coaching summary |
| Code safety | N/A (cloud sandboxed) | 🛡️ Self-hosted judge with isolated process execution + timeout protection |

</div>

---

<div align="center">

## 🎯 Key Features

| Feature | Description |
|---|---|
| 🔐 **Secure Authentication** | Signup/login with BCrypt-hashed passwords |
| ♾️ **Unlimited Question Variety** | 100+ curated problem seeds across 9 topics, fleshed out by AI so wording and test cases are always fresh |
| ⚖️ **Real Code Judge** | Compiles and runs submitted Java in an isolated process with strict timeouts |
| 🤖 **AI Coaching Feedback** | Reviews every submission for style, efficiency, and approach |
| 🎙️ **Live Interviewer Follow-Ups** | Asks a natural follow-up question after each submission and evaluates your answer |
| 📈 **Weak-Area Tracking** | Aggregates your submission history per topic and generates a personalized improvement summary |
| 🛡️ **Rate Limiting** | Per-IP request throttling to protect against abuse |

</div>

---

<div align="center">

## 🏗️ System Architecture

```
┌──────────────┐        HTTP / JSON        ┌────────────────────┐
│   Frontend   │ ────────────────────────▶ │   Spring Boot API   │
│ HTML·CSS·JS  │ ◀──────────────────────── │      (Java 21)      │
└──────────────┘                            └──────────┬─────────┘
                                                        │
                              ┌─────────────────────────┼─────────────────────────┐
                              ▼                         ▼                         ▼
                     ┌────────────────┐        ┌────────────────┐        ┌────────────────┐
                     │   MySQL DB     │        │   Groq API     │        │  Isolated Java │
                     │ users·history  │        │ (Llama 3.3 70B)│        │  Process Judge │
                     └────────────────┘        └────────────────┘        └────────────────┘
```

### 🔗 Backend Services

| Service | Responsibility |
|---|---|
| `QuestionGeneratorService` | Picks a random unseen problem from the curated bank, asks AI to write full details + test cases |
| `CodeRunnerService` | Compiles & runs submitted code in an isolated process, with a 5s timeout against infinite loops |
| `AiFeedbackService` | Reviews submitted code for style, efficiency, and approach |
| `InterviewerService` | Generates a live follow-up question and evaluates the candidate's answer |
| `WeakAreaService` | Aggregates submission history per topic into pass-rate stats + AI summary |
| `RateLimiterService` | Throttles requests per IP to protect the Groq API quota |

</div>

---

<div align="center">

## 💬 How a Submission Actually Works

```
User submits code
      │
      ▼
┌─────────────────────────────────────────┐
│ 1. CodeRunnerService                     │
│    Compiles code → runs against          │
│    AI-generated test cases → PASS/FAIL   │
└─────────────────┬─────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│ 2. AiFeedbackService                     │
│    Reviews code style & efficiency       │
└─────────────────┬─────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│ 3. Submission saved to MySQL             │
│    (username, topic, pass/fail, code)    │
└─────────────────┬─────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│ 4. InterviewerService                    │
│    Asks a live follow-up question        │
│    e.g. "What's the time complexity?"    │
└───────────────────────────────────────────┘
```

</div>

---

<div align="center">

## 🧱 Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML, CSS, Vanilla JavaScript |
| Backend | Java 21, Spring Boot (REST API) |
| Database | MySQL (Spring Data JPA / Hibernate) |
| AI | Groq API — Llama 3.3 70B |
| Security | BCrypt password hashing, per-IP rate limiting |

</div>

---



## 📁 Project Structure

```
ai-coding-interview-simulator/
│
├── frontend/
│   ├── index.html              Practice UI, auth, history, weak-areas views
│   ├── style.css                Dark, LeetCode-inspired theme
│   └── script.js                 All client-side logic & API calls
│
├── backend/
│   └── src/main/java/com/aisimulator/backend/
│       ├── SubmitController.java        REST endpoints
│       ├── AuthController.java          Signup / login
│       ├── CodeRunnerService.java       Compiles & judges code safely
│       ├── QuestionGeneratorService.java Curated bank + AI question generation
│       ├── AiFeedbackService.java       AI code review
│       ├── InterviewerService.java      Live follow-up Q&A
│       ├── WeakAreaService.java         Per-topic progress analysis
│       ├── RateLimiterService.java      Per-IP throttling
│       ├── User.java / Submission.java  JPA entities
│       └── UserRepository.java / SubmissionRepository.java
│
├── .gitignore
└── README.md
```



---

<div align="center">

## 🚀 Getting Started

</div>

### Prerequisites
- Java 21+
- MySQL 8+
- A free [Groq API key](https://console.groq.com)

### Setup

```bash
git clone https://github.com/YOUR_USERNAME/ai-coding-interview-simulator.git
cd ai-coding-interview-simulator
```

**1. Create the database**
```sql
CREATE DATABASE ai_interview_simulator;
```

**2. Configure the backend**

Create `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai_interview_simulator
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
groq.api.key=YOUR_GROQ_API_KEY
```

**3. Run the backend**
```bash
cd backend
./mvnw spring-boot:run
```

**4. Run the frontend**

Open `frontend/index.html` with a local server (e.g. VS Code's Live Server extension).

---

<div align="center">

## 🗺️ Roadmap

| Stage | Status | Description |
|---|---|---|
| 1 | ✅ Done | Core judge — compile, run, test-case validation, timeout safety |
| 2 | ✅ Done | AI question generation + AI coaching feedback |
| 3 | ✅ Done | Auth, MySQL persistence, submission history |
| 4 | ✅ Done | Live interviewer follow-up Q&A |
| 5 | ✅ Done | Per-topic weak-area tracking with AI insights |
| 6 | 🔲 Planned | Time complexity (Big-O) analysis of submitted code |
| 7 | 🔲 Planned | "Explain your approach first" mode |
| 8 | 🔲 Planned | Multi-language support beyond Java |

</div>

---

<div align="center">

## 🧠 The Mind Behind This

Not just a project — a proof that one person, a laptop, and enough stubbornness can ship something that thinks, judges, coaches, and remembers. Architecture, backend, AI prompt design, database, and UI — built solo, end to end, one bug at a time.

## 📬 Contact

[![Email](https://img.shields.io/badge/Email-mani1524.senthil@gmail.com-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:mani1524.senthil@gmail.com)
[![LinkedIn](https://img.shields.io/badge/https://www.linkedin.com/in/manikandan-s-870b5932b-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/manikandan-s-870b5932b)

</div>
