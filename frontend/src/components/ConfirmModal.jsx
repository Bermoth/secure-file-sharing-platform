export default function ConfirmModal({
    title,
    message,
    confirmText = "Confirm",
    onConfirm,
    onCancel,
    danger = false
}) {
    return (
        <div
            className="modal-overlay"
            onClick={onCancel}
        >
            <div
                className="modal"
                onClick={(event) =>
                    event.stopPropagation()
                }
            >
                <h2>{title}</h2>

                <p>{message}</p>

                <div className="modal-actions">
                    <button
                        className="secondary-button"
                        onClick={onCancel}
                    >
                        Cancel
                    </button>

                    <button
                        className={
                            danger
                                ? "danger-button"
                                : "primary-button"
                        }
                        onClick={onConfirm}
                    >
                        {confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
}