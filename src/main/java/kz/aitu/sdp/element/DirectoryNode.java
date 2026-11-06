package kz.aitu.sdp.element;

import kz.aitu.sdp.visitor.FileSystemVisitor;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DirectoryNode implements Element {
    private final String name;
    private DirectoryNode parent;
    private final Map<String, Element> children = new LinkedHashMap<>();

    public DirectoryNode(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Directory name must not be null or blank");
        }
        this.name = name;
    }

    public void addElement(Element child) {
        Objects.requireNonNull(child, "Child element must not be null");

        if (child == this) {
            throw new IllegalArgumentException("Cycle detected: Directory cannot contain itself");
        }
        if (child.getParent() != null) {
            throw new IllegalStateException("Element already belongs to another parent: " + child.getName());
        }
        if (children.containsKey(child.getName())) {
            throw new IllegalArgumentException("Duplicate sibling name in directory: " + child.getName());
        }

        DirectoryNode current = this;
        while (current != null) {
            if (current == child) {
                throw new IllegalArgumentException("Cycle detected in directory hierarchy");
            }
            current = current.getParent();
        }

        child.setParent(this);
        children.put(child.getName(), child);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DirectoryNode getParent() {
        return parent;
    }

    @Override
    public void setParent(DirectoryNode parent) {
        this.parent = parent;
    }

    public Collection<Element> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

    @Override
    public void accept(FileSystemVisitor visitor) {
        Objects.requireNonNull(visitor, "Visitor must not be null");
        visitor.visit(this);
    }
}
