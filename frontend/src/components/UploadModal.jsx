import { useState } from "react";
import { uploadFile } from "../services/api";

export default function UploadModal({
    onClose,
    onUploaded
}) {
    const [file, setFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    async function handleUpload(event) {
        event.preventDefault();

        if (!file) {
            setError("Please choose a file.");
            return;
        }

        setLoading(true);
        setError("");

        try {
            const uploaded = await uploadFile(file);

            onUploaded(uploaded);
            onClose();
        } catch (err) {
            setError(
                err.message || "Upload failed."
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <div
            className="modal-overlay"
            onClick={onClose}
        >
            <div
                className="modal"
                onClick={(event) =>
                    event.stopPropagation()
                }
            >
                <h2>Upload File</h2>

                <p className="modal-description">
                    Select a file to securely upload.
                </p>

                <form onSubmit={handleUpload}>
                    <label className="file-input">
                        <span>
                            {file
                                ? file.name
                                : "Choose a file"}
                        </span>

                        <input
                            type="file"
                            onChange={(event) => {
                                setFile(
                                    event.target.files?.[0] ||
                                    null
                                );
                                setError("");
                            }}
                        />
                    </label>

                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}

                    <div className="modal-actions">
                        <button
                            type="button"
                            className="secondary-button"
                            onClick={onClose}
                            disabled={loading}
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            className="primary-button"
                            disabled={loading}
                        >
                            {loading
                                ? "Uploading..."
                                : "Upload"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}