import { useState } from "react";

import Layout from "../components/Layout";

export default function Settings() {
    const user = JSON.parse(
        localStorage.getItem("user") || "null"
    );

    const [saved, setSaved] =
        useState(false);

    function handleSave(event) {
        event.preventDefault();

        setSaved(true);

        setTimeout(() => {
            setSaved(false);
        }, 2500);
    }

    return (
        <Layout
            title="Settings"
            subtitle="Manage your account"
        >
            <div className="settings-card">
                <h2>Account Information</h2>

                <form
                    className="settings-form"
                    onSubmit={handleSave}
                >
                    <label>
                        Email
                        <input
                            type="email"
                            value={
                                user?.email || ""
                            }
                            readOnly
                        />
                    </label>

                    <label>
                        Account
                        <input
                            type="text"
                            value="Active"
                            readOnly
                        />
                    </label>

                    <button
                        className="primary-button"
                        type="submit"
                    >
                        Save changes
                    </button>

                    {saved && (
                        <div className="success-message">
                            Changes saved successfully.
                        </div>
                    )}
                </form>
            </div>

            <div className="settings-card">
                <h2>Security</h2>

                <p>
                    Your authentication token is stored locally
                    in your browser and sent with protected API
                    requests.
                </p>

                <p className="muted">
                    All protected file operations require
                    authentication.
                </p>
            </div>
        </Layout>
    );
}