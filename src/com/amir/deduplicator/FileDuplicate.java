package com.amir.deduplicator;

import java.util.Collection;

public class FileDuplicate {

    final String name;
    final Collection<String> paths;

    FileDuplicate(final String name,
                  final Collection<String> paths) {
        this.name = name;
        this.paths = paths;
    }

    @Override
    public String toString() {
        return this.name + "\n" + "\t" + this.paths;
    }

}
