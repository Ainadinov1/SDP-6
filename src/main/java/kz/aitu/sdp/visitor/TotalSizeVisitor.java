package kz.aitu.sdp.visitor;

import kz.aitu.sdp.element.DirectoryNode;
import kz.aitu.sdp.element.Element;
import kz.aitu.sdp.element.FileNode;

public class TotalSizeVisitor implements FileSystemVisitor {
    private long totalBytes = 0;

    public void reset() {
        totalBytes = 0;
    }

    @Override
    public void visit(FileNode file) {
        totalBytes += file.getSize();
    }

    @Override
    public void visit(DirectoryNode directory) {
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}
