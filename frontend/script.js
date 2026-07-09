// ===== Configuration =====
// Change this to your deployed backend URL when deploying to Vercel/Railway
// e.g., const API_BASE_URL = "https://your-backend.up.railway.app/api";
const API_BASE_URL = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1" 
  ? "http://localhost:8080/api" 
  : "https://ai-coding-interview-simulator-production.up.railway.app/api";

// ===== Elements =====
const authBox = document.getElementById("auth-box");
const authTitle = document.getElementById("auth-title");
const usernameInput = document.getElementById("username-input");
const passwordInput = document.getElementById("password-input");
const authSubmitBtn = document.getElementById("auth-submit-btn");
const authMessage = document.getElementById("auth-message");
const authToggleText = document.getElementById("auth-toggle-text");
const authToggleLink = document.getElementById("auth-toggle-link");

const appContent = document.getElementById("app-content");
const welcomeUsername = document.getElementById("welcome-username");
const logoutLink = document.getElementById("logout-link");

const practiceView = document.getElementById("practice-view");
const historyBox = document.getElementById("history-box");
const weakAreasBox = document.getElementById("weak-areas-box");

const newQuestionBtn = document.getElementById("new-question-btn");
const topicSelect = document.getElementById("topic-select");
const questionText = document.getElementById("question-text");
const methodSignature = document.getElementById("method-signature");
const submitBtn = document.getElementById("submit-btn");
const codeInput = document.getElementById("code-input");
const resultText = document.getElementById("result-text");

const historyLink = document.getElementById("history-link");
const historyList = document.getElementById("history-list");
const closeHistoryBtn = document.getElementById("close-history-btn");
const lineNumbers = document.getElementById("line-numbers");
const tagTopic = document.getElementById("tag-topic");

const interviewerBox = document.getElementById("interviewer-box");
const followupQuestion = document.getElementById("followup-question");
const followupAnswer = document.getElementById("followup-answer");
const followupSubmitBtn = document.getElementById("followup-submit-btn");
const followupFeedback = document.getElementById("followup-feedback");

const weakAreasLink = document.getElementById("weak-areas-link");
const weakAreasSummary = document.getElementById("weak-areas-summary");
const weakAreasList = document.getElementById("weak-areas-list");
const closeWeakAreasBtn = document.getElementById("close-weak-areas-btn");

let currentTestCases = [];
let currentFollowUpQuestion = "";
let isSignupMode = true;

// ===== View Switching (only ONE of these three is visible at a time) =====
function showPracticeView() {
  practiceView.style.display = "block";
  historyBox.style.display = "none";
  weakAreasBox.style.display = "none";
}

function showHistoryView() {
  practiceView.style.display = "none";
  historyBox.style.display = "block";
  weakAreasBox.style.display = "none";
}

function showWeakAreasView() {
  practiceView.style.display = "none";
  historyBox.style.display = "none";
  weakAreasBox.style.display = "block";
}

// ===== Line number gutter for code editor =====
function updateLineNumbers() {
  const lines = codeInput.value.split("\n").length;
  let numbers = "";
  for (let i = 1; i <= lines; i++) {
    numbers += i + "\n";
  }
  lineNumbers.textContent = numbers;
}

codeInput.addEventListener("input", updateLineNumbers);
codeInput.addEventListener("scroll", () => {
  lineNumbers.scrollTop = codeInput.scrollTop;
});

// ===== Toggle between Sign Up / Log In =====
authToggleLink.addEventListener("click", (e) => {
  e.preventDefault();
  isSignupMode = !isSignupMode;

  if (isSignupMode) {
    authTitle.textContent = "Sign Up";
    authSubmitBtn.textContent = "Sign Up";
    authToggleText.textContent = "Already have an account?";
    authToggleLink.textContent = "Log in";
  } else {
    authTitle.textContent = "Log In";
    authSubmitBtn.textContent = "Log In";
    authToggleText.textContent = "Don't have an account?";
    authToggleLink.textContent = "Sign up";
  }
  authMessage.textContent = "";
});

