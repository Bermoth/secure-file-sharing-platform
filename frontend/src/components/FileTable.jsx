import FileRow from "./FileRow";
import EmptyState from "./EmptyState";

export default function FileTable({
    files,
    onDeleted,
    showOwner = false
}) {
    if (!files || files.length === 0) {
        return (
            <EmptyState
                icon="□"
                title="No files yet"
                message="There are no files to display."
            />
        );
    }

    return (
        <div className="file-table">
            <div
                className={
                    showOwner
                        ? "file-table-header with-owner"
                        : "file-table-header"
                }
            >
                <div>Name</div>
                <div>Size</div>
                <div>Date</div>

                {showOwner && (
                    <div>Owner</div>
                )}

                <div>Actions</div>
            </div>

            {files.map((file) => (
                <FileRow
                    key={file.id}
                    file={file}
                    onDeleted={onDeleted}
                    showOwner={showOwner}
                />
            ))}
        </div>
    );
}