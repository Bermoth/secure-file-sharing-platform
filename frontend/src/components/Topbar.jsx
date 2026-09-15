export default function Topbar({ title, subtitle }) {
    const user = JSON.parse(
        localStorage.getItem("user") || "null"
    );

    const email = user?.email || "User";
    const initial = email.charAt(0).toUpperCase();

    return (
        <header className="topbar">
            <div>
                <h1>{title}</h1>

                {subtitle && (
                    <p>{subtitle}</p>
                )}
            </div>

            <div className="profile">
                <div className="profile-avatar">
                    {initial}
                </div>

                <div className="profile-info">
                    <strong>{email}</strong>
                    <span>Account</span>
                </div>
            </div>
        </header>
    );
}