package kz.aitu.sdp.element;

import kz.aitu.sdp.visitor.FileSystemVisitor;

public interface Element {
    String getName();
    DirectoryNode getParent();
    void setParent(DirectoryNode parent);
    void accept(FileSystemVisitor visitor);
}
