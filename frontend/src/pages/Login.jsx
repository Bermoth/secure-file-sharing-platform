import { useState } from "react";
import {
    Link,
    useNavigate
} from "react-router-dom";

import { login } from "../services/api";

export default function Login() {
    const navigate = useNavigate();

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");

    async function handleSubmit(event) {
        event.preventDefault();

        setError("");

        if (!email || !password) {
            setError(
                "Please enter your email and password."
            );
            return;
        }

        setLoading(true);

        try {
            const data =
                await login(
                    email,
                    password
                );

            if (
                typeof data === "object" &&
                data
            ) {
                localStorage.setItem(
                    "user",
                    JSON.stringify({
                        email
                    })
                );
            } else {
                localStorage.setItem(
                    "user",
                    JSON.stringify({
                        email
                    })
                );
            }

            navigate("/dashboard");
        } catch (err) {
            setError(
                err.message ||
                "Login failed."
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

                <h2>Welcome back</h2>

                <p className="auth-subtitle">
                    Sign in to access your secure files.
                </p>

                <form
                    className="auth-form"
                    onSubmit={handleSubmit}
                >
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
                            ? "Signing in..."
                            : "Sign in"}
                    </button>
                </form>

                <p className="auth-footer">
                    Don't have an account?{" "}
                    <Link to="/register">
                        Create one
                    </Link>
                </p>
            </div>
        </div>
    );
}