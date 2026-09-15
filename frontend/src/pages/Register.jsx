import { useState } from "react";

import {
    Link,
    useNavigate
} from "react-router-dom";

import { register } from "../services/api";

export default function Register() {
    const navigate = useNavigate();

    const [username, setUsername] =
        useState("");

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [confirmPassword, setConfirmPassword] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");

    async function handleSubmit(event) {
        event.preventDefault();

        setError("");

        if (
            !username ||
            !email ||
            !password ||
            !confirmPassword
        ) {
            setError(
                "Please fill in all fields."
            );
            return;
        }

        if (password !== confirmPassword) {
            setError(
                "Passwords do not match."
            );
            return;
        }

        if (password.length < 6) {
            setError(
                "Password must contain at least 6 characters."
            );
            return;
        }

        setLoading(true);

        try {
            await register(
                username,
                email,
                password
            );

            navigate("/login");
        } catch (err) {
            setError(
                err.message ||
                "Registration failed."
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="auth-logo">
                    <div className="logo-icon">
                        S
                    </div>

                    <h1>SecureShare</h1>
                </div>

                <h2>Create account</h2>

                <p className="auth-subtitle">
                    Create an account to securely manage your files.
                </p>

                <form
                    className="auth-form"
                    onSubmit={handleSubmit}
                >
                    <label>
                        Username
                        <input
                            type="text"
                            placeholder="Your username"
                            value={username}
                            onChange={(event) =>
                                setUsername(
                                    event.target.value
                                )
                            }
                        />
                    </label>

                    <label>
                        Email
                        <input
                            type="email"
                            placeholder="you@example.com"
                            value={email}
                            onChange={(event) =>
                                setEmail(
                                    event.target.value
                                )
                            }
                        />
                    </label>

                    <label>
                        Password
                        <input
                            type="password"
                            placeholder="••••••••"
                            value={password}
                            onChange={(event) =>
                                setPassword(
                                    event.target.value
                                )
                            }
                        />
                    </label>

                    <label>
                        Confirm password
                        <input
                            type="password"
                            placeholder="••••••••"
                            value={confirmPassword}
                            onChange={(event) =>
                                setConfirmPassword(
                                    event.target.value
                                )
                            }
                        />
                    </label>

                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        className="primary-button full-width"
                        disabled={loading}
                    >
                        {loading
                            ? "Creating account..."
                            : "Create account"}
                    </button>
                </form>

                <p className="auth-footer">
                    Already have an account?{" "}
                    <Link to="/login">
                        Sign in
                    </Link>
                </p>
            </div>
        </div>
    );
}