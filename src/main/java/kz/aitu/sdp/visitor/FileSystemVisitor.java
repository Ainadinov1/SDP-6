package kz.aitu.sdp.visitor;

import kz.aitu.sdp.element.DirectoryNode;
import kz.aitu.sdp.element.FileNode;

public interface FileSystemVisitor {
    void visit(FileNode file);
    void visit(DirectoryNode directory);
}
