import { useEffect, useState } from "react";

import Layout from "../components/Layout";
import FileTable from "../components/FileTable";

import { getSharedFiles } from "../services/api";

export default function Shared() {
    const [files, setFiles] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");

    async function loadFiles() {
        setLoading(true);
        setError("");

        try {
            const data =
                await getSharedFiles();

            setFiles(data || []);
        } catch (err) {
            setError(
                err.message ||
                "Could not load shared files."
            );
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadFiles();
    }, []);

    return (
        <Layout
            title="Shared With Me"
            subtitle="Files other users have shared with you"
        >
            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            {loading ? (
                <div className="loading-state">
                    Loading shared files...
                </div>
            ) : (
                <FileTable
                    files={files}
                    showOwner
                />
            )}
        </Layout>
    );
}