authSubmitBtn.addEventListener("click", async () => {
  const username = usernameInput.value.trim();
  const password = passwordInput.value.trim();

  if (username === "" || password === "") {
    authMessage.textContent = "Please fill in both fields.";
    authMessage.style.color = "red";
    return;
  }

  const endpoint = isSignupMode ? "signup" : "login";

  try {
    const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    const message = await response.text();

    if (message.includes("successful")) {
      if (endpoint === "login") {
        welcomeUsername.textContent = username;
        authBox.style.display = "none";
        appContent.style.display = "block";
        localStorage.setItem("username", username);
        showPracticeView();
      } else {
        authMessage.textContent = message + " Please log in now.";
        authMessage.style.color = "green";
        isSignupMode = false;
        authTitle.textContent = "Log In";
        authSubmitBtn.textContent = "Log In";
        authToggleText.textContent = "Don't have an account?";
        authToggleLink.textContent = "Sign up";
      }
    } else {
      authMessage.textContent = message;
      authMessage.style.color = "red";
    }

  } catch (error) {
    authMessage.textContent = "Could not reach backend. Is it running?";
    authMessage.style.color = "red";
    console.error(error);
  }
});

logoutLink.addEventListener("click", (e) => {
  e.preventDefault();
  localStorage.removeItem("username");
  appContent.style.display = "none";
  authBox.style.display = "block";
  usernameInput.value = "";
  passwordInput.value = "";
});

window.addEventListener("load", () => {
  const savedUsername = localStorage.getItem("username");
  if (savedUsername) {
    welcomeUsername.textContent = savedUsername;
    authBox.style.display = "none";
    appContent.style.display = "block";
    showPracticeView();
  }
  updateLineNumbers();
});

// ===== Get New Question =====
newQuestionBtn.addEventListener("click", async () => {
  const topic = topicSelect.value;
  questionText.innerHTML = '<span class="loading-spinner"></span>Generating question...';
  methodSignature.textContent = "";
  resultText.textContent = "Submit your code to see results here.";

  // Reset: a new question means the old interviewer follow-up no longer applies
  interviewerBox.style.display = "none";
  followupQuestion.textContent = "";
  followupAnswer.value = "";
  followupFeedback.textContent = "";

  try {
    const response = await fetch(`${API_BASE_URL}/generate-question?topic=${topic}`);
    const data = await response.json();

    questionText.textContent = data.questionText;
    methodSignature.textContent = data.methodSignature;
    tagTopic.textContent = topic.charAt(0).toUpperCase() + topic.slice(1);
    codeInput.value = data.methodSignature + " {\n    // your code here\n}";
    updateLineNumbers();

    currentTestCases = data.testCases.map(tc => ({
      call: tc.call,
      expected: tc.expected
    }));

  } catch (error) {
    questionText.textContent = "Could not generate question. Try again.";
    console.error(error);
  }
});

// ===== Submit Code =====
submitBtn.addEventListener("click", async () => {
  const code = codeInput.value.trim();

  if (code === "") {
    resultText.textContent = "Please write some code before submitting.";
    resultText.style.color = "red";
    return;
  }

  if (currentTestCases.length === 0) {
    resultText.textContent = "Please get a question first!";
    resultText.style.color = "red";
    return;
  }

  resultText.innerHTML = '<span class="loading-spinner"></span>Running tests and getting AI feedback...';

  try {
    const response = await fetch(`${API_BASE_URL}/submit`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: localStorage.getItem("username"),
        questionText: questionText.textContent,
        topic: topicSelect.value,
        code: code,
        testCases: currentTestCases
      })
    });

    const message = await response.text();
    resultText.innerHTML = formatResults(message);

    // Only NOW does the interviewer follow-up appear, after a real submission
    getFollowUpQuestion(questionText.textContent, code);

  } catch (error) {
    resultText.textContent = "Could not reach backend. Is it running?";
    console.error(error);
  }
});

// Turns plain text results into colored, readable HTML
function formatResults(message) {
  return message
    .split("\n")
    .map(line => {
      if (line.startsWith("===")) {
        return `<span class="section-title">${line.replace(/=/g, "").trim()}</span>`;
      }
      if (line.includes("PASS")) {
        return `<div class="test-pass">✅ ${line}</div>`;
      }
      if (line.includes("FAIL")) {
        return `<div class="test-fail">❌ ${line}</div>`;
      }
      if (line.includes("ERROR") || line.includes("Error")) {
        return `<div class="test-error">⚠️ ${line}</div>`;
      }
      return `<div>${line}</div>`;
    })
    .join("");
}

