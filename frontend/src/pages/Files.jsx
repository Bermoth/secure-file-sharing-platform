import { useEffect, useState } from "react";

import Layout from "../components/Layout";
import FileTable from "../components/FileTable";
import UploadModal from "../components/UploadModal";

import { getMyFiles } from "../services/api";

export default function Files() {
    const [files, setFiles] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");

    const [showUpload, setShowUpload] =
        useState(false);

    async function loadFiles() {
        setLoading(true);
        setError("");

        try {
            const data =
                await getMyFiles();

            setFiles(data || []);
        } catch (err) {
            setError(
                err.message ||
                "Could not load files."
            );
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadFiles();
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
            title="My Files"
            subtitle="Files you have uploaded"
        >
            <div className="page-toolbar">
                <div>
                    <strong>
                        {files.length}{" "}
                        {files.length === 1
                            ? "file"
                            : "files"}
                    </strong>
                </div>

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

            {loading ? (
                <div className="loading-state">
                    Loading files...
                </div>
            ) : (
                <FileTable
                    files={files}
                    onDeleted={handleDeleted}
                />
            )}

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