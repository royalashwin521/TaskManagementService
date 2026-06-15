// --- Configuration ---
const AUTH_URL = 'http://localhost:8081/api/v1/auth';
const TMS_URL = 'http://localhost:8082/api/v1';
let currentPage = 0;

// --- App Initialization ---
window.onload = () => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
        showDashboard();
    } else {
        showLogin();
    }
};

function showLogin() {
    document.getElementById('loginSection').classList.remove('d-none');
    document.getElementById('dashboardSection').classList.add('d-none');
    document.getElementById('logoutBtn').classList.add('d-none');
}

function showDashboard() {
    document.getElementById('loginSection').classList.add('d-none');
    document.getElementById('dashboardSection').classList.remove('d-none');
    document.getElementById('logoutBtn').classList.remove('d-none');
    loadTasks(0);
}

// --- 1. Login Logic ---
document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const errorBox = document.getElementById('loginError');
    errorBox.classList.add('d-none');

    try {
        const response = await fetch(`${AUTH_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, password: password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('jwt_token', data.token); // Save Token
            showDashboard();
        } else {
            errorBox.innerText = "Invalid email or password.";
            errorBox.classList.remove('d-none');
        }
    } catch (error) {
        errorBox.innerText = "Cannot connect to Auth Server (Port 8081).";
        errorBox.classList.remove('d-none');
    }
});

// --- 2. Logout Logic ---
document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.removeItem('jwt_token'); // Remove Token
    showLogin();
});

// --- 3. Fetch Tasks Logic ---
async function loadTasks(page) {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;

    try {
        const response = await fetch(`${TMS_URL}/tasks?page=${page}&size=5&sort=createdAt,desc`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` } // Attach Token
        });

        if (response.status === 401) {
            alert("Session expired. Please log in again.");
            localStorage.removeItem('jwt_token');
            showLogin();
            return;
        }

        const data = await response.json();
        const tbody = document.getElementById('taskTableBody');
        tbody.innerHTML = '';

        if (data.content) {
            data.content.forEach(task => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td class="align-middle">${task.title}</td>
                    <td class="align-middle">${task.priority}</td>
                    <td class="align-middle">
                        <select class="form-select form-select-sm w-auto" onchange="updateTaskStatus('${task.id}', this.value)">
                            <option value="TODO" ${task.status === 'TODO' ? 'selected' : ''}>TODO</option>
                            <option value="IN_PROGRESS" ${task.status === 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                            <option value="DONE" ${task.status === 'DONE' ? 'selected' : ''}>DONE</option>
                        </select>
                    </td>
                    <td class="align-middle">
                        <button class="btn btn-sm btn-danger fw-bold" onclick="deleteTask('${task.id}')">Delete</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });

            // Update Pagination UI
            document.getElementById('pageIndicator').innerText = `Page ${data.number + 1} of ${Math.max(1, data.totalPages)}`;
            document.getElementById('prevBtn').disabled = data.first;
            document.getElementById('nextBtn').disabled = data.last;
            currentPage = data.number;
        }
    } catch (error) {
        console.error("Error fetching tasks:", error);
    }
}

// --- 4. Create Task Logic ---
document.getElementById('createTaskForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const token = localStorage.getItem('jwt_token');

    // Reset Alerts
    document.getElementById('successAlert').classList.add('d-none');
    document.getElementById('errorAlert').classList.add('d-none');
    document.querySelectorAll('.error-text').forEach(el => el.style.display = 'none');

    const requestData = {
        projectId: document.getElementById('projectId').value.trim(),
        title: document.getElementById('taskTitle').value.trim(),
        priority: document.getElementById('taskPriority').value
    };

    // Frontend Validation
    let hasError = false;
    if (!requestData.title) { document.getElementById('titleError').style.display = 'block'; document.getElementById('titleError').innerText = "Required"; hasError = true; }
    if (!requestData.projectId) { document.getElementById('projectIdError').style.display = 'block'; document.getElementById('projectIdError').innerText = "Required"; hasError = true; }
    if (hasError) return;

    try {
        const response = await fetch(`${TMS_URL}/tasks`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` // Attach Token
            },
            body: JSON.stringify(requestData)
        });

        if (response.ok) {
            document.getElementById('successAlert').classList.remove('d-none');
            document.getElementById('createTaskForm').reset();
            loadTasks(0); // Refresh Table
        } else {
            const responseData = await response.json();
            // Map Backend Validation Errors to UI
            if (response.status === 400 && responseData.errors) {
                if (responseData.errors.title) { document.getElementById('titleError').innerText = responseData.errors.title; document.getElementById('titleError').style.display = 'block'; }
                if (responseData.errors.projectId) { document.getElementById('projectIdError').innerText = responseData.errors.projectId; document.getElementById('projectIdError').style.display = 'block'; }
            } else {
                document.getElementById('errorAlert').innerText = responseData.message || "An error occurred.";
                document.getElementById('errorAlert').classList.remove('d-none');
            }
        }
    } catch (error) {
        document.getElementById('errorAlert').innerText = "Failed to connect to Task Server (Port 8082).";
        document.getElementById('errorAlert').classList.remove('d-none');
    }
});

// --- 5. Update Task Status Logic ---
async function updateTaskStatus(taskId, newStatus) {
    const token = localStorage.getItem('jwt_token');
    try {
        const response = await fetch(`${TMS_URL}/tasks/${taskId}/status`, {
            method: 'PATCH', // Assumes your backend uses PATCH for status updates
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ status: newStatus })
        });

        if (!response.ok) {
            alert("Failed to update task status.");
            loadTasks(currentPage); // Revert dropdown on failure
        }
    } catch (error) {
        console.error("Error updating status:", error);
        loadTasks(currentPage);
    }
}

// --- 6. Delete Task Logic ---
async function deleteTask(taskId) {
    if (!confirm("Are you sure you want to delete this task?")) return;

    const token = localStorage.getItem('jwt_token');
    try {
        const response = await fetch(`${TMS_URL}/tasks/${taskId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            loadTasks(currentPage); // Refresh Table
        } else {
            alert("Failed to delete task. You might not have permission.");
        }
    } catch (error) {
        console.error("Error deleting task:", error);
        alert("Failed to connect to the server.");
    }
}

// --- 7. Pagination Buttons ---
document.getElementById('prevBtn').addEventListener('click', () => loadTasks(currentPage - 1));
document.getElementById('nextBtn').addEventListener('click', () => loadTasks(currentPage + 1));