// ===== Interviewer Follow-Up (only triggered after a submission) =====
async function getFollowUpQuestion(questionTextValue, codeValue) {
  interviewerBox.style.display = "block";
  followupQuestion.innerHTML = '<span class="loading-spinner"></span>Interviewer is thinking of a follow-up...';
  followupFeedback.textContent = "";
  followupAnswer.value = "";

  try {
    const response = await fetch(`${API_BASE_URL}/follow-up`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        questionText: questionTextValue,
        code: codeValue
      })
    });

    const question = await response.text();
    currentFollowUpQuestion = question;
    followupQuestion.textContent = "🎙️ " + question;

  } catch (error) {
    followupQuestion.textContent = "Could not load follow-up question.";
    console.error(error);
  }
}

followupSubmitBtn.addEventListener("click", async () => {
  const answer = followupAnswer.value.trim();
  if (answer === "") {
    followupFeedback.textContent = "Please type an answer first.";
    return;
  }

  followupFeedback.innerHTML = '<span class="loading-spinner"></span>Evaluating your answer...';

  try {
    const response = await fetch(`${API_BASE_URL}/follow-up-answer`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        followUpQuestion: currentFollowUpQuestion,
        answer: answer,
        code: codeInput.value
      })
    });

    const feedback = await response.text();
    followupFeedback.textContent = "💬 " + feedback;

  } catch (error) {
    followupFeedback.textContent = "Could not evaluate your answer.";
    console.error(error);
  }
});

// ===== View Submission History =====
historyLink.addEventListener("click", async (e) => {
  e.preventDefault();
  const username = localStorage.getItem("username");

  historyList.innerHTML = "Loading...";
  showHistoryView();

  try {
    const response = await fetch(`${API_BASE_URL}/history/${username}`);
    const submissions = await response.json();

    if (submissions.length === 0) {
      historyList.innerHTML = "<p>No submissions yet. Go solve something!</p>";
      return;
    }

    historyList.innerHTML = submissions.map(sub => `
      <div style="border-bottom:1px solid #333; padding:10px 0;">
        <p><strong>${sub.allTestsPassed ? "PASSED" : "FAILED"}</strong> - ${new Date(sub.submittedAt).toLocaleString()}</p>
        <p>${sub.questionText.substring(0, 100)}...</p>
      </div>
    `).join("");

  } catch (error) {
    historyList.innerHTML = "Could not load history.";
    console.error(error);
  }
});

closeHistoryBtn.addEventListener("click", () => {
  showPracticeView();
});

// ===== Weak Area Tracking =====
weakAreasLink.addEventListener("click", async (e) => {
  e.preventDefault();
  const username = localStorage.getItem("username");

  weakAreasSummary.innerHTML = '<span class="loading-spinner"></span>Analyzing your progress...';
  weakAreasList.innerHTML = "";
  showWeakAreasView();

  try {
    const response = await fetch(`${API_BASE_URL}/weak-areas/${username}`);
    const data = await response.json();

    weakAreasSummary.textContent = "💬 " + data.aiSummary;

    if (data.topics.length === 0) {
      weakAreasList.innerHTML = "<p>No submissions yet.</p>";
      return;
    }

    weakAreasList.innerHTML = data.topics.map(t => `
      <div style="margin-bottom:14px;">
        <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
          <strong>${t.topic}</strong>
          <span>${t.passes}/${t.attempts} passed (${t.passRate}%)</span>
        </div>
        <div style="background:#333; border-radius:6px; height:8px; overflow:hidden;">
          <div style="background:${t.passRate >= 70 ? '#2cbb5d' : t.passRate >= 40 ? '#ffa116' : '#ef4743'}; height:100%; width:${t.passRate}%;"></div>
        </div>
      </div>
    `).join("");

  } catch (error) {
    weakAreasSummary.textContent = "Could not load your progress.";
    console.error(error);
  }
});

closeWeakAreasBtn.addEventListener("click", () => {
  showPracticeView();
});