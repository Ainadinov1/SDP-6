package kz.aitu.sdp.element;

import kz.aitu.sdp.visitor.FileSystemVisitor;
import java.util.Objects;

public class FileNode implements Element {
    private final String name;
    private final long size;
    private DirectoryNode parent;

    public FileNode(String name, long size) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name must not be null or blank");
        }
        if (size < 0) {
            throw new IllegalArgumentException("File size must be non-negative: " + size);
        }
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    @Override
    public DirectoryNode getParent() {
        return parent;
    }

    @Override
    public void setParent(DirectoryNode parent) {
        this.parent = parent;
    }

    @Override
    public void accept(FileSystemVisitor visitor) {
        Objects.requireNonNull(visitor, "Visitor must not be null");
        visitor.visit(this);
    }
}
