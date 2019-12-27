package org.lappsgrid.health.model

/**
 *
 */
class Version implements Comparable<Version> {
    int major
    int minor
    int revision
    String qualifier

    Version(String input) {
//        println "Initializing $input"
        String[] parts = input.split("\\.")
        major = parts[0] as int
        minor = parts[1] as int
        int hyphen = parts[2].indexOf('-')
        if (hyphen > 0) {
            String[] revs = parts[2].split('-')
            revision = parts[2].substring(0, hyphen) as int
            qualifier = parts[2].substring(hyphen+1)
        }
        else {
            revision = parts[2] as int
            qualifier = null
        }
    }

    @Override
    int compareTo(Version v) {
//        println "Comparing $this to $v"
        int delta = this.major - v.major
//        println "Major delta is $delta"
        if (delta != 0) return delta
        delta = this.minor - v.minor
//        println "Minor delta is $delta"
        if (delta != 0) return delta
        delta = revision - v.revision
//        println "Rev delta is $delta"
        if (delta != 0) return delta
        if (qualifier == null && v.qualifier == null) return 0
        if (qualifier == null) return 1
        if (v.qualifier == null) return -1
        return qualifier.compareTo(v.qualifier)
    }

    @Override
    boolean equals(Object o) {
        if (o == null || !(o instanceof Version)) {
            return false
        }
        return this.compareTo((Version) o) == 0
    }

    String toString() {
        return "${major}.${minor}.${revision}"
    }
}
