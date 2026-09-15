import { useEffect, useState } from "react";

import {
    getFileShares,
    revokeShare,
    shareFile
} from "../services/api";

export default function ShareModal({
    file,
    onClose
}) {
    const [email, setEmail] = useState("");
    const [shares, setShares] = useState([]);
    const [loading, setLoading] = useState(true);
    const [sharing, setSharing] = useState(false);
    const [error, setError] = useState("");

    async function loadShares() {
        try {
            const data = await getFileShares(file.id);
            setShares(data || []);
        } catch (err) {
            setError(
                err.message ||
                "Could not load shares."
            );
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadShares();
    }, [file.id]);

    async function handleShare(event) {
        event.preventDefault();

        if (!email.trim()) {
            setError("Enter an email address.");
            return;
        }

        setSharing(true);
        setError("");

        try {
            await shareFile(
                file.id,
                email.trim()
            );

            setEmail("");
            await loadShares();
        } catch (err) {
            setError(
                err.message ||
                "Could not share the file."
            );
        } finally {
            setSharing(false);
        }
    }

    async function handleRevoke(userId) {
        setError("");

        try {
            await revokeShare(
                file.id,
                userId
            );

            await loadShares();
        } catch (err) {
            setError(
                err.message ||
                "Could not revoke access."
            );
        }
    }

    return (
        <div
            className="modal-overlay"
            onClick={onClose}
        >
            <div
                className="modal share-modal"
                onClick={(event) =>
                    event.stopPropagation()
                }
            >
                <h2>Share File</h2>

                <p className="modal-description">
                    {file.originalFilename}
                </p>

                <form
                    className="share-form"
                    onSubmit={handleShare}
                >
                    <input
                        type="email"
                        placeholder="user@example.com"
                        value={email}
                        onChange={(event) =>
                            setEmail(event.target.value)
                        }
                    />

                    <button
                        className="primary-button"
                        type="submit"
                        disabled={sharing}
                    >
                        {sharing
                            ? "Sharing..."
                            : "Share"}
                    </button>
                </form>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <div className="shares-section">
                    <h3>People with access</h3>

                    {loading ? (
                        <p className="muted">
                            Loading...
                        </p>
                    ) : shares.length === 0 ? (
                        <p className="muted">
                            This file isn't shared with anyone.
                        </p>
                    ) : (
                        <div className="share-list">
                            {shares.map((user) => (
                                <div
                                    className="share-item"
                                    key={user.id}
                                >
                                    <div>
                                        <strong>
                                            {user.username ||
                                                user.email}
                                        </strong>

                                        {user.username &&
                                            user.email && (
                                                <span>
                                                    {user.email}
                                                </span>
                                            )}
                                    </div>

                                    <button
                                        className="small-danger-button"
                                        onClick={() =>
                                            handleRevoke(
                                                user.id
                                            )
                                        }
                                    >
                                        Revoke
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div className="modal-actions">
                    <button
                        className="secondary-button"
                        onClick={onClose}
                    >
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
}