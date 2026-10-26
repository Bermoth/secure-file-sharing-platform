
import { useState } from "react";
import {
    Link,
    useLocation,
    useNavigate
} from "react-router-dom";

import { verifyOtp } from "../services/api";

export default function VerifyOtp() {
    const navigate = useNavigate();
    const location = useLocation();

    const [email, setEmail] = useState(
        location.state?.email || ""
    );
    const [otp, setOtp] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");

        if (!email.trim()) {
            setError("Please enter your email address.");
            return;
        }

        if (!/^\d{6}$/.test(otp)) {
            setError("Please enter the 6-digit verification code.");
            return;
        }

        setLoading(true);

        try {
            await verifyOtp(email.trim(), otp);
            navigate("/login", {
                state: { message: "Email verified successfully. You can now log in." }
            });
        } catch (err) {
            setError(
                err.message || "Verification failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="auth-logo">
                    <div className="logo-icon">S</div>
                    <h1>SecureShare</h1>
                </div>

                <h2>Verify your email</h2>

                <p className="auth-subtitle">
                    Enter the 6-digit code sent to your email address.
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
                                setEmail(event.target.value)
                            }
                            required
                        />
                    </label>

                    <label>
                        Verification code
                        <input
                            type="text"
                            inputMode="numeric"
                            autoComplete="one-time-code"
                            placeholder="123456"
                            value={otp}
                            onChange={(event) =>
                                setOtp(
                                    event.target.value
                                        .replace(/\D/g, "")
                                        .slice(0, 6)
                                )
                            }
                            maxLength={6}
                            required
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
                            ? "Verifying..."
                            : "Verify email"}
                    </button>
                </form>

                <p className="auth-footer">
                    Already verified?{" "}
                    <Link to="/login">Sign in</Link>
                </p>
            </div>
        </div>
    );
}
