/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.vdjtools.io.parser

import com.antigenomics.vdjtools.Software
import com.antigenomics.vdjtools.sample.Clonotype
import com.antigenomics.vdjtools.sample.Sample

import static com.antigenomics.vdjtools.util.CommonUtil.*

/**
 * A clonotype parser implementation that handles output from MiXCR software, see
 * {@url http://mixcr.milaboratory.com/}
 */
public class MiXcrParser extends ClonotypeStreamParser {
    boolean initialized = false;
    int countColumn, freqColumn, cdr3ntColumn, cdr3aaColumn,
        vHitsColumn, dHitsColumn, jHitsColumn,
        vAlignmentsColumn, dAlignmentsColumn, jAlignmentsColumn

    /**
     * {@inheritDoc}
     */
    protected MiXcrParser(Iterator<String> innerIter, Sample sample) {
        super(innerIter, Software.MiXcr, sample)
    }

    /**
     * Performs parser initialization based on the header string.
     *
     * @throws RuntimeException if header line doesn't contain required columns
     */
    private synchronized void ensureInitialized() {
        if (initialized)
            return;

        // Parsing header line to determine positions of certain columns with clones properties

        String headerLine = this.header[0];
        String[] splitHeaderLine = headerLine.split(software.delimiter)

        countColumn = splitHeaderLine.findIndexOf { it.equalsIgnoreCase("Clone count") }
        freqColumn = splitHeaderLine.findIndexOf { it.equalsIgnoreCase("Clone fraction") }
        cdr3ntColumn = splitHeaderLine.findIndexOf { it.equalsIgnoreCase("N. Seq. CDR3") }
        cdr3aaColumn = splitHeaderLine.findIndexOf { it.equalsIgnoreCase("AA. Seq. CDR3") }
        vAlignmentsColumn = splitHeaderLine.findIndexOf { it =~ /(?i)V alignment/ }
        dAlignmentsColumn = splitHeaderLine.findIndexOf { it =~ /(?i)D alignment/ }
        jAlignmentsColumn = splitHeaderLine.findIndexOf { it =~ /(?i)J alignment/ }
        vHitsColumn = splitHeaderLine.findIndexOf { it =~ /(?i)V hits/ }
        dHitsColumn = splitHeaderLine.findIndexOf { it =~ /(?i)D hits/ }
        jHitsColumn = splitHeaderLine.findIndexOf { it =~ /(?i)J hits/ }
        if (countColumn == -1 || freqColumn == -1 || cdr3ntColumn == -1 || cdr3aaColumn == -1 ||
                vAlignmentsColumn == -1 || dAlignmentsColumn == -1 || jAlignmentsColumn == -1)
            throw new RuntimeException("Some mandatory columns are absent in the input file.");

        // Initialized
        initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Clonotype innerParse(String clonotypeString) {
        ensureInitialized()

        def splitString = clonotypeString.split(software.delimiter)

        def count = splitString[countColumn].toInteger()
        def freq = splitString[freqColumn].toDouble()

        def cdr3nt = splitString[cdr3ntColumn]

        def cdr3aa = splitString[cdr3aaColumn] // no need to unify, MiXCR is based on milib


        String v, d, j
        (v, d, j) = extractVDJ(splitString[[vHitsColumn, dHitsColumn, jHitsColumn]])

        List<Alignment> vAlignemtns = parseAlignments(splitString[vAlignmentsColumn])
        List<Alignment> dAlignemtns = parseAlignments(splitString[dAlignmentsColumn])
        List<Alignment> jAlignemtns = parseAlignments(splitString[jAlignmentsColumn])

        def segmPoints = [vAlignemtns[0].seq2End - 1,
                          dAlignemtns.size() > 0 ? dAlignemtns[0].seq2Begin : -1,
                          dAlignemtns.size() > 0 ? dAlignemtns[0].seq2End - 1 : -1,
                          jAlignemtns[0].seq2Begin] as int[]

        boolean inFrame = inFrame(cdr3aa),
                noStop = noStop(cdr3aa),
                isComplete = true

        new Clonotype(sample, count, freq,
                segmPoints, v, d, j, cdr3nt, cdr3aa,
                inFrame, noStop, isComplete)
    }

    private static List<Alignment> parseAlignments(String alignmentsLine) {
        if (alignmentsLine.isEmpty())
            return Collections.EMPTY_LIST

        String[] splitByAlignments = alignmentsLine.split(",")
        List<Alignment> ret = new ArrayList<>()
        for (String alignmentString : splitByAlignments) {
            String[] splitByFields = alignmentString.split("\\|")
            ret.add(new Alignment(splitByFields[0].toInteger(), splitByFields[1].toInteger(),
                    splitByFields[3].toInteger(), splitByFields[4].toInteger()));
        }
        return ret;
    }

    /**
     * Now all positions are in coordinates of clonal sequence (not CDR3 coordinates). In case of non-CDR3 clonal
     * sequence there is currently no way to converto positions to CDR3 positions, so non-CDR3 positions will be
     * forbidden before appropriate information will be made available in MiXCR output.
     *
     * Seq1 - reference sequence
     *
     * Seq2 - clonal sequence
     */
    private static class Alignment {
        int seq1Begin, seq1End, seq2Begin, seq2End

        Alignment(int seq1Begin, int seq1End, int seq2Begin, int seq2End) {
            this.seq1Begin = seq1Begin
            this.seq1End = seq1End
            this.seq2Begin = seq2Begin
            this.seq2End = seq2End
        }
    }
}
