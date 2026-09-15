import { useEffect, useState } from "react";

import Layout from "../components/Layout";
import UploadModal from "../components/UploadModal";
import FileTable from "../components/FileTable";
import EmptyState from "../components/EmptyState";

import {
    getMyFiles,
    getSharedFiles
} from "../services/api";

export default function Dashboard() {
    const [files, setFiles] =
        useState([]);

    const [sharedFiles, setSharedFiles] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");

    const [showUpload, setShowUpload] =
        useState(false);

    async function loadData() {
        setLoading(true);
        setError("");

        try {
            const [
                myFiles,
                shared
            ] = await Promise.all([
                getMyFiles(),
                getSharedFiles()
            ]);

            setFiles(myFiles || []);
            setSharedFiles(shared || []);
        } catch (err) {
            setError(
                err.message ||
                "Could not load dashboard."
            );
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadData();
    }, []);

    function handleUploaded(file) {
        setFiles((current) => [
            file,
            ...current
        ]);
    }

    function handleDeleted(id) {
        setFiles((current) =>
            current.filter(
                (file) => file.id !== id
            )
        );
    }

    return (
        <Layout
            title="Dashboard"
            subtitle="Manage your secure files"
        >
            <div className="dashboard-actions">
                <button
                    className="primary-button"
                    onClick={() =>
                        setShowUpload(true)
                    }
                >
                    + Upload File
                </button>
            </div>

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            <div className="stats-grid">
                <div className="stat-card">
                    <span>My Files</span>
                    <strong>
                        {loading
                            ? "..."
                            : files.length}
                    </strong>
                </div>

                <div className="stat-card">
                    <span>Shared With Me</span>
                    <strong>
                        {loading
                            ? "..."
                            : sharedFiles.length}
                    </strong>
                </div>

                <div className="stat-card">
                    <span>Total Storage</span>
                    <strong>
                        {loading
                            ? "..."
                            : `${(
                                files.reduce(
                                    (
                                        total,
                                        file
                                    ) =>
                                        total +
                                        (file.size ||
                                            0),
                                    0
                                ) /
                                (1024 * 1024)
                            ).toFixed(1)} MB`}
                    </strong>
                </div>
            </div>

            <section className="content-section">
                <div className="section-header">
                    <div>
                        <h2>Recent Files</h2>
                        <p>
                            Your latest uploaded files
                        </p>
                    </div>
                </div>

                {loading ? (
                    <div className="loading-state">
                        Loading files...
                    </div>
                ) : files.length === 0 ? (
                    <EmptyState
                        icon="↑"
                        title="No files uploaded"
                        message="Upload your first file to get started."
                    />
                ) : (
                    <FileTable
                        files={files.slice(0, 5)}
                        onDeleted={handleDeleted}
                    />
                )}
            </section>

            {showUpload && (
                <UploadModal
                    onClose={() =>
                        setShowUpload(false)
                    }
                    onUploaded={handleUploaded}
                />
            )}
        </Layout>
    );
}