import { useState } from "react";

import {
    deleteFile,
    downloadFile
} from "../services/api";

import ConfirmModal from "./ConfirmModal";
import ShareModal from "./ShareModal";

function formatSize(bytes) {
    if (!bytes) {
        return "0 B";
    }

    const units = [
        "B",
        "KB",
        "MB",
        "GB"
    ];

    const index = Math.floor(
        Math.log(bytes) / Math.log(1024)
    );

    return `${(
        bytes /
        Math.pow(1024, index)
    ).toFixed(index === 0 ? 0 : 1)} ${
        units[index]
    }`;
}

function formatDate(date) {
    if (!date) {
        return "Unknown";
    }

    return new Date(date).toLocaleDateString();
}

export default function FileRow({
    file,
    onDeleted,
    showOwner = false
}) {
    const [downloading, setDownloading] =
        useState(false);

    const [showShare, setShowShare] =
        useState(false);

    const [showDelete, setShowDelete] =
        useState(false);

    const [error, setError] =
        useState("");

    async function handleDownload() {
        setDownloading(true);
        setError("");

        try {
            const blob =
                await downloadFile(file.id);

            const url =
                window.URL.createObjectURL(blob);

            const link =
                document.createElement("a");

            link.href = url;
            link.download =
                file.originalFilename ||
                "download";

            document.body.appendChild(link);
            link.click();

            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            setError(
                err.message ||
                "Download failed."
            );
        } finally {
            setDownloading(false);
        }
    }

    async function handleDelete() {
        setError("");

        try {
            await deleteFile(file.id);

            setShowDelete(false);

            if (onDeleted) {
                onDeleted(file.id);
            }
        } catch (err) {
            setError(
                err.message ||
                "Could not delete file."
            );
        }
    }

    return (
        <>
            <div className="file-row">
                <div className="file-name-cell">
                    <div className="file-icon">
                        ▣
                    </div>

                    <div>
                        <strong>
                            {file.originalFilename}
                        </strong>

                        <span>
                            {file.contentType ||
                                "File"}
                        </span>
                    </div>
                </div>

                <div>
                    {formatSize(file.size)}
                </div>

                <div>
                    {formatDate(file.createdAt)}
                </div>

                {showOwner && (
                    <div>
                        {file.owner?.username ||
                            file.owner?.email ||
                            "Unknown"}
                    </div>
                )}

                <div className="file-actions">
                    <button
                        className="icon-button"
                        title="Download"
                        onClick={handleDownload}
                        disabled={downloading}
                    >
                        {downloading
                            ? "..."
                            : "↓"}
                    </button>

                    {!showOwner && (
                        <>
                            <button
                                className="icon-button"
                                title="Share"
                                onClick={() =>
                                    setShowShare(true)
                                }
                            >
                                ↗
                            </button>

                            <button
                                className="icon-button danger-icon"
                                title="Delete"
                                onClick={() =>
                                    setShowDelete(true)
                                }
                            >
                                ×
                            </button>
                        </>
                    )}
                </div>
            </div>

            {error && (
                <div className="row-error">
                    {error}
                </div>
            )}

            {showShare && (
                <ShareModal
                    file={file}
                    onClose={() =>
                        setShowShare(false)
                    }
                />
            )}

            {showDelete && (
                <ConfirmModal
                    title="Delete file?"
                    message={`Are you sure you want to permanently delete "${file.originalFilename}"?`}
                    confirmText="Delete"
                    danger
                    onCancel={() =>
                        setShowDelete(false)
                    }
                    onConfirm={handleDelete}
                />
            )}
        </>
    );
}