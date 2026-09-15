import { NavLink, useNavigate } from "react-router-dom";

export default function Sidebar() {
    const navigate = useNavigate();

    function logout() {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        navigate("/login");
    }

    return (
        <aside className="sidebar">
            <div className="sidebar-logo">
                <div className="logo-icon">S</div>
                <div>
                    <h2>SecureShare</h2>
                    <span>File Sharing</span>
                </div>
            </div>

            <nav className="sidebar-nav">
                <NavLink
                    to="/dashboard"
                    className={({ isActive }) =>
                        isActive ? "nav-link active" : "nav-link"
                    }
                >
                    <span>⌂</span>
                    Dashboard
                </NavLink>

                <NavLink
                    to="/files"
                    className={({ isActive }) =>
                        isActive ? "nav-link active" : "nav-link"
                    }
                >
                    <span>▣</span>
                    My Files
                </NavLink>

                <NavLink
                    to="/shared"
                    className={({ isActive }) =>
                        isActive ? "nav-link active" : "nav-link"
                    }
                >
                    <span>↗</span>
                    Shared With Me
                </NavLink>

                <NavLink
                    to="/settings"
                    className={({ isActive }) =>
                        isActive ? "nav-link active" : "nav-link"
                    }
                >
                    <span>⚙</span>
                    Settings
                </NavLink>
            </nav>

            <div className="sidebar-bottom">
                <button
                    className="logout-button"
                    onClick={logout}
                >
                    <span>↪</span>
                    Log out
                </button>
            </div>
        </aside>
    );
}