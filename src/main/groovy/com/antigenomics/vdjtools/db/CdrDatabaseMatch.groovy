package com.antigenomics.vdjtools.db

import com.antigenomics.vdjtools.Clonotype

/**
 * Created by mikesh on 10/22/14.
 */
class CdrDatabaseMatch {
    private static final List<MatchSubstitution> dummy = new LinkedList<>()
    public final Clonotype query
    public final boolean vMatch, jMatch
    private final List<MatchSubstitution> substitutions
    public final CdrDatabaseEntry subject

    CdrDatabaseMatch(Clonotype query, CdrDatabaseEntry subject, boolean vMatch, boolean jMatch) {
        this(query, subject, vMatch, jMatch, dummy)
    }

    CdrDatabaseMatch(Clonotype query, CdrDatabaseEntry subject, boolean vMatch, boolean jMatch,
                     List<MatchSubstitution> substitutions) {
        this.query = query
        this.vMatch = vMatch
        this.jMatch = jMatch
        this.subject = subject
        this.substitutions = substitutions
    }


    List<MatchSubstitution> getSubstitutions() {
        Collections.unmodifiableList(substitutions)
    }

    public static final String HEADER = "query_cdr3aa\tquery_v\tquery_j\t" +
            "subject_cdr3aa\tsubject_v\tsubject_j\t" +
            "v_match\tj_match\tsubstitutions"

    @Override
    String toString() {
        [query.cdr3aa, query.v, query.j,
         subject.cdr3aa, subject.v, subject.j,
         vMatch, jMatch, substitutions.size() > 0 ? substitutions.collect { it.toString() }.join(",") : ".",
         subject.annotation.collect()].flatten().join("\t")
    }
}