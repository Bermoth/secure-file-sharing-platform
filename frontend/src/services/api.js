const API_URL = "http://localhost:8080";

function getToken() {
    return localStorage.getItem("token");
}

async function request(endpoint, options = {}) {
    const token = getToken();

    const headers = {
        ...(options.headers || {})
    };

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        headers
    });

    if (response.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        window.location.href = "/login";
        throw new Error("Your session has expired. Please log in again.");
    }

    if (!response.ok) {
        let message = "Something went wrong.";

        try {
            const data = await response.json();
            message =
                data.message ||
                data.error ||
                message;
        } catch {
            try {
                const text = await response.text();
                if (text) {
                    message = text;
                }
            } catch {
                // Ignore parsing errors
            }
        }

        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }

    return response;
}

export async function login(email, password) {
    const response = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email,
            password
        })
    });

    if (!response.ok) {
        let message = "Invalid email or password.";

        try {
            const data = await response.json();

            message =
                data.message ||
                data.error ||
                message;
        } catch {
            try {
                const text = await response.text();

                if (text) {
                    message = text;
                }
            } catch {
                // Ignore
            }
        }

        throw new Error(message);
    }

    const token = await response.text();

    if (!token) {
        throw new Error(
            "Login succeeded but no JWT token was returned."
        );
    }

    localStorage.setItem("token", token);

    return token;
}

export async function register(username, email, password) {
    return request("/users", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username,
            email,
            password
        })
    });
}

export async function getMyFiles() {
    return request("/files/myFiles");
}

export async function getSharedFiles() {
    return request("/files/shared");
}

export async function uploadFile(file) {
    const token = getToken();

    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${API_URL}/files/upload`, {
        method: "POST",
        headers: {
            Authorization: `Bearer ${token}`
        },
        body: formData
    });

    if (!response.ok) {
        let message = "Could not upload file.";

        try {
            const data = await response.json();
            message =
                data.message ||
                data.error ||
                message;
        } catch {
            // Ignore
        }

        throw new Error(message);
    }

    return response.json();
}

export async function downloadFile(fileId) {
    const token = getToken();

    const response = await fetch(
        `${API_URL}/files/${fileId}/download`,
        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    if (!response.ok) {
        throw new Error("Could not download file.");
    }

    return response.blob();
}

export async function deleteFile(fileId) {
    return request(`/files/${fileId}`, {
        method: "DELETE"
    });
}

export async function shareFile(fileId, email) {
    return request(
        `/files/${fileId}/share?email=${encodeURIComponent(email)}`,
        {
            method: "POST"
        }
    );
}

export async function getFileShares(fileId) {
    return request(`/files/${fileId}/shares`);
}

export async function revokeShare(fileId, userId) {
    return request(
        `/files/${fileId}/share/${userId}`,
        {
            method: "DELETE"
        }
    );
}